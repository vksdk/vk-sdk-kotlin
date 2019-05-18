package com.petersamokhin.bots.sdk.objects;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Client;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.utils.Utils;
import com.petersamokhin.bots.sdk.utils.web.MultipartUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by PeterSamokhin on 29/09/2017 02:49
 */
public class Chat {

    public static final Integer CHAT_PREFIX = 2000000000;

    private static final Logger LOG = LoggerFactory.getLogger(Chat.class);

    private Client client;
    private Integer chatId;

    public Chat(Client client, Integer chatId) {
        this.client = client;
        this.chatId = chatId;

        if (client instanceof Group) {
            LOG.error("Now groups can't work with chats, sorry.");
        }
    }

    public void addUser(Integer userId, Callback<Object>... callbacks) {
        this.client.api().call("messages.addChatUser", "{chat_id:" + (chatId - CHAT_PREFIX) + ",user_id:" + userId + "}", response -> {
            if (callbacks.length > 0) {
                callbacks[0].onResult(response);
            }
        });
    }

    public void kickUser(Integer userId, Callback<Object>... callbacks) {
        this.client.api().call("messages.removeChatUser", "{chat_id:" + (chatId - CHAT_PREFIX) + ",user_id:" + userId + "}", response -> {
            if (callbacks.length > 0) {
                callbacks[0].onResult(response);
            }
        });
    }

    public void deletePhoto(Callback<Object>... callbacks) {
        this.client.api().call("messages.deleteChatPhoto", "{chat_id:" + (chatId - CHAT_PREFIX) + "}", response -> {
            if (callbacks.length > 0) {
                callbacks[0].onResult(response);
            }
        });
    }

    public void editTitle(String newTitle, Callback<Object>... callbacks) {
        this.client.api().call("messages.editChat", "{chat_id:" + (chatId - CHAT_PREFIX) + ",title:" + newTitle + "}", response -> {
            if (callbacks.length > 0) {
                callbacks[0].onResult(response);
            }
        });
    }

    public void getUsers(String fields, Callback<JSONArray> callback) {
        this.client.api().call("messages.getChatUsers", "{chat_id:" + (chatId - CHAT_PREFIX) + ",fields:" + fields + "}", response -> {
            callback.onResult(new JSONArray(response.toString()));
        });
    }

    public void setPhoto(String photo, Callback<Object> callback) {

        String type = null;
        File photoFile = new File(photo);
        if (photoFile.exists()) {
            type = "fromFile";
        }

        URL photoUrl = null;
        if (type == null) {
            try {
                photoUrl = new URL(photo);
                type = "fromUrl";
            } catch (MalformedURLException ignored) {
                LOG.error("Error when trying add photo to message: file not found, or url is bad. Your param: {}", photo);
                callback.onResult("false");
                return;
            }
        }

        byte[] photoBytes;
        switch (type) {

            case "fromFile": {
                try {
                    photoBytes = Files.readAllBytes(Paths.get(photoFile.toURI()));
                } catch (IOException ignored) {
                    LOG.error("Error when reading file {}", photoFile.getAbsolutePath());
                    callback.onResult("false");
                    return;
                }
                break;
            }

            case "fromUrl": {
                try {
                    photoBytes = Utils.toByteArray(photoUrl);
                } catch (IOException ignored) {
                    LOG.error("Error {} occured when reading URL {}", ignored.toString(), photo);
                    callback.onResult("false");
                    return;
                }
                break;
            }

            default: {
                LOG.error("Bad 'photo' string: path to file, URL or already uploaded 'photo()_()' was expected.");
                callback.onResult("false");
                return;
            }
        }

        if (photoBytes != null) {

            JSONObject params_getMessagesUploadServer = new JSONObject().put("chat_id", chatId);
            client.api().call("photos.getChatUploadServer", params_getMessagesUploadServer, response -> {

                if (response.toString().equalsIgnoreCase("false")) {
                    LOG.error("Can't get messages upload server, aborting. Photo wont be attached to message.");
                    callback.onResult(false);
                    return;
                }

                String uploadUrl = new JSONObject(response.toString()).getString("upload_url");

                MultipartUtility multipartUtility = new MultipartUtility(uploadUrl);
                multipartUtility.addBytesPart("file", "photo.png", photoBytes);

                String response_uploadFileString = multipartUtility.finish();

                if (response_uploadFileString.length() < 2 || response_uploadFileString.contains("error") || !response_uploadFileString.contains("response")) {
                    LOG.error("Photo wan't uploaded: {}", response_uploadFileString);
                    callback.onResult("false");
                    return;
                }

                JSONObject getPhotoStringResponse;

                try {
                    getPhotoStringResponse = new JSONObject(response_uploadFileString);
                } catch (JSONException ignored) {
                    LOG.error("Bad response of uploading photo: {}", response_uploadFileString);
                    callback.onResult("false");
                    return;
                }

                if (!getPhotoStringResponse.has("response")) {
                    LOG.error("Bad response of uploading chat photo, no 'response' param: {}", getPhotoStringResponse.toString());
                    callback.onResult("false");
                    return;
                }

                String responseParam = getPhotoStringResponse.getString("response");

                JSONObject params_photosSaveMessagesPhoto = new JSONObject().put("file", responseParam);

                client.api().call("messages.setChatPhoto", params_photosSaveMessagesPhoto, response1 -> {


                    if (response1.toString().equalsIgnoreCase("false")) {
                        LOG.error("Error when saving uploaded photo: response is 'false', see execution errors.");
                        callback.onResult("false");
                        return;
                    }

                    callback.onResult(response1);
                });
            });
        }
    }

    public void getChatInfo(Callback<JSONObject> callback) {

        client.api().call("messages.getChat", "{chat_id:" + chatId + "}", response ->
            callback.onResult((JSONObject) response)
        );
    }
}
