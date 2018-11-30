package com.petersamokhin.bots.sdk.utils.vkapi;

/**
 * Settings for interacting with Callback API
 */
public class CallbackApiSettings {

    private String host, path;
    private int port;
    private boolean autoAnswer;

    public CallbackApiSettings(String host, int port, String path, boolean autoAnswer, boolean autoSet) {
        this.host = host;
        this.path = path;
        this.port = port;
        this.autoAnswer = autoAnswer;
        CallbackApiHandler.autoSetEvents = autoSet;
    }

    String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    int getPort() {
        return port;
    }

    boolean isAutoAnswer() {
        return autoAnswer;
    }
}
