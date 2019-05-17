package com.petersamokhin.bots.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
     *
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
     *
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

    public static String guessFileNameByContentType(String contentType) {

        contentType = contentType
                .replace("mpeg", "mp3")
                .replace("svg+xml", "svg")
                .replace("javascript", "js")
                .replace("plain", "txt")
                .replace("markdown", "md");

        String mainType = contentType.substring(0, contentType.indexOf('/'));
        if (contentType.contains(" ")) {
            contentType = contentType.substring(0, contentType.indexOf(' '));
        }
        String subType = contentType.substring(contentType.lastIndexOf('/') + 1);
        if (subType.contains("-") || subType.contains(".") || subType.contains("+"))
            subType = "unknown";

        return mainType + '.' + subType;
    }

    /*
     * Methods from commons-lang library of Apache
     * Added to not use the library for several methods
     */

    public static byte[] toByteArray(URL url) throws IOException {
        URLConnection conn = url.openConnection();

        byte[] var2;
        try {
            var2 = toByteArray(conn);
        } finally {
            close(conn);
        }

        return var2;
    }

    public static byte[] toByteArray(URLConnection urlConn) throws IOException {
        InputStream inputStream = urlConn.getInputStream();

        byte[] var2;
        try {
            var2 = toByteArray(inputStream);
        } finally {
            inputStream.close();
        }

        return var2;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy((InputStream) input, (OutputStream) output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for (count = 0L; (n = input.read(buffer)) != -1; count += (long) n) {
            output.write(buffer, 0, n);
        }

        return count;
    }

    public static void close(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout) throws IOException {
        URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        copyInputStreamToFile(connection.getInputStream(), destination);
    }

    public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
        try {
            copyToFile(source, destination);
        } finally {
            closeQuietly(source);
        }
    }

    public static void copyToFile(InputStream source, File destination) throws IOException {
        FileOutputStream output = openOutputStream(destination);

        try {
            copy(source, output);
            output.close();
        } finally {
            closeQuietly(output);
        }
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }

            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }

        return new FileOutputStream(file, append);
    }

    public static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable) output);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException var2) {
        }

    }
}
