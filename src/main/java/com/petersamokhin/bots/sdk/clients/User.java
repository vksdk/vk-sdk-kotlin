package com.petersamokhin.bots.sdk.clients;

/**
 * User client, that contains important methods to work with users
 *
 * Not need now to put methods there: use API.call
 */
public class User extends Client {

    /**
     * Default constructor
     * @param id User or group id
     * @param access_token Access token key
     */
    public User(Integer id, String access_token) {
        super(id, access_token);
    }

    /**
     * Default constructor
     * @param access_token Access token key
     */
    public User(String access_token) {
        super(access_token);
    }
}