package com.petersamokhin.bots.sdk.longpoll;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.callbacks.messages.*;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.longpoll.responses.GetLongPollServerResponse;
import com.petersamokhin.bots.sdk.objects.Message;
import com.petersamokhin.bots.sdk.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * com.petersamokhin.bots.sdk.Main class for work with VK longpoll server
 * More: <a href="https://vk.com/dev/using_longpoll">link</a>
 */
public class LongPoll {

    private static final Logger LOG = LoggerFactory.getLogger(LongPoll.class);

    private String access_token = null;
    private Client client = null;

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

    private volatile boolean on = false;


    /**
     * Map with callbacks
     */
    private volatile Map<String, Callback> callbacks = new HashMap<>();

    /**
     * Simple default constructor that requires only access token
     *
     * @param client client with your access token key, more: <a href="https://vk.com/dev/access_token">link</a>
     */
    public LongPoll(Client client) {

        this.client = client;
        setData(client.getAccessToken(), null, null, null, null, null);

        if (!on) {
            on = true;
            new Thread(this::startListening).start();
        }
    }

    /**
     * Custom constructor
     *
     * @param client client with your access token key, more: <a href="https://vk.com/dev/access_token">link</a>
     * @param need_pts     more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param version      more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param API          more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param wait         more: <a href="https://vk.com/dev/using_longpoll">link</a>
     * @param mode         more: <a href="https://vk.com/dev/using_longpoll">link</a>
     */
    public LongPoll(Client client, Integer need_pts, Integer version, Double API, Integer wait, Integer mode) {

        this.client = client;
        setData(client.getAccessToken(), need_pts, version, API, wait, mode);

        if (!on) {
            on = true;
            new Thread(this::startListening).start();
        }
    }

    /**
     * If you need to set new longpoll server, or restart listening
     * off old before.
     */
    public void off() {
        on = false;
    }

    /**
     * Add callback to the map
     *
     * @param name     Callback name
     * @param callback Callback
     */
    public void registerCallback(String name, Callback callback) {
        this.callbacks.put(name, callback);
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

        if (serverResponse == null) {
            LOG.error("Some error occured, can't start.");
            return;
        }

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

        if (!response.has("response")) {
            LOG.error("No response! Error: {}", response);
            return null;
        }

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
    private void startListening() {

        LOG.error("Started listening to events from VK LongPoll server...");

        while (on) {

            StringBuilder query = new StringBuilder();

            query.append("https://").append(server).append("?act=a_check&key=").append(key).append("&ts=").append(ts).append("&wait=").append(wait).append("&mode=").append(mode).append("&version=").append(version).append("&msgs_limit=100000");

            JSONObject response = Connection.getRequestResponse(query.toString());

            if (response.has("failed")) {

                int code = response.getInt("failed");

                LOG.error("Response of VK LongPoll fallen with error code {}", code);

                switch (code) {

                    default: {

                        ts = response.has("ts") ? response.getInt("ts") : ts;
                        setData(null, null, null, null, null, null);
                        break;
                    }

                    case 4: {

                        version = response.getInt("max_version");
                        break;
                    }
                }
            } else {

                if (this.client.commands.size() > 0 || callbacks.size() > 0) {

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

                                    int messageFlags = currentUpdate.getInt(2);

                                    // Check if message is received
                                    if ((messageFlags & 2) == 0) {

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

                                        // check for commands
                                        handleCommands(message);

                                        switch (message.messageType()) {

                                            case "voiceMessage": {
                                                if (callbacks.containsKey("OnVoiceMessageCallback")) {
                                                    ((OnVoiceMessageCallback) callbacks.get("OnVoiceMessageCallback")).OnVoiceMessage(message);
                                                }
                                                break;
                                            }

                                            case "stickerMessage": {
                                                if (callbacks.containsKey("OnStickerMessageCallback")) {
                                                    ((OnStickerMessageCallback) callbacks.get("OnStickerMessageCallback")).OnStickerMessage(message);
                                                }
                                                break;
                                            }

                                            case "gifMessage": {
                                                if (callbacks.containsKey("OnGifMessageCallback")) {
                                                    ((OnGifMessageCallback) callbacks.get("OnGifMessageCallback")).OnGifMessage(message);
                                                }
                                                break;
                                            }

                                            case "audioMessage": {
                                                if (callbacks.containsKey("OnAudioMessageCallback")) {
                                                    ((OnAudioMessageCallback) callbacks.get("OnAudioMessageCallback")).onAudioMessage(message);
                                                }
                                                break;
                                            }

                                            case "videoMessage": {
                                                if (callbacks.containsKey("OnVideoMessageCallback")) {
                                                    ((OnVideoMessageCallback) callbacks.get("OnVideoMessageCallback")).onVideoMessage(message);
                                                }
                                                break;
                                            }

                                            case "docMessage": {
                                                if (callbacks.containsKey("OnDocMessageCallback")) {
                                                    ((OnDocMessageCallback) callbacks.get("OnDocMessageCallback")).OnDocMessage(message);
                                                }
                                                break;
                                            }

                                            case "wallMessage": {
                                                if (callbacks.containsKey("OnWallMessageCallback")) {
                                                    ((OnVoiceMessageCallback) callbacks.get("OnWallMessageCallback")).OnVoiceMessage(message);
                                                }
                                                break;
                                            }

                                            case "photoMessage": {
                                                if (callbacks.containsKey("OnPhotoMessageCallback")) {
                                                    ((OnPhotoMessageCallback) callbacks.get("OnPhotoMessageCallback")).onPhotoMessage(message);
                                                }
                                                break;
                                            }

                                            case "linkMessage": {
                                                if (callbacks.containsKey("OnLinkMessageCallback")) {
                                                    ((OnLinkMessageCallback) callbacks.get("OnLinkMessageCallback")).OnLinkMessage(message);
                                                }
                                                break;
                                            }

                                            case "simpleTextMessage": {
                                                if (callbacks.containsKey("OnSimpleTextMessageCallback")) {
                                                    ((OnSimpleTextMessageCallback) callbacks.get("OnSimpleTextMessageCallback")).OnSimpleTextMessage(message);
                                                }
                                                break;
                                            }
                                        }

                                        if (callbacks.containsKey("OnMessageCallback")) {
                                            ((OnMessageCallback) callbacks.get("OnMessageCallback")).onMessage(message);
                                        }
                                    }
                                    break;
                                }

                                case 61: {

                                    if (callbacks.containsKey("OnTypingCallback")) {
                                        ((OnTypingCallback) callbacks.get("OnTypingCallback")).OnTyping(currentUpdate.getInt(1));
                                    }
                                    break;
                                }
                            }
                        }

                        ts = new_ts;

                    } else {
                        LOG.error("Bad response from VK LongPoll server: no `ts` or `updates` array: {}", response);
                    }
                }
            }
        }
    }

    /**
     * Handle message and call back if it contains any command
     *
     * @param message received message
     */
    private void handleCommands(Message message) {

        for (Client.Commmand command : this.client.commands) {
            for (int i = 0; i < command.getCommands().length; i++)
                if (containsIgnoreCase(message.getText(), command.getCommands()[i].toString()))
                    command.getCallback().OnCommand(message);
        }
    }
}