package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.longpoll.LongPoll;
import com.petersamokhin.bots.sdk.utils.API;

/**
 * Main client class, that contains all necessary methods and fields
 * for base work with VK and longpoll server
 */
public abstract class Client {

    private String accessToken;
    private Integer id;
    private API api;
    private LongPoll longPoll = null;

    /**
     * Default constructor
     * @param id User or group id
     * @param access_token Access token key
     */
    Client(Integer id, String access_token) {

        this.id = id;
        this.accessToken = access_token;
        this.longPoll = new LongPoll(access_token);

        this.api = new API(this);
    }

    /**
     * If need to set not default longpoll
     */
    public void setLongPoll(LongPoll LP) {

        this.longPoll = LP;
    }

    /**
     * Get longpoll of current client
     */
    public LongPoll longPoll() {
        return longPoll;
    }

    /**
     * Get API for making requests
     */
    public API api() {
        return api;
    }

    // Getters and setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
