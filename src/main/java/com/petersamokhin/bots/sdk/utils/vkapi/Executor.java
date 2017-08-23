package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Best way to use VK API: you can call up to 25 vk api methods by call execute once
 * Because without execute you only can call up to 3 methods per second
 * <p>
 * See more: <a href="https://vk.com/dev/execute">link</a>
 */
public class Executor {

    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    /**
     * We can call 'execute' method no more than three times per second.
     * 1000/3 ~ 333 milliseconds
     */
    private static final int delay = 335;

    /**
     * Queue of requests
     */
    private volatile List<Call> queue = new ArrayList<>();

    private final String URL = "https://api.vk.com/method/execute?code=";
    private final String accessToken;
    private final String V = "&v=" + 5.67;

    private static final ScheduledExecutorService sheduler = Executors.newSingleThreadScheduledExecutor();

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
        sheduler.scheduleWithFixedDelay(this::executing, 0, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Method that makes 'execute' requests
     * with first 25 calls from queue.
     */
    private void executing() {


        List<Call> tmpQueue = new ArrayList<>();
        int count = 0;

        for (Iterator<Call> iterator = queue.iterator(); iterator.hasNext() && count < 25; count++) {
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

        String code = "return null;";
        try {
            code = URLEncoder.encode(calls.toString(), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        // Execute
        if (count > 0) {
            String vkCallQuery = URL + "return " + code + ";" + accessToken + V;
            JSONObject response = Connection.getRequestResponse(vkCallQuery);

            if (response.has("execute_errors")) {
                try {
                    LOG.error("Errors when exeturing " + URLDecoder.decode(code, "UTF-8") + ", error: {}", response.get("execute_errors").toString());
                } catch (UnsupportedEncodingException ignored) {
                }
            }

            if (!response.has("response")) {
                LOG.error("No 'response' object when executing code, VK response: {}", response);
                tmpQueue.forEach(call -> call.getCallback().onResponse("false"));
                return;
            }

            JSONArray responses = response.getJSONArray("response");

            IntStream.range(0, count).forEachOrdered(i -> tmpQueue.get(i).getCallback().onResponse(responses.get(i)));

            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Method that makes string in json format from call object.
     *
     * @param call Call object
     * @return String 'API.method.name({param:value})'
     * @see Call
     */
    private String codeForExecute(Call call) {

        return "API." + call.getMethodName() + '(' + call.getParams().toString() + ')';
    }

    /**
     * Method that puts all requests in a queue.
     *
     * @param call Call to be executed.
     */
    public void execute(Call call) {
        queue.add(call);
    }
}
