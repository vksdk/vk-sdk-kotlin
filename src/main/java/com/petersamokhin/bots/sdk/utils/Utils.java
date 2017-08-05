package com.petersamokhin.bots.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utils class with useful methods
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Analog of JS setTimeout
     */
    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (InterruptedException e) {
                System.err.println(e.toString());
            }
        }).start();
    }

    /**
     * Convert to URL params query
     *
     * @param arr Map or JSONObject
     * @return Query
     */
    public static String MapToURLParamsQuery(Object arr) {

        String answer = null;

        if (arr instanceof JSONObject) {
            JSONObject j = (JSONObject) arr;
            answer = j.toMap().entrySet().stream()
                    .map(p -> p.getKey() + "=" + toQueryString(p.getValue()))
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");
        } else if (arr instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) arr;
            answer = map.entrySet().stream()
                    .map(p -> p.getKey() + "=" + toQueryString(p.getValue()))
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");
        }

        return answer;
    }

    /**
     * Arrays and lists to comma separated string
     */
    public static String toQueryString(Object o) {

        if (o instanceof List) {
            return toQueryString(((List) o).toArray());
        }

        if (o instanceof String[]) {
            return String.join(",", (String[]) o);
        }

        if (o instanceof Object[]) {
            Object[] a = (Object[]) o;
            String[] s = new String[a.length];
            for (int i = 0; i < a.length; i++)
                s[i] = String.valueOf(a[i]);
            return toQueryString(s);
        }

        if (o instanceof JSONArray) {
            return ((JSONArray) o).join(",");
        }

        return o.toString();
    }

    /**
     * Params from varargs: [&quot;user_id&quot;, 62802565] to &quot;&amp;user_id=62802565&quot;
     * @param params Params
     * @return String
     */
    public static String paramsToString(Object... params) {
        if (params.length % 2 != 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < params.length; i++) {
                if (i % 2 == 0) {
                    sb.append("&").append(String.valueOf(params[i])).append("=");
                } else {
                    sb.append(String.valueOf(params[i]));
                }
            }

            return sb.toString();
        }
    }

    /**
     * Convert params query to map
     */
    public static JSONObject explodeQuery(String query) {

        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        Map<String, Object> map = new HashMap<>();

        String[] arr = query.split("&");

        for (String param : arr) {
            String[] tmp_arr = param.split("=");
            String key = tmp_arr[0], value = tmp_arr[1];

            if (tmp_arr[1].contains(",")) {
                map.put(key, new JSONArray(Arrays.asList(value.split(","))));
            } else {
                map.put(key, value);
            }
        }

        return new JSONObject(map);
    }

    /**
     * Calculcating size of file in url
     * @param url URL
     * @param dim Bits, KBits or MBits
     * @return Size
     */
    public static int sizeOfFile(String url, String dim) {

        try {
            URL URL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
            conn.getInputStream();

            double sizeInBits = conn.getContentLength();

            switch (dim) {
                case "bits": {
                    return (int) sizeInBits;
                }
                case "kbits": {
                    return (int) Math.round(sizeInBits / 1024.0);
                }
                case "mbits": {
                    return (int) Math.round(sizeInBits / (1024.0 * 1024.0));
                }
            }
        } catch (IOException ignored) {
            LOG.error("IOException when calculating size of file {} , error: {}", url, ignored.toString());
        }

        return 0;
    }
}
