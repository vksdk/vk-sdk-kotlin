package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.utils.vkapi.calls.Call;
import com.petersamokhin.bots.sdk.utils.vkapi.calls.CallAsync;
import com.petersamokhin.bots.sdk.utils.vkapi.calls.CallSync;
import com.petersamokhin.bots.sdk.utils.web.Connection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.petersamokhin.bots.sdk.clients.Client.scheduler;

/**
 * Best way to use VK API: you can call up to 25 vk api methods by call execute once
 * Because without execute you only can call up to 3 methods per second
 * <p>
 * See more: <a href="https://vk.com/dev/execute">link</a>
 */
public class Executor {

    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    public static boolean LOG_REQUESTS = false;

    /**
     * We can call 'execute' method no more than three times per second.
     * 1000/3 ~ 333 milliseconds
     */
    private static final int delay = 335;

    /**
     * Queue of requests
     */
    private volatile List<CallAsync> queue = new ArrayList<>();

    private final String URL = "https://api.vk.com/method/execute";
    private final String accessToken;
    private final String V = "&v=" + 5.69;


    /**
     * Init executor
     * <p>
     * All requests called directly by using client.api().call(...)
     * or indirectly (by calling methods that will use VK API) will be queued.
     * Every 350 milliseconds first 25 requests from queue will be executed.
     * VK response will be returned to the callback.
     * <p>
     * Important:
     *
     * @param accessToken Method 'execute' will be called using this access_token.
     * @see API#callSync(String, Object) requests made by this method wont be queued, be careful.
     * And responses of callSync seems like {"response":{...}} and all are instances of JSONObject.
     * but from method 'execute' will be returned "response" object directly (can be integer, boolean etc).
     */
    public Executor(String accessToken) {
        this.accessToken = "&access_token=" + accessToken;

        scheduler.scheduleWithFixedDelay(this::executing, 0, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Method that makes 'execute' requests
     * with first 25 calls from queue.
     */
    private void executing() {

        List<CallAsync> tmpQueue = new ArrayList<>();
        int count = 0;

        for (Iterator<CallAsync> iterator = queue.iterator(); iterator.hasNext() && count < 25; count++) {
            tmpQueue.add(iterator.next());
        }

        queue.removeAll(tmpQueue);

        StringBuilder calls = new StringBuilder();
        calls.append('[');

        for (int i = 0; i < count; i++) {
            String codeTmp = codeForExecute(tmpQueue.get(i));
            calls.append(codeTmp);
            if (i < count - 1) {
                calls.append(',');
            }
        }
        calls.append(']');

        String code = calls.toString();

        // Execute
        if (count > 0) {
            String vkCallParams = "code=return " + code + ";" + accessToken + V;

            String responseString = Connection.postRequestResponse(URL, vkCallParams);

            if (LOG_REQUESTS) {
                LOG.error("New executing request response: {}", responseString);
            }

            JSONObject response;

            try {
                response = new JSONObject(responseString);
            } catch (JSONException e) {
                tmpQueue.forEach(call -> call.getCallback().onResult("false"));
                LOG.error("Bad response from executing: {}, params: {}", responseString, vkCallParams);
                return;
            }

            if (response.has("execute_errors")) {
                try {
                    LOG.error("Errors when executing: {}, code: {}", response.get("execute_errors").toString(), URLDecoder.decode(code, "UTF-8"));
                } catch (UnsupportedEncodingException ignored) {
                }
            }

            if (!response.has("response")) {
                LOG.error("No 'response' object when executing code, VK response: {}", response);
                tmpQueue.forEach(call -> call.getCallback().onResult("false"));
                return;
            }

            JSONArray responses = response.getJSONArray("response");

            IntStream.range(0, count).forEachOrdered(i -> tmpQueue.get(i).getCallback().onResult(responses.get(i)));
        }
    }

    /**
     * Method that makes string in json format from call object.
     *
     * @param call Call object
     * @return String 'API.method.name({param:value})'
     * @see Call
     * @see CallAsync
     * @see CallSync
     */
    public String codeForExecute(Call call) {

        return "API." + call.getMethodName() + '(' + call.getParams().toString() + ')';
    }

    /**
     * Method that puts all requests in a queue.
     *
     * @param call Call to be executed.
     */
    public void execute(CallAsync call) {
        queue.add(call);
    }
}
