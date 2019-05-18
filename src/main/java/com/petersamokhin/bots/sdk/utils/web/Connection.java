package com.petersamokhin.bots.sdk.utils.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Web work: get and post requests
 */
public final class Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    /**
     * Make GET-request
     *
     * @param urlString URL
     * @return String response body
     */
    public static String getRequestResponse(String urlString) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Charset", "utf-8");

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder responseSb = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseSb.append(inputLine);
            }
            in.close();

            String response = responseSb.toString();

            if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                LOG.error("Response of 'get' request to url {} is not succesful, code is {} and response is {}", url, responseCode, response);
            }

            return response;
        } catch (IOException ignored) {
            LOG.error("IOException occured when processing request to {}, error is {}", urlString, ignored.toString());
            return "{}";
        }
    }

    /**
     * Make POST-request
     *
     * @param urlString URL
     * @param body      Request body
     * @return String response body
     */
    public static String postRequestResponse(String urlString, String body) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "VKAndroidApp/4.9-1118 (Android 5.1; SDK 22; armeabi-v7a; UMI IRON; ru)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();

            os.write(body.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder responseSb = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseSb.append(inputLine);
            }
            in.close();

            String response = responseSb.toString();

            if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                LOG.error("Response of 'post' request to url {} with body {} is not succesful, code is {} and response is {}", url, body, responseCode);
            }

            return response;
        } catch (IOException ignored) {
            LOG.error("IOException occured when processing request to {} with body {}, error is {}", urlString, body, ignored.toString());
            return "{}";
        }
    }
}