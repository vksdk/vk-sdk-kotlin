package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Best way to use VK API: you can call up to 25 vk api methods by call execute once
 * Because without execute you only can call up to 3 methods per second
 *
 * See more: <a href="https://vk.com/dev/execute">link</a>
 */
public class Executor {

    private static final int delay = 400;

    private volatile List<Call> queue = new ArrayList<>();

    private final String URL = "https://api.vk.com/method/execute?code=", accessToken, V = "&v=" + 5.67;

    // https://api.vk.com/method/execute?code=
    // code
    // // return [a, b, c, d];
    // // // a = API.method.one(params), b = API.method.two(params)
    // &access_token=MDA&v=5.67

    public Executor(String accessToken) {
        this.accessToken = accessToken;

        new Thread(this::executing).start();
    }

    private void executing() {

        while (true) {

            // Make code
            int count = queue.size() > 25 ? 25 : queue.size();
            StringBuilder calls = new StringBuilder();
            calls.append('[');

            for (int i = 0; i < count; i++) {
                String codeTmp = codeForExecute(queue.get(i));
                calls.append(codeTmp);
                if (i < count - 1) {
                    calls.append(',');
                }
            }
            calls.append(']');

            String code = "return null;";
            try {
                code = URLEncoder.encode(' ' + calls.toString(), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
            }

            // Execute
            if (count > 0) {
                String vkCallQuery = URL + "return" + code + ";" + accessToken + V;
                JSONObject response = Connection.getRequestResponse(vkCallQuery);
                JSONArray responses = response.getJSONArray("response");

                for (int i = 0; i < count; i++) {
                    queue.get(i).getCallback().onResponse(responses.get(i));
                }

                if (count <= queue.size()) {
                    queue = queue.subList(count, queue.size());
                }

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
