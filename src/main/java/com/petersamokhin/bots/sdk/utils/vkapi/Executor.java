package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Best way to use VK API: you can call up to 25 vk api methods by call execute once
 * Because without execute you only can call up to 3 methods per second
 * <p>
 * See more: <a href="https://vk.com/dev/execute">link</a>
 */
public class Executor {

    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    private static final int delay = 400;

    private volatile List<Call> queue = new ArrayList<>();

    private final String URL = "https://api.vk.com/method/execute?code=";
    private final String accessToken;
    private final String V = "&v=" + 5.67;

    // https://api.vk.com/method/execute?code=
    // code
    // // return [a, b, c, d];
    // // // a = API.method.one(params), b = API.method.two(params)
    // &access_token=MDA&v=5.67

    public Executor(String accessToken) {
        this.accessToken = "&access_token=" + accessToken;
        new Thread(this::executing).start();
    }

    private void executing() {

        while (true) {

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

                if (!response.has("response")) {
                    LOG.error("No 'response' object when executing code, VK response: {}", response);
                    tmpQueue.forEach(call -> call.getCallback().onResponse("false"));
                    continue;
                }

                JSONArray responses = response.getJSONArray("response");

                IntStream.range(0, count).forEachOrdered(i -> tmpQueue.get(i).getCallback().onResponse(responses.get(i)));

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private String codeForExecute(Call call) {

        return new StringBuilder().append("API.").append(call.getMethodName()).append('(').append(call.getParams().toString()).append(')').toString();
    }

    public void execute(Call call) {
        queue.add(call);
    }
}
