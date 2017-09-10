package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.utils.Utils;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiHandler;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiSettings;
import com.petersamokhin.bots.sdk.utils.web.MultipartUtility;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Group client, that contains important methods to work with groups
 */
public class Group extends Client {

    private CallbackApiHandler callbackApiHandler = null;

    private static final Logger LOG = LoggerFactory.getLogger(Group.class);

    /**
     * Default constructor
     *
     * @param id           User or group id
     * @param access_token Access token key
     */
    public Group(Integer id, String access_token) {
        super(id, access_token);
    }

    /**
     * Default constructor
     *
     * @param access_token Access token key
     */
    public Group(String access_token) {
        super(access_token);
    }

    /**
     * Upload group cover by file from url or from disk
     */
    public void uploadCover(String cover, Callback<Object> callback) {

        if (this.getId() == null || this.getId() == 0) {
            LOG.error("Please, provide group_id when initialising the client, because it's impossible to upload cover to group not knowing it id.");
            return;
        }

        byte[] bytes;

        File coverFile = new File(cover);
        if (coverFile.exists()) {
            try {
                bytes = Utils.toByteArray(coverFile.toURI().toURL());
            } catch (IOException ignored) {
                LOG.error("Cover file was exists, but IOException occured: {}", ignored.toString());
                return;
            }
        } else {
            URL coverUrl;
            try {
                coverUrl = new URL(cover);
                bytes = Utils.toByteArray(coverUrl);
            } catch (IOException ignored) {
                LOG.error("Bad string was proviced to uploadCocver method: path to file or url was expected, but got this: {}, error: {}", cover, ignored.toString());
                return;
            }
        }

        updateCoverByFile(bytes, callback);
    }

    /**
     * Updating cover by bytes (of file or url)
     *
     * @param bytes    bytes[]
     * @param callback response will return to callback
     */
    private void updateCoverByFile(byte[] bytes, Callback<Object>... callback) {

        JSONObject params_getUploadServer = new JSONObject()
                .put("group_id", getId())
                .put("crop_x", 0)
                .put("crop_y", 0)
                .put("crop_x2", 1590)
                .put("crop_y2", 400);

        api().call("photos.getOwnerCoverPhotoUploadServer", params_getUploadServer, response -> {

            String uploadUrl = new JSONObject(response.toString()).getString("upload_url");

            MultipartUtility multipartUtility = new MultipartUtility(uploadUrl);
            multipartUtility.addBytesPart("photo", "photo.png", bytes);
            String coverUploadedResponseString = multipartUtility.finish();

            coverUploadedResponseString = (coverUploadedResponseString != null && coverUploadedResponseString.length() > 2) ? coverUploadedResponseString : "{}";

            JSONObject coverUploadedResponse = new JSONObject(coverUploadedResponseString);

            if (coverUploadedResponse.has("hash") && coverUploadedResponse.has("photo")) {

                String hash_field = coverUploadedResponse.getString("hash");
                String photo_field = coverUploadedResponse.getString("photo");

                JSONObject params_saveCover = new JSONObject()
                        .put("hash", hash_field)
                        .put("photo", photo_field);

                boolean sync = true; // vk, please fix `execute` method!
                if (sync) {
                    JSONObject responseS = new JSONObject(api().callSync("photos.saveOwnerCoverPhoto", params_saveCover));
                    System.out.println("params is " + params_saveCover);
                    if (responseS.toString().length() < 10 || responseS.toString().contains("error")) {
                        LOG.error("Some error occured, cover not uploaded: {}", responseS);
                    }
                    if (callback.length > 0)
                        callback[0].onResult(responseS);
                } else {
                    api().call("photos.saveOwnerCoverPhoto", params_saveCover, response1 -> {

                        if (response1.toString().length() < 10 || response1.toString().contains("error")) {
                            LOG.error("Some error occured, cover not uploaded: {}", response1);
                        }
                        if (callback.length > 0) {
                            callback[0].onResult(response1);
                        }
                    });
                }
            } else {
                LOG.error("Error occured when uploading cover: no 'photo' or 'hash' param in response {}", coverUploadedResponse);
                if (callback.length > 0)
                    callback[0].onResult("false");
            }
        });
    }

