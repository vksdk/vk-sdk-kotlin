package com.petersamokhin.bots.sdk.longpoll;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.longpoll.responses.GetLongPollServerResponse;
import com.petersamokhin.bots.sdk.objects.Message;
import com.petersamokhin.bots.sdk.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main class for work with VK longpoll server
 * More: <a href="https://vk.com/dev/using_longpoll">link</a>
 */
public class LongPoll {

    private String access_token = null;

    private String server = null;
    private String key = null;
    private Integer ts = null;
    private Integer pts = null;

    private Integer wait = 25;

    /**
     * 2 + 32 + 128
     * attachments + pts + random_id
     */
    private Integer mode = 162;

    private Integer version = 2;
    private Integer need_pts = 1;
    private Double API = 5.67;

    private boolean on = false;

    /**
     * Simple default constructor that requires only access token
     *
     * @param access_token your access token key, more: <a href="https://vk.com/dev/access_token">link</a>
     */
    public LongPoll(String access_token) {

        on = true;
        setData(access_token, null, null, null, null, null);
    }

    /**
     * Custom constructor
     *
     * @param access_token your access token key, more: <a href="https://vk.com/dev/access_token">link</a>
     * @param need_pts     more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param version      more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param API          more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param wait         more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param mode         more: <a href="https://vk.com/dev/using_longpoll">link</a>
     */
    public LongPoll(String access_token, Integer need_pts, Integer version, Double API, Integer wait, Integer mode) {

        on = true;
        setData(access_token, need_pts, version, API, wait, mode);
    }

    /**
     * If you need to set new longpoll server, or restart listening
     * off old before.
     */
    public void off() {
        on = false;
    }

    /**
     * Setting all necessary parameters
     *
     * @param access_token Access token of user or group
     * @param need_pts     param, info: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param version      param, info: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param API          param, info: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param wait         param, info: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param mode         param, info: <a href="https://vk.com/dev/using_longpoll">link</a>
     */
    private void setData(String access_token, Integer need_pts, Integer version, Double API, Integer wait, Integer mode) {

        this.access_token = (access_token != null && access_token.length() > 5) ? access_token : this.access_token;

        this.need_pts = need_pts == null ? this.need_pts : need_pts;
        this.version = version == null ? this.version : version;
        this.API = API == null ? this.API : API;
        this.wait = wait == null ? this.wait : wait;
        this.mode = mode == null ? this.mode : mode;

        GetLongPollServerResponse serverResponse = getLongPollServer(this.access_token);
        this.server = serverResponse.getServer();
        this.key = serverResponse.getKey();
        this.ts = serverResponse.getTs();
        this.pts = serverResponse.getPts();
    }

    /**
     * First getting of longpoll server params
     *
     * @param access_token Access token
     * @return LongPoll params
     */
    private GetLongPollServerResponse getLongPollServer(String access_token) {

        String query = "https://api.vk.com/method/messages.getLongPollServer?need_pts=" + need_pts + "&lp_version=" + version + "&access_token=" + access_token + "&v=" + API;

        JSONObject response = Connection.getRequestResponse(query);
        JSONObject data = response.getJSONObject("response");

        return new GetLongPollServerResponse(
                data.getString("key"),
                data.getString("server"),
                data.getInt("ts"),
                data.getInt("pts")
        );
    }


    /**
     * Listening to events from VK longpoll server
     * and call callbacks on events.
     * You can override only necessary methods in callback to get necessary events.
     */
    public void listen(Callback callback) {

        String query;

        while (on) {

            query = "https://" + server + "?act=a_check&key=" + key + "&ts=" + ts + "&wait=" + wait + "&mode=" + mode + "&version=" + version + "&msgs_limit=100000";

            JSONObject response = Connection.getRequestResponse(query);

            if (response.has("failed")) {

                int code = response.getInt("failed");

                System.out.println("[RESPONSE IS FAILED!] with code: " + code);

                switch (code) {

                    default: {

                        Integer new_ts = response.has("ts") ? response.getInt("ts") : ts;


                        setData(null, null, null, null, null, null);

                        break;
                    }

                    case 4: {

                        version = response.getInt("max_version");

                        break;
                    }
                }
            } else {

                if (response.has("ts") && response.has("updates")) {

                    Integer new_ts = response.getInt("ts");

                    Integer new_pts = response.has("pts") ? response.getInt("pts") : pts;

                    JSONArray updates = response.getJSONArray("updates");

                    for (Object currentUpdateObject : updates) {

                        JSONArray currentUpdate = (JSONArray) currentUpdateObject;

                        int updateType = currentUpdate.getInt(0);

                        switch (updateType) {

                            // Handling new message
                            case 4: {

                                if ((currentUpdate.getInt(2) & 2) == 0) {

                                    Message message = new Message(
                                            access_token,
                                            currentUpdate.getInt(1),
                                            currentUpdate.getInt(2),
                                            currentUpdate.getInt(3),
                                            currentUpdate.getInt(4),
                                            currentUpdate.getString(5),
                                            (currentUpdate.length() > 6 ? (currentUpdate.get(6).toString().startsWith("{") ? new JSONObject(currentUpdate.get(6).toString()) : null) : null),
                                            currentUpdate.length() > 7 ? currentUpdate.getInt(7) : null
                                    );


                                    switch (message.messageType()) {

                                        case "voiceMessage": {
                                            callback.onVoiceMessage(message);
                                            break;
                                        }

                                        case "stickerMessage": {
                                            callback.onStickerMessage(message);
                                            break;
                                        }

                                        case "gifMessage": {
                                            callback.onGifMessage(message);
                                            break;
                                        }

                                        case "audioMessage": {
                                            callback.onAudioMessage(message);
                                            break;
                                        }

                                        case "videoMessage": {
                                            callback.onVideoMessage(message);
                                            break;
                                        }

                                        case "docMessage": {
                                            callback.onDocMessage(message);
                                            break;
                                        }

                                        case "wallMessage": {
                                            callback.onWallMessage(message);
                                            break;
                                        }

                                        case "photoMessage": {
                                            callback.onPhotoMessage(message);
                                            break;
                                        }

                                        case "linkMessage": {
                                            callback.onLinkMessage(message);
                                            break;
                                        }

                                        case "simpleTextMessage": {
                                            callback.onSimpleTextMessage(message);
                                            break;
                                        }
                                    }

                                    callback.onMessage(message);
                                }

                                break;
                            }

                            case 61: {

                                callback.onTyping(currentUpdate.getInt(1));
                                break;
                            }
                        }
                    }

                    ts = new_ts;

                } else {
                    System.out.println("Bad response from longpoll: no ts or updates array: " + response.toString());
                }

            }

        }
    }
}
