package com.petersamokhin.bots.sdk.longpoll.responses;

/**
 * Deserialized object of VK response
 */
public class GetLongPollServerResponse {

    private String key;
    private String server;
    private Integer ts;
    private Integer pts;

    /**
     * VK response without pts (more: <a href="https://vk.com/dev/messages.getLongPollHistory">link</a>
     *
     * @param key    <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param server <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param ts     <a href="https://vk.com/dev/using_longpoll">link</a>
     */
    public GetLongPollServerResponse(String key, String server, Integer ts) {
        this.key = key;
        this.server = server;
        this.ts = ts;
    }

    /**
     * VK response with pts (more: <a href="https://vk.com/dev/messages.getLongPollHistory">link</a>
     *
     * @param key    <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param server <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param ts     <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param pts    <a href="https://vk.com/dev/using_longpoll">link</a>
     */
    public GetLongPollServerResponse(String key, String server, Integer ts, Integer pts) {
        this.key = key;
        this.server = server;
        this.ts = ts;
        this.pts = pts;
    }

    // Getters and Setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getTs() {
        return ts;
    }

    public void setTs(Integer ts) {
        this.ts = ts;
    }

    public Integer getPts() {
        return pts;
    }

    public void setPts(Integer pts) {
        this.pts = pts;
    }
}
