package com.petersamokhin.bots.sdk.utils;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Web work: get and post requests
 */
public final class Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();

    /**
     * Make GET-request
     *
     * @param url URL
     * @return String response body
     */
    public static JSONObject getRequestResponse(String url) {

        Request request = new Request.Builder()
                .url(url)
                .build();

        String responseBody = "";

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) LOG.error("Request: {} , response is not successful: {}", url, response);

            ResponseBody rb = response.body();
            responseBody = rb != null ? rb.string() : "{}";
        } catch (IOException ignored) {
            LOG.error("IOException when processing GET request {} , and error is {}", url, ignored.toString());
        }

        client.connectionPool().evictAll();

        responseBody = (responseBody == null) ? "{}" : responseBody;

        JSONObject answer = new JSONObject();

        try {
            answer = new JSONObject(responseBody);
        } catch (JSONException ignored) {
            LOG.error("Query: {}, Bad response: {}, error: {}", url, responseBody, ignored.toString());
        }

        return answer;
    }

    /**
     * Make POST-request
     *
     * @param url  URL
     * @param body Request body
     * @return String response body
     */
    public static JSONObject postRequestResponse(String url, String body) {

        MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON_TYPE, body))
                .build();

        String responseBody = "{}";
        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) LOG.error("Request: {} , response is not successful: {}", url, response);

            ResponseBody rb = response.body();
            responseBody = rb != null ? rb.string() : "{}";

        } catch (IOException ignored) {
            LOG.error("IOException when processing POST request {} , body is {} and error is {}", url, body, ignored.toString());
        }

        client.connectionPool().evictAll();

        JSONObject answer = new JSONObject();

        try {
            answer = new JSONObject(responseBody);
        } catch (JSONException ignored) {
            LOG.error("Bad response: {}, error: {}", responseBody, ignored.toString());
        }

        return answer;
    }

    /**
     * Loading of file to VK server
     * @param uploadUrl upload url
     * @param fieldName field name
     * @param mediaType MediaType
     * @param file file
     * @return server response
     */
    public static String getFileUploadAnswerOfVK(String uploadUrl, String fieldName, MediaType mediaType, File file) {

        if (file.exists()) {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(fieldName, file.getName(), RequestBody.create(mediaType, file))
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            String answerOfUpload = "{}";
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                answerOfUpload = responseBody != null ? responseBody.string() : "";

            } catch (IOException ignored) {
                LOG.error("IOException when processing request (upload file in multipart), fieldName is {} and filepath is {}", fieldName, file.getAbsolutePath());
            }

            client.connectionPool().evictAll();

            return answerOfUpload;
        } else {
            LOG.error("File is not exists: {}", file.getAbsolutePath());
        }

        return "{}";
    }
}