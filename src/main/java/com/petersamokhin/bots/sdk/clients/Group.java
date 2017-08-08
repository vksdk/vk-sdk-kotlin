package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.callbacks.callbackapi.audios.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.boards.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.group.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.market.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.messages.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.photos.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.videos.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.wall.*;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiHandler;
import com.petersamokhin.bots.sdk.utils.Connection;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiSettings;
import okhttp3.MediaType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.regex.Pattern;

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
     * Check are users in group
     *
     * @param id IDs of users
     */
    public boolean isMember(Object id) {
        return getId() != null && api().callSync("groups.isMember", "group_id", getId().toString().replace("-", ""), "user_id", id).getInt("response") == 1;
    }

    /**
     * Upload group cover by file from url or from disk
     *
     * @return VK response
     */
    public JSONObject uploadCover(Object... params) {

        String cover;
        Integer groupId;
        String accessToken;

        switch (params.length) {
            case 0:
                return new JSONObject();

            case 1: {

                if (this.getId() == null || this.getId() == 0)
                    return new JSONObject();

                groupId = this.getId();
                cover = String.valueOf(params[0]);
                accessToken = this.getAccessToken();
                break;
            }

            case 2: {
                groupId = (Integer) params[0];
                cover = String.valueOf(params[1]);
                accessToken = this.getAccessToken();
                break;
            }

            case 3: {
                groupId = (Integer) params[0];
                accessToken = String.valueOf(params[1]);
                cover = String.valueOf(params[2]);
                break;
            }

            default: {
                return new JSONObject().put("response", "Some error occured, cover not uploaded.");
            }
        }

        // we can upload cover only by file from disk or url
        if (cover.endsWith(".png") || cover.endsWith(".jpg") || cover.endsWith(".gif") || cover.endsWith(".jpeg")) {

            File templatePhoto;
            String template_name = "template_cover." + FilenameUtils.getExtension(cover);
            boolean photoFromUrl = false;
            // from url
            if (Pattern.matches("https?://.+", cover)) {

                try {
                    templatePhoto = new File(template_name);
                    templatePhoto.createNewFile();
                    Files.setPosixFilePermissions(Paths.get(templatePhoto.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
                    FileUtils.copyURLToFile(new URL(cover), templatePhoto, 5000, 5000);

                    photoFromUrl = true;
                } catch (IOException ignored) {
                    LOG.error("IOException when downloading file {}, message: {}", cover, ignored.toString());
                    return new JSONObject().put("response", "Some error occured, cover not uploaded.");
                }
            } else {
                templatePhoto = new File(cover);
            }

            String getUploadServerQuery = "https://api.vk.com/method/photos.getOwnerCoverPhotoUploadServer?group_id=" + groupId + "&crop_x=0&crop_y=0&crop_x2=1590&crop_y2=400&access_token=" + accessToken + "&v=5.67";

            JSONObject getUploadServerResponse = Connection.getRequestResponse(getUploadServerQuery);

            String coverUploadUrl = "";

            if (getUploadServerResponse.has("response") && getUploadServerResponse.getJSONObject("response").has("upload_url"))
                getUploadServerResponse.getJSONObject("response").getString("upload_url");


            if (coverUploadUrl.length() < 5)
                return new JSONObject();

            String responseOfUploadingPhotoToVk = Connection.getFileUploadAnswerOfVK(
                    coverUploadUrl,
                    "photo",
                    MediaType.parse("image/*"),
                    templatePhoto
            );

            JSONObject response = new JSONObject();

            try {
                response = new JSONObject(responseOfUploadingPhotoToVk);
            } catch (JSONException ignored) {
                LOG.error("Bad response of uploading cover: {}, error: {}", responseOfUploadingPhotoToVk, ignored.toString());
            }

            if (photoFromUrl) {
                try {
                    Files.delete(Paths.get(templatePhoto.getAbsolutePath()));
                } catch (IOException ignored) {
                }
            }

            if (response.has("hash") && response.has("photo")) {

                String hashField = response.getString("hash");
                String photoField = response.getString("photo");

                String save_cover_query = "https://api.vk.com/method/photos.saveOwnerCoverPhoto?hash=" + hashField + "&photo=" + photoField + "&access_token=" + accessToken + "&v=5.67";

                return Connection.getRequestResponse(save_cover_query);
            }
        }

        return new JSONObject().put("response", "Some error occured, cover not uploaded.");
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

    public void onAudioNew(OnAudioNewCallback callback) {
        callbackApiHandler.registerCallback("audio_new", callback);
    }

    public void onBoardPostDelete(OnBoardPostDeleteCallback callback) {
        callbackApiHandler.registerCallback("board_post_delete", callback);
    }

    public void onBoardPostEdit(OnBoardPostEditCallback callback) {
        callbackApiHandler.registerCallback("board_post_edit", callback);
    }

    public void onBoardPostNew(OnBoardPostNewCallback callback) {
        callbackApiHandler.registerCallback("board_post_new", callback);
    }

    public void onBoardPostRestore(OnBoardPostDeleteCallback callback) {
        callbackApiHandler.registerCallback("board_post_restore", callback);
    }

    public void onGroupChangePhoto(OnGroupChangePhotoCallback callback) {
        callbackApiHandler.registerCallback("group_change_photo", callback);
    }

    public void onGroupChangeSettings(OnGroupChangeSettingsCallback callback) {
        callbackApiHandler.registerCallback("group_change_settings", callback);
    }

    public void onGroupJoin(OnGroupJoinCallback callback) {
        callbackApiHandler.registerCallback("group_join", callback);
    }

    public void onGroupLeave(OnGroupLeaveCallback callback) {
        callbackApiHandler.registerCallback("group_leave", callback);
    }

    public void onGroupOfficersEdit(OnGroupOfficersEditCallback callback) {
        callbackApiHandler.registerCallback("group_officers_edit", callback);
    }

    public void onPollVoteNew(OnPollVoteNewCallback callback) {
        callbackApiHandler.registerCallback("poll_vote_new", callback);
    }

    public void onMarketCommentDelete(OnMarketCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("market_comment_delete", callback);
    }

    public void onMarketCommentEdit(OnMarketCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("market_comment_edit", callback);
    }

    public void onMarketCommentNew(OnMarketCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("market_comment_new", callback);
    }

    public void onMarketCommentRestore(OnMarketCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("market_comment_restore", callback);
    }

    public void onMessageAllow(OnMessageAllowCallback callback) {
        callbackApiHandler.registerCallback("message_allow", callback);
    }

    public void onMessageDeny(OnMessageDenyCallback callback) {
        callbackApiHandler.registerCallback("message_deny", callback);
    }

    public void onMessageNew(OnMessageNewCallback callback) {
        callbackApiHandler.registerCallback("message_new", callback);
    }

    public void onMessageReply(OnMessageReplyCallback callback) {
        callbackApiHandler.registerCallback("message_reply", callback);
    }

    public void onPhotoCommentEdit(OnPhotoCommentEditCallback callback) {
        callbackApiHandler.registerCallback("photo_comment_edit", callback);
    }

    public void onPhotoCommentNew(OnPhotoCommentNewCallback callback) {
        callbackApiHandler.registerCallback("photo_comment_new", callback);
    }

    public void onPhotoCommentRestore(OnPhotoCommentRestoreCallback callback) {
        callbackApiHandler.registerCallback("photo_comment_restore", callback);
    }

    public void onPhotoNew(OnPhotoNewCallback callback) {
        callbackApiHandler.registerCallback("photo_new", callback);
    }

    public void onPhotoCommentDelete(OnPhotoCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("photo_comment_delete", callback);
    }

    public void onVideoCommentEdit(OnVideoCommentEditCallback callback) {
        callbackApiHandler.registerCallback("video_comment_edit", callback);
    }

    public void onVideoCommentNew(OnVideoCommentNewCallback callback) {
        callbackApiHandler.registerCallback("video_comment_new", callback);
    }

    public void onVideoCommentRestore(OnVideoCommentRestoreCallback callback) {
        callbackApiHandler.registerCallback("video_comment_restore", callback);
    }

    public void onVideoNew(OnVideoNewCallback callback) {
        callbackApiHandler.registerCallback("video_new", callback);
    }

    public void onVideoCommentDelete(OnVideoCommentDeleteCallback callback) {
        callbackApiHandler.registerCallback("video_comment_delete", callback);
    }

    public void onWallPostNew(OnWallPostNewCallback callback) {
        callbackApiHandler.registerCallback("wall_post_new", callback);
    }

    public void onWallReplyDelete(OnWallReplyDeleteCallback callback) {
        callbackApiHandler.registerCallback("wall_reply_delete", callback);
    }

    public void onWallReplyEdit(OnWallReplyEditCallback callback) {
        callbackApiHandler.registerCallback("wall_reply_edit", callback);
    }

    public void onWallReplyNew(OnWallReplyNewCallback callback) {
        callbackApiHandler.registerCallback("wall_reply_new", callback);
    }

    public void onWallReplyRestore(OnWallReplyRestoreCallback callback) {
        callbackApiHandler.registerCallback("wall_reply_restore", callback);
    }

    public void onWallRepost(OnWallRepostCallback callback) {
        callbackApiHandler.registerCallback("wall_repost", callback);
    }
}