package com.petersamokhin.bots.sdk.utils;

import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Web work: get and post requests
 */
public final class Connection {

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

            if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

            ResponseBody rb = response.body();
            responseBody = rb != null ? rb.string() : "{}";
        } catch (IOException ignored) {
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date()) + "] " + "[Connection.java:43] IOException when processing GET request: " + ignored.toString());

        }

        client.connectionPool().evictAll();

        responseBody = (responseBody == null || responseBody.length() < 2) ? "P{" : responseBody;

        return new JSONObject(responseBody);
    }

    /**
     * Make POST-request
     *
     * @param url  URL
     * @param Body Request body
     * @return String response body
     */
    public static JSONObject postRequestResponse(String url, String Body) {

        MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON_TYPE, Body))
                .build();

        String answer = "{}";
        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

            ResponseBody rb = response.body();
            answer = rb != null ? rb.string() : "{}";

        } catch (IOException ignored) {
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date()) + "] " + "[Connection.java:77] IOException when processing POST request: " + ignored.toString());
        }

        client.connectionPool().evictAll();

        return new JSONObject(answer);
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

                System.out.println("[" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date()) + "] " + "[Connection.java:120] I/O exception when processing request (upload file in multipart), fieldName is " + fieldName + " and filepath is " + file.getAbsolutePath());
            }

            client.connectionPool().evictAll();

            return answerOfUpload;
        } else {
            System.out.println("[" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date()) + "] " + "[Connection.java:127] File not exists: " + file.getAbsolutePath());
        }

        return "{}";
    }
}