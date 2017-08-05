package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.audios.OnAudioNewCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.boards.OnBoardPostDeleteCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.boards.OnBoardPostEditCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.boards.OnBoardPostNewCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.boards.OnBoardPostRestoreCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.group.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.market.OnMarketCommentDeleteCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.market.OnMarketCommentEditCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.market.OnMarketCommentNewCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.market.OnMarketCommentRestoreCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.messages.OnMessageAllowCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.messages.OnMessageDenyCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.messages.OnMessageNewCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.messages.OnMessageReplyCallback;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.photos.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.videos.*;
import com.petersamokhin.bots.sdk.callbacks.callbackapi.wall.*;
import com.petersamokhin.bots.sdk.clients.Group;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Interacting with VK Callback API
 */
public class CallbackApiHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private final String ok = "ok";

    private Map<String, Callback> callbacks = new HashMap<>();

    private volatile boolean autoSetEvents = true;
    private Group group;

    private volatile boolean serverIsStarted = false;

    /**
     * Simple constructor
     *
     * @param path  path to listening: /callback
     */
    public CallbackApiHandler(String path) {

        if (serverIsStarted) {
            Spark.stop();
            serverIsStarted = false;
        } else {
            serverIsStarted = true;
        }

        // We need to listen port 80 to get events from VK
        // But if you have another server, you can set any port
        // And pre-roam
        int port = 1234;
        Spark.port(port);
        LOG.info("Started listening to VK Callback API on port {}.", port);

        // Handle
        Spark.post(path, (request, response) -> {

            JSONObject req = new JSONObject(request.body());

            String type = req.has("type") ? req.getString("type") : "";

            if (type.equals("confirmation")) {
                LOG.info("New confirmation request: {}", req);
                return group.api().callSync("groups.getCallbackConfirmationCode", "group_id", group.getId()).getJSONObject("response").getString("code");

            } else {
                handle(req);
                return ok;
            }
        });
    }

    /**
     * Non-default handler
     */
    public CallbackApiHandler(CallbackApiSettings settings) {

        if (serverIsStarted) {
            Spark.stop();
            serverIsStarted = false;
        } else {
            serverIsStarted = true;
        }

        // We need to listen port 80 to get events from VK
        // But if you have another server, you can set any port
        // And pre-roam
        Spark.port(settings.getPort());
        LOG.info("Started listening to VK Callback API on port 80.");

        // Set server if it's not setted
        if (settings.getHost() == null || settings.getHost().length() < 2) {
            LOG.error("Server is not set: trying to set...");
            boolean server_ok = false;
            while (!server_ok) {
                JSONObject response = group.api().callSync("groups.setCallbackServer", "group_id", group.getId(), "server_url", settings.getHost() + settings.getPath()).getJSONObject("response");
                LOG.error("New attempt to set server. Response: {}", response);
                if (response.getString("state").equals("ok")) {
                    server_ok = true;
                    LOG.info("Server is installed.");
                }
            }
        }

        // Handle
        Spark.post(settings.getPath(), (request, response) -> {

            JSONObject req = new JSONObject(request.body());

            String type = req.has("type") ? req.getString("type") : "";

            if (type.equals("confirmation")) {

                LOG.info("New confirmation request: {}", req);
                return group.api().callSync("groups.getCallbackConfirmationCode", "group_id", group.getId()).getJSONObject("response").getString("code");

            } else {

                // Autoanswer needed if you want to answer "ok" immediatly
                // Because if error or something else will occure
                // VK will repeat requests until you will answer "ok"
                if (settings.isAutoAnswer()) {
                    HttpServletResponse resp = response.raw();
                    OutputStream os = resp.getOutputStream();
                    os.write(ok.getBytes("UTF-8"));
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentLength(2);
                    os.close();
                }

                handle(req);

                return ok;
            }
        });
    }

    /**
     * Set callback
     *
     * @param callback Callback
     */
    public void registerCallback(String name, Callback callback) {

        if (autoSetEvents) {
            group.api().callSync("groups.setCallbackSettings", "group_id", group.getId(), name, 1);
        }

        this.callbacks.put(name, callback);
    }

    /**
     * Call the necessary methods in callback
     *
     * @param request Incoming request
     */
    private void handle(JSONObject request) {

        if (request.has("type") && request.has("object")) {
            String type = request.getString("type");
            JSONObject object = request.getJSONObject("object");

            switch (type) {

                case "message_new": {
                    if (callbacks.containsKey("message_new")) {
                        ((OnMessageNewCallback) callbacks.get("message_new")).callback(object);
                    }
                    break;
                }
                case "message_reply": {
                    if (callbacks.containsKey("message_reply")) {
                        ((OnMessageReplyCallback) callbacks.get("message_reply")).callback(object);
                    }
                    break;
                }
                case "message_allow": {
                    if (callbacks.containsKey("message_allow")) {
                        ((OnMessageAllowCallback) callbacks.get("message_allow")).callback(object);
                    }
                    break;
                }
                case "message_deny": {
                    if (callbacks.containsKey("message_deny")) {
                        ((OnMessageDenyCallback) callbacks.get("message_deny")).callback(object);
                    }
                    break;
                }
                case "photo_new": {
                    if (callbacks.containsKey("photo_new")) {
                        ((OnPhotoNewCallback) callbacks.get("photo_new")).callback(object);
                    }
                    break;
                }
                case "photo_comment_new": {
                    if (callbacks.containsKey("photo_comment_new")) {
                        ((OnPhotoCommentNewCallback) callbacks.get("photo_comment_new")).callback(object);
                    }
                    break;
                }
                case "photo_comment_edit": {
                    if (callbacks.containsKey("photo_comment_edit")) {
                        ((OnPhotoCommentEditCallback) callbacks.get("photo_comment_edit")).callback(object);
                    }
                    break;
                }
                case "photo_comment_restore": {
                    if (callbacks.containsKey("photo_comment_restore")) {
                        ((OnPhotoCommentRestoreCallback) callbacks.get("photo_comment_restore")).callback(object);
                    }
                    break;
                }
                case "photo_comment_delete": {
                    if (callbacks.containsKey("photo_comment_delete")) {
                        ((OnPhotoCommentDeleteCallback) callbacks.get("photo_comment_delete")).callback(object);
                    }
                    break;
                }
                case "audio_new": {
                    if (callbacks.containsKey("audio_new")) {
                        ((OnAudioNewCallback) callbacks.get("audio_new")).callback(object);
                    }
                    break;
                }
                case "video_new": {
                    if (callbacks.containsKey("video_new")) {
                        ((OnVideoNewCallback) callbacks.get("video_new")).callback(object);
                    }
                    break;
                }
                case "video_comment_new": {
                    if (callbacks.containsKey("video_comment_new")) {
                        ((OnVideoCommentNewCallback) callbacks.get("video_comment_new")).callback(object);
                    }
                    break;
                }
                case "video_comment_edit": {
                    if (callbacks.containsKey("video_comment_edit")) {
                        ((OnVideoCommentEditCallback) callbacks.get("video_comment_edit")).callback(object);
                    }
                    break;
                }
                case "video_comment_restore": {
                    if (callbacks.containsKey("video_comment_restore")) {
                        ((OnVideoCommentRestoreCallback) callbacks.get("video_comment_restore")).callback(object);
                    }
                    break;
                }
                case "video_comment_delete": {
                    if (callbacks.containsKey("video_comment_delete")) {
                        ((OnVideoCommentDeleteCallback) callbacks.get("video_comment_delete")).callback(object);
                    }
                    break;
                }
                case "wall_post_new": {
                    if (callbacks.containsKey("wall_post_new")) {
                        ((OnWallPostNewCallback) callbacks.get("wall_post_new")).callback(object);
                    }
                    break;
                }
                case "wall_repost": {
                    if (callbacks.containsKey("wall_repost")) {
                        ((OnWallRepostCallback) callbacks.get("wall_repost")).callback(object);
                    }
                    break;
                }
                case "wall_reply_new": {
                    if (callbacks.containsKey("wall_reply_new")) {
                        ((OnWallReplyNewCallback) callbacks.get("wall_reply_new")).callback(object);
                    }
                    break;
                }
                case "wall_reply_edit": {
                    if (callbacks.containsKey("wall_reply_edit")) {
                        ((OnWallReplyEditCallback) callbacks.get("wall_reply_edit")).callback(object);
                    }
                    break;
                }
                case "wall_reply_restore": {
                    if (callbacks.containsKey("wall_reply_restore")) {
                        ((OnWallReplyRestoreCallback) callbacks.get("wall_reply_restore")).callback(object);
                    }
                    break;
                }
                case "wall_reply_delete": {
                    if (callbacks.containsKey("wall_reply_delete")) {
                        ((OnWallReplyDeleteCallback) callbacks.get("wall_reply_delete")).callback(object);
                    }
                    break;
                }
                case "board_post_new": {
                    if (callbacks.containsKey("board_post_new")) {
                        ((OnBoardPostNewCallback) callbacks.get("board_post_new")).callback(object);
                    }
                    break;
                }
                case "board_post_edit": {
                    if (callbacks.containsKey("board_post_edit")) {
                        ((OnBoardPostEditCallback) callbacks.get("board_post_edit")).callback(object);
                    }
                    break;
                }
                case "board_post_restore": {
                    if (callbacks.containsKey("board_post_restore")) {
                        ((OnBoardPostRestoreCallback) callbacks.get("board_post_restore")).callback(object);
                    }
                    break;
                }
                case "board_post_delete": {
                    if (callbacks.containsKey("board_post_delete")) {
                        ((OnBoardPostDeleteCallback) callbacks.get("board_post_delete")).callback(object);
                    }
                    break;
                }
                case "market_comment_new": {
                    if (callbacks.containsKey("market_comment_new")) {
                        ((OnMarketCommentNewCallback) callbacks.get("market_comment_new")).callback(object);
                    }
                    break;
                }
                case "market_comment_edit": {
                    if (callbacks.containsKey("market_comment_edit")) {
                        ((OnMarketCommentEditCallback) callbacks.get("market_comment_edit")).callback(object);
                    }
                    break;
                }
                case "market_comment_restore": {
                    if (callbacks.containsKey("market_comment_restore")) {
                        ((OnMarketCommentRestoreCallback) callbacks.get("market_comment_restore")).callback(object);
                    }
                    break;
                }
                case "market_comment_delete": {
                    if (callbacks.containsKey("market_comment_delete")) {
                        ((OnMarketCommentDeleteCallback) callbacks.get("market_comment_delete")).callback(object);
                    }
                    break;
                }
                case "group_leave": {
                    if (callbacks.containsKey("group_leave")) {
                        ((OnGroupLeaveCallback) callbacks.get("group_leave")).callback(object);
                    }
                    break;
                }
                case "group_join": {
                    if (callbacks.containsKey("group_join")) {
                        ((OnGroupJoinCallback) callbacks.get("group_join")).callback(object);
                    }
                    break;
                }
                case "poll_vote_new": {
                    if (callbacks.containsKey("poll_vote_new")) {
                        ((OnPollVoteNewCallback) callbacks.get("poll_vote_new")).callback(object);
                    }
                    break;
                }
                case "group_officers_edit": {
                    if (callbacks.containsKey("group_officers_edit")) {
                        ((OnGroupOfficersEditCallback) callbacks.get("group_officers_edit")).callback(object);
                    }
                    break;
                }
                case "group_change_settings": {
                    if (callbacks.containsKey("group_change_settings")) {
                        ((OnGroupChangeSettingsCallback) callbacks.get("group_change_settings")).callback(object);
                    }
                    break;
                }
                case "group_change_photo": {
                    if (callbacks.containsKey("group_change_photo")) {
                        ((OnGroupChangePhotoCallback) callbacks.get("group_change_photo")).callback(object);
                    }
                    break;
                }
            }
        }
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Settings for interacting with Callback API
     */
    public class CallbackApiSettings {

        private String host = null, path;
        private int port = 80;
        private boolean autoAnswer = false;

        private CallbackApiSettings(String host, int port, String path, boolean autoAnswer, boolean autoSet) {
            this.host = host;
            this.path = path;
            this.port = port;
            this.autoAnswer = autoAnswer;
            autoSetEvents = autoSet;
        }

        /* Getters and setters */
        private String getHost() {
            return host;
        }

        private String getPath() {
            return path;
        }

        private int getPort() {
            return port;
        }

        private boolean isAutoAnswer() {
            return autoAnswer;
        }
    }
}