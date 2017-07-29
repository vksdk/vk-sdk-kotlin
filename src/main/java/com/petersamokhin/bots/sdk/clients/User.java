package com.petersamokhin.bots.sdk.clients;

/**
 * User client, that contains important methods to work with users
 *
 * Not need now to put methods there: use API.call
 */
public class User extends Client {

    public User(Integer id, String access_token) {
        super(id, access_token);
    }
}