package com.petersamokhin.bots.sdk.utils.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * OkHttp sending byte[] array not works with vk
 * Using this.
 *
 * Stealed from <a href="https://stackoverflow.com/a/35925747/7519767">there</a>.
 */
public class MultipartUtility {

    private static final Logger LOG = LoggerFactory.getLogger(MultipartUtility.class);

    private HttpURLConnection httpConn;
    private DataOutputStream request;
    private final String boundary = "*****";
    private final String crlf = "\r\n";
    private final String twoHyphens = "--";

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     */
    public MultipartUtility(String requestURL) {

        try {
            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);

            request = new DataOutputStream(httpConn.getOutputStream());
        } catch (IOException ignored) {
            LOG.error("Error when trying to connect to the url for uploading file in multipart/form-data, url: {}", requestURL);
        }
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name of field in body of POST-request
     * @param uploadFile a File to be uploaded
     */
    public void addFilePart(String fieldName, File uploadFile) {
        try {
            String fileName = uploadFile.getName();
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    fieldName + "\";filename=\"" +
                    fileName + "\"" + this.crlf);
            request.writeBytes(this.crlf);

            byte[] bytes = Files.readAllBytes(uploadFile.toPath());
            request.write(bytes);
        } catch (IOException ignored) {
            LOG.error("Error when adding file as multipart/form-data field. Field name is {} and file path is {}.", fieldName, uploadFile.getAbsolutePath());
        }
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name of field in body of POST-requestx
     * @param bytes     an array of bytes to be uploaded
     */
    public void addBytesPart(String fieldName, String fileName, byte[] bytes) {
        try {
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\";filename=\"" + fileName + "\"" + this.crlf);
            request.writeBytes(this.crlf);

            request.write(bytes);
        } catch (IOException ignored) {
            LOG.error("Error when adding bytes as multipart/form-data field. Field name is {} and file name is {}.", fieldName, fileName);
        }
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     */
    public String finish() {

        String response = "error";

        try {
            request.writeBytes(this.crlf);
            request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

            request.flush();
            request.close();

            int status = httpConn.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {
                InputStream responseStream = new BufferedInputStream(httpConn.getInputStream());

                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString();
                httpConn.disconnect();
            } else {
                LOG.error("Some error occured when receiving answer of sending file or bytes in multipart/form-date format: http status is {} and url is {}.", status, httpConn.getURL());
            }
        } catch (IOException ignored) {
            LOG.error("Some error occured when receiving answer of sending file or bytes in multipart/form-date format: {}", ignored.toString());
        }

        return response;
    }
}