    /**
     * Work with vk callback api
     *
     * @param path '/path' to listen
     */
    public Group callbackApi(String path) {
        if (callbackApiHandler == null) {
            callbackApiHandler = new CallbackApiHandler(path);
            callbackApiHandler.setGroup(this);
        }
        return this;
    }

    /**
     * Work with vk callback api
     *
     * @param settings (host, path, port etc)
     */
    public Group callbackApi(CallbackApiSettings settings) {
        if (callbackApiHandler == null) {
            callbackApiHandler = new CallbackApiHandler(settings);
            callbackApiHandler.setGroup(this);
        }
        return this;
    }

    /**
     * If need to set own port, host, etc
     *
     * @param settings Settings: host, port, path etc
     */
    public void setCallbackApiSettings(CallbackApiSettings settings) {
        callbackApiHandler = new CallbackApiHandler(settings);
        callbackApiHandler.setGroup(this);
    }

    /**
     * Default: will be listening for events from VK on port 80
     */
    public void setCallbackApiSettings(String path) {
        callbackApiHandler = new CallbackApiHandler(path);
        callbackApiHandler.setGroup(this);
    }

    /* Callback API */

    public void onAudioNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("audio_new", callback);
    }

    public void onBoardPostDelete(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("board_post_delete", callback);
    }

    public void onBoardPostEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("board_post_edit", callback);
    }

    public void onBoardPostNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("board_post_new", callback);
    }

    public void onBoardPostRestore(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("board_post_restore", callback);
    }

    public void onGroupChangePhoto(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("group_change_photo", callback);
    }

    public void onGroupChangeSettings(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("group_change_settings", callback);
    }

    public void onGroupJoin(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("group_join", callback);
    }

    public void onGroupLeave(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("group_leave", callback);
    }

    public void onGroupOfficersEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("group_officers_edit", callback);
    }

    public void onPollVoteNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("poll_vote_new", callback);
    }

    public void onMarketCommentDelete(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("market_comment_delete", callback);
    }

    public void onMarketCommentEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("market_comment_edit", callback);
    }

    public void onMarketCommentNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("market_comment_new", callback);
    }

    public void onMarketCommentRestore(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("market_comment_restore", callback);
    }

    public void onMessageAllow(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("message_allow", callback);
    }

    public void onMessageDeny(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("message_deny", callback);
    }

    public void onMessageNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("message_new", callback);
    }

    public void onMessageReply(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("message_reply", callback);
    }

    public void onPhotoCommentEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("photo_comment_edit", callback);
    }

    public void onPhotoCommentNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("photo_comment_new", callback);
    }

    public void onPhotoCommentRestore(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("photo_comment_restore", callback);
    }

    public void onPhotoNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("photo_new", callback);
    }

    public void onPhotoCommentDelete(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("photo_comment_delete", callback);
    }

    public void onVideoCommentEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("video_comment_edit", callback);
    }

    public void onVideoCommentNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("video_comment_new", callback);
    }

    public void onVideoCommentRestore(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("video_comment_restore", callback);
    }

    public void onVideoNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("video_new", callback);
    }

    public void onVideoCommentDelete(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("video_comment_delete", callback);
    }

    public void onWallPostNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_post_new", callback);
    }

    public void onWallReplyDelete(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_reply_delete", callback);
    }

    public void onWallReplyEdit(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_reply_edit", callback);
    }

    public void onWallReplyNew(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_reply_new", callback);
    }

    public void onWallReplyRestore(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_reply_restore", callback);
    }

    public void onWallRepost(Callback<JSONObject> callback) {
        callbackApiHandler.registerCallback("wall_repost", callback);
    }
}