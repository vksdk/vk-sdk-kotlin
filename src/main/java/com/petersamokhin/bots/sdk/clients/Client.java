package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.callbacks.commands.OnCommandCallback;
import com.petersamokhin.bots.sdk.callbacks.messages.*;
import com.petersamokhin.bots.sdk.longpoll.LongPoll;
import com.petersamokhin.bots.sdk.utils.vkapi.API;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main client class, that contains all necessary methods and fields
 * for base work with VK and longpoll server
 */
public abstract class Client {

    private String accessToken;
    private Integer id;
    private static API api;
    private LongPoll longPoll = null;

    public CopyOnWriteArrayList<Command> commands = new CopyOnWriteArrayList<>();

    /**
     * Default constructor
     *
     * @param access_token Access token key
     */
    Client(String access_token) {

        this.accessToken = access_token;
        this.longPoll = new LongPoll(this);

        api = new API(this);
    }

    /**
     * Default constructor
     *
     * @param id           User or group id
     * @param access_token Access token key
     */
    Client(Integer id, String access_token) {

        this.id = id;
        this.accessToken = access_token;
        this.longPoll = new LongPoll(this);

        api = new API(this);
    }

    /**
     * If need to set not default longpoll
     */
    public void setLongPoll(LongPoll LP) {

        this.longPoll.off();
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

    /**
     * If the client need to start typing
     * after receiving message
     * and until client's message is sent
     */
    public void enableTyping(boolean enable) {
        this.longPoll().enableTyping(enable);
    }

    /* Messages */

    public void onEveryMessage(OnEveryMessageCallback callback) {
        this.longPoll().registerCallback("OnEveryMessageCallback", callback);
    }

    public void onMessageWithFwds(OnMessageWithFwdsCallback callback) {
        this.longPoll().registerCallback("OnMessageWithFwdsCallback", callback);
    }

    public void onAudioMessage(OnAudioMessageCallback callback) {
        this.longPoll().registerCallback("OnAudioMessageCallback", callback);
    }

    public void onDocMessage(OnDocMessageCallback callback) {
        this.longPoll().registerCallback("OnDocMessageCallback", callback);
    }

    public void onGifMessage(OnGifMessageCallback callback) {
        this.longPoll().registerCallback("OnGifMessageCallback", callback);
    }

    public void onLinkMessage(OnLinkMessageCallback callback) {
        this.longPoll().registerCallback("OnLinkMessageCallback", callback);
    }

    public void onMessage(OnMessageCallback callback) {
        this.longPoll().registerCallback("OnMessageCallback", callback);
    }

    public void onPhotoMessage(OnPhotoMessageCallback callback) {
        this.longPoll().registerCallback("OnPhotoMessageCallback", callback);
    }

    public void onSimpleTextMessage(OnSimpleTextMessageCallback callback) {
        this.longPoll().registerCallback("OnSimpleTextMessageCallback", callback);
    }

    public void onStickerMessage(OnStickerMessageCallback callback) {
        this.longPoll().registerCallback("OnStickerMessageCallback", callback);
    }

    public void onTyping(OnTypingCallback callback) {
        this.longPoll().registerCallback("OnTypingCallback", callback);
    }

    public void onVideoMessage(OnVideoMessageCallback callback) {
        this.longPoll().registerCallback("OnVideoMessageCallback", callback);
    }

    public void onVoiceMessage(OnVoiceMessageCallback callback) {
        this.longPoll().registerCallback("OnVoiceMessageCallback", callback);
    }

    public void onWallMessage(OnWallMessageCallback callback) {
        this.longPoll().registerCallback("OnWallMessageCallback", callback);
    }

    /* Commands */

    public void onCommand(Object command, OnCommandCallback callback) {
        this.commands.add(new Command(command, callback));
    }

    public void onCommand(OnCommandCallback callback, Object... commands) {
        this.commands.add(new Command(commands, callback));
    }

    public void onCommand(Object[] commands, OnCommandCallback callback) {
        this.commands.add(new Command(commands, callback));
    }

    public void onCommand(List<?> list, OnCommandCallback callback) {
        this.commands.add(new Command(list, callback));
    }

    /**
     * Command object
     */
    public class Command {
        private Object[] commands;
        private OnCommandCallback callback;

        public Command(Object[] commands, OnCommandCallback callback) {
            this.commands = commands;
            this.callback = callback;
        }

        public Command(Object command, OnCommandCallback callback) {
            this.commands = new Object[]{command};
            this.callback = callback;
        }

        public Command(List<?> command, OnCommandCallback callback) {
            this.commands = command.toArray();
            this.callback = callback;
        }

        public Object[] getCommands() {
            return commands;
        }

        public OnCommandCallback getCallback() {
            return callback;
        }
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
