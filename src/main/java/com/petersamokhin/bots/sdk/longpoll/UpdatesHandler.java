package com.petersamokhin.bots.sdk.longpoll;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.objects.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.petersamokhin.bots.sdk.clients.Client.service;
import static com.petersamokhin.bots.sdk.clients.Client.sheduler;

/**
 * Class for handling all updates in other thread
 */
public class UpdatesHandler extends Thread {

    private volatile Queue queue = new Queue();

    volatile boolean sendTyping = false;

    /**
     * Map with callbacks
     */
    private ConcurrentHashMap<String, Callback> callbacks = new ConcurrentHashMap<>();

    /**
     * Client with access_token
     */
    private Client client;

    UpdatesHandler(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        sheduler.scheduleWithFixedDelay(this::handleCurrentUpdate, 0, 1, TimeUnit.MILLISECONDS);
    }

    /**
     * Handle the array of updates
     */
    void handle(JSONArray updates) {
        this.queue.putAll(updates);
    }

    /**
     * Handle one event from longpoll server
     */
    private void handleCurrentUpdate() {

        JSONArray currentUpdate;

        if (this.queue.updates.isEmpty()) {
            return;
        } else {
            currentUpdate = this.queue.shift();
        }

        int updateType = currentUpdate.getInt(0);

        switch (updateType) {

            // Handling new message
            case 4: {

                int messageFlags = currentUpdate.getInt(2);

                // check if message is received
                if ((messageFlags & 2) == 0) {
                    service.submit(() -> handleMessageUpdate(currentUpdate));
                }
                break;
            }

            // Handling update (user started typing)
            case 61: {
                handleTypingUpdate(currentUpdate);
                break;
            }
        }
    }

    /**
     * Handle new message
     */
    private void handleMessageUpdate(JSONArray updateObject) {

        boolean messageIsAlreadyHandled = false;

        Message message = new Message(
                this.client,
                updateObject.getInt(1),
                updateObject.getInt(2),
                updateObject.getInt(3),
                updateObject.getInt(4),
                updateObject.getString(5),
                (updateObject.length() > 6 ? (updateObject.get(6).toString().startsWith("{") ? new JSONObject(updateObject.get(6).toString()) : null) : null),
                updateObject.length() > 7 ? updateObject.getInt(7) : null
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
                callbacks.get("OnMessageWithFwdsCallback").onResult(message);
                messageIsAlreadyHandled = true;
            }
        }

        if (!messageIsAlreadyHandled) {
            switch (message.messageType()) {

                case "voiceMessage": {
                    if (callbacks.containsKey("OnVoiceMessageCallback")) {
                        callbacks.get("OnVoiceMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "stickerMessage": {
                    if (callbacks.containsKey("OnStickerMessageCallback")) {
                        callbacks.get("OnStickerMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "gifMessage": {
                    if (callbacks.containsKey("OnGifMessageCallback")) {
                        callbacks.get("OnGifMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "audioMessage": {
                    if (callbacks.containsKey("OnAudioMessageCallback")) {
                        callbacks.get("OnAudioMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "videoMessage": {
                    if (callbacks.containsKey("OnVideoMessageCallback")) {
                        callbacks.get("OnVideoMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "docMessage": {
                    if (callbacks.containsKey("OnDocMessageCallback")) {
                        callbacks.get("OnDocMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "wallMessage": {
                    if (callbacks.containsKey("OnWallMessageCallback")) {
                        callbacks.get("OnWallMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "photoMessage": {
                    if (callbacks.containsKey("OnPhotoMessageCallback")) {
                        callbacks.get("OnPhotoMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "linkMessage": {
                    if (callbacks.containsKey("OnLinkMessageCallback")) {
                        callbacks.get("OnLinkMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }

                case "simpleTextMessage": {
                    if (callbacks.containsKey("OnSimpleTextMessageCallback")) {
                        callbacks.get("OnSimpleTextMessageCallback").onResult(message);
                        messageIsAlreadyHandled = true;
                    }
                    break;
                }
            }
        }

        if (callbacks.containsKey("OnMessageCallback") && !messageIsAlreadyHandled) {
            callbacks.get("OnMessageCallback").onResult(message);
        }

        if (callbacks.containsKey("OnEveryMessageCallback")) {
            callbacks.get("OnEveryMessageCallback").onResult(message);
        }
    }

    /**
     * Handle dialog with typing user
     */
    private void handleTypingUpdate(JSONArray updateObject) {

        if (callbacks.containsKey("OnTypingCallback")) {
            callbacks.get("OnTypingCallback").onResult(updateObject.getInt(1));
        }
    }

    /**
     * Add callback to the map
     *
     * @param name     Callback name
     * @param callback Callback
     */
    void registerCallback(String name, Callback callback) {
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

        for (Client.Command command : this.client.commands) {
            for (int i = 0; i < command.getCommands().length; i++) {
                if (message.getText().toLowerCase().contains(command.getCommands()[i].toString().toLowerCase())) {
                    command.getCallback().onResult(message);
                    is = true;
                }
            }
        }

        return is;
    }
}
