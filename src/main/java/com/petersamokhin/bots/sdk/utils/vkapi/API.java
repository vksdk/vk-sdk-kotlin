package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.utils.Utils;
import com.petersamokhin.bots.sdk.utils.vkapi.calls.CallAsync;
import com.petersamokhin.bots.sdk.utils.vkapi.calls.CallSync;
import com.petersamokhin.bots.sdk.utils.web.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple interacting with VK API
 */
public class API {

    private static final Logger LOG = LoggerFactory.getLogger(API.class);

    private static Executor executor;

    private String URL = "https://api.vk.com/method/", V = "&v=" + 5.67;
    private String accessToken;

    private static boolean executionStarted = false;

    /**
     * Get the token from client
     * todo Not all methods available with group tokens, and few methods available without token
     * todo Need to make client with both tokens, or any another conclusion
     *
     * @param client Client with token
     */
    public API(Client client) {
        this.accessToken = "&access_token=" + client.getAccessToken();
        if (!executionStarted) {
            executor = new Executor(client.getAccessToken());
            executionStarted = true;
        }
    }

    /**
     * todo Not all methods available with group tokens, and few methods available without token
     * todo Need to make client with both tokens, or any another conclusion
     *
     * @param token access_token
     */
    public API(String token) {
        this.accessToken = "&access_token=" + token;
        if (!executionStarted) {
            executor = new Executor(token);
            executionStarted = true;
        }
    }

    /**
     * Call to VK API
     *
     * @param method   Method name
     * @param params   Params as string, JSONObject or Map
     * @param callback Callback to return the response
     */
    public void call(String method, Object params, Callback<Object> callback) {

        try {
            JSONObject parameters = new JSONObject();

            if (params != null) {
                boolean good = false;

                // Work with map
                if (params instanceof Map) {

                    parameters = new JSONObject((Map) params);
                    good = true;
                }

                // with JO
                if (params instanceof JSONObject) {
                    parameters = (JSONObject) params;
                    good = true;
                }

                // or string
                if (params instanceof String) {
                    String s = params.toString();
                    if (s.startsWith("{")) {
                        parameters = new JSONObject(s);
                        good = true;
                    } else {
                        if (s.contains("&") && s.contains("=")) {
                            parameters = Utils.explodeQuery(s);
                            good = true;
                        }
                    }
                }

                if (good) {
                    CallAsync call = new CallAsync(method, parameters, callback);
                    executor.execute(call);
                }
            }
        } catch (Exception e) {
            LOG.error("Some error occured when calling VK API method {} with params {}, error is {}", method, params.toString(), e);
        }
    }

    /**
     * Call to VK API
     *
     * @param callback Callback to return the response
     * @param method   Method name
     * @param params   Floating count of params
     */
    public void call(Callback<Object> callback, String method, Object... params) {

        try {
            if (params != null) {

                if (params.length == 1) {
                    this.call(method, params[0], callback);
                }

                if (params.length > 1) {

                    if (params.length % 2 == 0) {
                        Map<String, Object> map = new HashMap<>();

                        for (int i = 0; i < params.length - 1; i += 2) {
                            map.put(params[i].toString(), params[i + 1]);
                        }

                        this.call(method, map, callback);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Some error occured when calling VK API: {}", e);
        }
    }

    /**
     * Call to 'execute' method, because can not call API.execute inside execute.
     * More: <a href="https://vk.com/dev/execute">link</a>;
     */
    public JSONObject execute(String code) {

        return new JSONObject(callSync("execute", new JSONObject().put("code", code)));
    }

    /**
     * Execute float count of calls, up to 25
     *
     * @param calls single call to VK API or calls separated by comma.
     * @see CallAsync
     */
    public void execute(CallAsync... calls) {
        if (calls.length < 26) {
            for (CallAsync call : calls) {
                executor.execute(call);
            }
        } else {
            CallAsync[] newCalls = new CallAsync[25];
            System.arraycopy(calls, 0, newCalls, 0, 25);
            for (CallAsync call : newCalls) {
                executor.execute(call);
            }
        }
    }

    /**
     * Execute float count of calls, up to 25
     *
     * @param calls single call to VK API or calls separated by comma.
     * @return JSONArray with responses of calls
     * @see CallSync
     */
    public JSONArray execute(CallSync... calls) {

        StringBuilder code = new StringBuilder("return [");

        for (int i = 0; i < calls.length; i++) {
            String codeTmp = executor.codeForExecute(calls[i]);
            code.append(codeTmp);
            if (i < calls.length - 1) {
                code.append(',');
            }
        }
        code.append("];");

        JSONObject response = null;
        try {
            response = new JSONObject(callSync("execute", new JSONObject().put("code", URLEncoder.encode(code.toString(), "UTF-8"))));
        } catch (UnsupportedEncodingException ignored) {
        }

        return response.getJSONArray("response");
    }

    /**
     * Call to VK API
     *
     * @param method Method name
     * @param params Params as string, JSONObject or Map
     * @return JSONObject response of VK answer
     * @deprecated not safe to use this method, because all async methods are in queue
     * and will be called in execute method, that can call 25 methods by one call
     */
    @Deprecated
    public String callSync(String method, Object params) {

        try {
            if (params != null) {

                String paramsString;

                // Work with map
                if (params instanceof Map) {

                    paramsString = Utils.MapToURLParamsQuery(new JSONObject((Map) params));

                } else {

                    paramsString = String.valueOf(params);

                    if (paramsString.startsWith("{")) {

                        paramsString = Utils.MapToURLParamsQuery(new JSONObject(paramsString));

                    }
                }

                String query = URL + method + "?" + paramsString + accessToken + V;

                return Connection.getRequestResponse(query);
            }
        } catch (Exception e) {
            LOG.error("Some error occured when calling VK API: {}", e);
        }
        return "error";
    }

    /**
     * Call to VK API
     *
     * @param method Method name
     * @param params Floating count of params
     * @return JSONObject response of VK answer
     */
    @Deprecated
    public String callSync(String method, Object... params) {

        try {
            if (params != null && params.length > 0) {

                String query = URL + method + "?" + Utils.paramsToString(params) + accessToken + V;

                return Connection.getRequestResponse(query);
            }
        } catch (Exception e) {
            LOG.error("Some error occured when calling VK API: {}", e);
        }

        return "";
    }
}
