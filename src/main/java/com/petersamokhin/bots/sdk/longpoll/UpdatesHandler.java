package com.petersamokhin.bots.sdk.longpoll;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.callbacks.messages.*;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.objects.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * Class for handling all updates in other thread
 */
public class UpdatesHandler extends Thread {

    private volatile Queue queue = new Queue();

    volatile boolean sendTyping = false;

    /**
     * Map with callbacks
     */
    private Map<String, Callback> callbacks = new HashMap<>();

    /**
     * Client with access_token
     */
    private Client client;

    UpdatesHandler(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            if (this.queue.updates.size() > 0) {
                handleCurrentUpdate(this.queue.shift());
            }
        }
    }

    /**
     * Handle the array of updates
     */
    void handle(JSONArray updates) {
        this.queue.putAll(updates);
    }

    /**
     * Handle one event from longpoll server
     *
     * @param currentUpdate answer of lps
     */
    private void handleCurrentUpdate(JSONArray currentUpdate) {

        int updateType = currentUpdate.getInt(0);

        switch (updateType) {

            // Handling new message
            case 4: {

                int messageFlags = currentUpdate.getInt(2);
                boolean messageIsAlreadyHandled = false;

                // Check if message is received
                if ((messageFlags & 2) == 0) {

                    Message message = new Message(
                            this.client.getAccessToken(),
                            currentUpdate.getInt(1),
                            currentUpdate.getInt(2),
                            currentUpdate.getInt(3),
                            currentUpdate.getInt(4),
                            currentUpdate.getString(5),
                            (currentUpdate.length() > 6 ? (currentUpdate.get(6).toString().startsWith("{") ? new JSONObject(currentUpdate.get(6).toString()) : null) : null),
                            currentUpdate.length() > 7 ? currentUpdate.getInt(7) : null
                    );

                    if (sendTyping) {
                        this.client.api().call("messages.setActivity", "{type:'typing',peer_id:" + message.authorId() + "}", response -> {
                        });
                    }

                    // check for commands
                    if (this.client.commands.size() > 0) {
                        messageIsAlreadyHandled = handleCommands(message);
                    }

                    if (message.hasFwds()) {
                        if (callbacks.containsKey("OnMessageWithFwdsCallback")) {
                            ((OnMessageWithFwdsCallback) callbacks.get("OnMessageWithFwdsCallback")).onMessage(message);
                            messageIsAlreadyHandled = true;
                        }
                    }

                    if (!messageIsAlreadyHandled) {
                        switch (message.messageType()) {

                            case "voiceMessage": {
                                if (callbacks.containsKey("OnVoiceMessageCallback")) {
                                    ((OnVoiceMessageCallback) callbacks.get("OnVoiceMessageCallback")).OnVoiceMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "stickerMessage": {
                                if (callbacks.containsKey("OnStickerMessageCallback")) {
                                    ((OnStickerMessageCallback) callbacks.get("OnStickerMessageCallback")).OnStickerMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "gifMessage": {
                                if (callbacks.containsKey("OnGifMessageCallback")) {
                                    ((OnGifMessageCallback) callbacks.get("OnGifMessageCallback")).OnGifMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "audioMessage": {
                                if (callbacks.containsKey("OnAudioMessageCallback")) {
                                    ((OnAudioMessageCallback) callbacks.get("OnAudioMessageCallback")).onAudioMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "videoMessage": {
                                if (callbacks.containsKey("OnVideoMessageCallback")) {
                                    ((OnVideoMessageCallback) callbacks.get("OnVideoMessageCallback")).onVideoMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "docMessage": {
                                if (callbacks.containsKey("OnDocMessageCallback")) {
                                    ((OnDocMessageCallback) callbacks.get("OnDocMessageCallback")).OnDocMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "wallMessage": {
                                if (callbacks.containsKey("OnWallMessageCallback")) {
                                    ((OnVoiceMessageCallback) callbacks.get("OnWallMessageCallback")).OnVoiceMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "photoMessage": {
                                if (callbacks.containsKey("OnPhotoMessageCallback")) {
                                    ((OnPhotoMessageCallback) callbacks.get("OnPhotoMessageCallback")).onPhotoMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "linkMessage": {
                                if (callbacks.containsKey("OnLinkMessageCallback")) {
                                    ((OnLinkMessageCallback) callbacks.get("OnLinkMessageCallback")).OnLinkMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }

                            case "simpleTextMessage": {
                                if (callbacks.containsKey("OnSimpleTextMessageCallback")) {
                                    ((OnSimpleTextMessageCallback) callbacks.get("OnSimpleTextMessageCallback")).OnSimpleTextMessage(message);
                                    messageIsAlreadyHandled = true;
                                }
                                break;
                            }
                        }
                    }

                    if (callbacks.containsKey("OnMessageCallback") && !messageIsAlreadyHandled) {
                        ((OnMessageCallback) callbacks.get("OnMessageCallback")).onMessage(message);
                    }

                    if (callbacks.containsKey("OnEveryMessageCallback")) {
                        ((OnEveryMessageCallback) callbacks.get("OnEveryMessageCallback")).OnEveryMessage(message);
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
     * Returns count of callbacks
     */
    int callbacksCount() {
        return this.callbacks.size();
    }

    /**
     * Returns count of commands
     */
    int commandsCount() {
        return this.client.commands.size();
    }

    /**
     * Handle message and call back if it contains any command
     *
     * @param message received message
     */
    private boolean handleCommands(Message message) {

        boolean is = false;

        for (Client.Commmand command : this.client.commands) {
            for (int i = 0; i < command.getCommands().length; i++) {
                if (containsIgnoreCase(message.getText(), command.getCommands()[i].toString())) {
                    command.getCallback().OnCommand(message);
                    is = true;
                }
            }
        }

        return is;
    }
}
