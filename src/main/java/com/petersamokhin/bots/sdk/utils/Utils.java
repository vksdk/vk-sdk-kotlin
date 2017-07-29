package com.petersamokhin.bots.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by PeterSamokhin on 27/07/2017 22:44
 */
public class Utils {

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
     * Params from varargs: "user_id", 62802565 -> "&user_id=62802565"
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
            System.out.println("[Utils.java:126] IOException: " + ignored.toString());
        }

        return 0;
    }
}
