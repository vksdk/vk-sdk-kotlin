package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.callbacks.callbackapi.ExecuteCallback;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.utils.Connection;
import com.petersamokhin.bots.sdk.utils.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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
    private byte LOG_LEVEL = 0;

    /**
     * Get the token from client
     * todo Not all methods available with group tokens, and few methods available without token
     * todo Need to make client with both tokens, or any another conclusion
     *
     * @param client Client with token
     */
    public API(Client client) {
        this.accessToken = "&access_token=" + client.getAccessToken();
        executor = new Executor(accessToken);
    }

    /**
     * todo Not all methods available with group tokens, and few methods available without token
     * todo Need to make client with both tokens, or any another conclusion
     *
     * @param token access_token
     */
    public API(String token) {
        this.accessToken = "&access_token=" + token;
        executor = new Executor(accessToken);
    }

    /**
     * Call to VK API
     *
     * @param method   Method name
     * @param params   Params as string, JSONObject or Map
     * @param callback Callback to return the response
     */
    public void call(String method, Object params, ExecuteCallback callback) {

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
                Call call = new Call(method, parameters, callback);
                executor.execute(call);
            }
        }
    }

    /**
     * Call to VK API
     *
     * @param callback Callback to return the response
     * @param method   Method name
     * @param params   Floating count of params
     */
    public void call(ExecuteCallback callback, String method, Object... params) {

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
    public JSONObject callSync(String method, Object params) {

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

            JSONObject response = Connection.getRequestResponse(query);

            switch (getLogLevel()) {

                case 1: {
                    System.out.println("[New call to API] Query: " + query);
                    break;
                }

                case 2: {
                    System.out.println("[New call to API] Response: " + response);
                    break;
                }

                case 3: {
                    System.out.println("[New call to API] Query: " + query);
                    System.out.println("[New call to API] Response: " + query);
                    break;
                }
            }

            return response;
        }
        return new JSONObject();
    }

    /**
     * Call to VK API
     *
     * @param method Method name
     * @param params Floating count of params
     * @return JSONObject response of VK answer
     */
    @Deprecated
    public JSONObject callSync(String method, Object... params) {

        if (params != null && params.length > 0) {

            String query = URL + method + "?" + Utils.paramsToString(params) + accessToken + V;

            JSONObject response = Connection.getRequestResponse(query);

            switch (getLogLevel()) {

                case 1: {
                    System.out.println("[New call to API] Query: " + query);
                    break;
                }

                case 2: {
                    System.out.println("[New call to API] Response: " + response);
                    break;
                }

                case 3: {
                    System.out.println("[New call to API] Query: " + query);
                    System.out.println("[New call to API] Response: " + query);
                    break;
                }
            }

            return response;
        }

        return new JSONObject();
    }


    /**
     * Logging properties:
     * 0 - no info
     * 1 - print every call query to console
     * 2 - print every call query and response to console
     *
     * @param newLogLevel new log level
     */
    public void setLogLevel(byte newLogLevel) {
        this.LOG_LEVEL = newLogLevel;
    }

    /**
     * Get current log level:
     * 0 - no info
     * 1 - print every call query to console
     * 2 - print every call query and response to console
     */
    public byte getLogLevel() {
        return this.LOG_LEVEL;
    }
}
