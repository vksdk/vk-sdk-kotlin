package com.petersamokhin.bots.sdk.utils;

import com.petersamokhin.bots.sdk.clients.Client;
import org.json.JSONObject;

import java.util.Map;

/**
 * Simple interacting with VK API
 */
public class API {

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
    }

    /**
     * todo Not all methods available with group tokens, and few methods available without token
     * todo Need to make client with both tokens, or any another conclusion
     *
     * @param token access_token
     */
    public API(String token) {
        this.accessToken = "&access_token=" + token;
    }

    /**
     * Call to VK API
     *
     * @param method Method name
     * @param params Params as string, JSONObject or Map
     * @return JSONObject response of VK answer
     */
    public JSONObject call(String method, Object params) {

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
    public JSONObject call(String method, Object... params) {

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
