package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.callbacks.Callback;
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

    private static final Logger LOG = LoggerFactory.getLogger(CallbackApiHandler.class);

    private final String ok = "ok";

    private Map<String, Callback> callbacks = new HashMap<>();

    public static volatile boolean autoSetEvents = true;
    private Group group;

    private volatile boolean serverIsStarted = false;

    /**
     * Simple constructor
     *
     * @param path path to listening: /callback
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
        int port = 80;
        Spark.port(port);
        LOG.info("Started listening to VK Callback API on port {}.", port);

        // Handle
        Spark.post(path, (request, response) -> {

            JSONObject req = new JSONObject(request.body());

            String type = req.has("type") ? req.getString("type") : "";

            if (type.equals("confirmation")) {
                LOG.info("New confirmation request: {}", req);
                return new JSONObject(group.api().callSync("groups.getCallbackConfirmationCode", "group_id", group.getId())).getJSONObject("response").getString("code");

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
                JSONObject response = new JSONObject(group.api().callSync("groups.setCallbackServer", "group_id", group.getId(), "server_url", settings.getHost() + settings.getPath())).getJSONObject("response");
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
                return new JSONObject(group.api().callSync("groups.getCallbackConfirmationCode", "group_id", group.getId())).getJSONObject("response").getString("code");

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
                        callbacks.get("message_new").onResult(object);
                    }
                    break;
                }
                case "message_reply": {
                    if (callbacks.containsKey("message_reply")) {
                        callbacks.get("message_reply").onResult(object);
                    }
                    break;
                }
                case "message_allow": {
                    if (callbacks.containsKey("message_allow")) {
                        callbacks.get("message_allow").onResult(object);
                    }
                    break;
                }
                case "message_deny": {
                    if (callbacks.containsKey("message_deny")) {
                        callbacks.get("message_deny").onResult(object);
                    }
                    break;
                }
                case "photo_new": {
                    if (callbacks.containsKey("photo_new")) {
                        callbacks.get("photo_new").onResult(object);
                    }
                    break;
                }
                case "photo_comment_new": {
                    if (callbacks.containsKey("photo_comment_new")) {
                        callbacks.get("photo_comment_new").onResult(object);
                    }
                    break;
                }
                case "photo_comment_edit": {
                    if (callbacks.containsKey("photo_comment_edit")) {
                        callbacks.get("photo_comment_edit").onResult(object);
                    }
                    break;
                }
                case "photo_comment_restore": {
                    if (callbacks.containsKey("photo_comment_restore")) {
                        callbacks.get("photo_comment_restore").onResult(object);
                    }
                    break;
                }
                case "photo_comment_delete": {
                    if (callbacks.containsKey("photo_comment_delete")) {
                        callbacks.get("photo_comment_delete").onResult(object);
                    }
                    break;
                }
                case "audio_new": {
                    if (callbacks.containsKey("audio_new")) {
                        callbacks.get("audio_new").onResult(object);
                    }
                    break;
                }
                case "video_new": {
                    if (callbacks.containsKey("video_new")) {
                        callbacks.get("video_new").onResult(object);
                    }
                    break;
                }
                case "video_comment_new": {
                    if (callbacks.containsKey("video_comment_new")) {
                        callbacks.get("video_comment_new").onResult(object);
                    }
                    break;
                }
                case "video_comment_edit": {
                    if (callbacks.containsKey("video_comment_edit")) {
                        callbacks.get("video_comment_edit").onResult(object);
                    }
                    break;
                }
                case "video_comment_restore": {
                    if (callbacks.containsKey("video_comment_restore")) {
                        callbacks.get("video_comment_restore").onResult(object);
                    }
                    break;
                }
                case "video_comment_delete": {
                    if (callbacks.containsKey("video_comment_delete")) {
                        callbacks.get("video_comment_delete").onResult(object);
                    }
                    break;
                }
                case "wall_post_new": {
                    if (callbacks.containsKey("wall_post_new")) {
                        callbacks.get("wall_post_new").onResult(object);
                    }
                    break;
                }
                case "wall_repost": {
                    if (callbacks.containsKey("wall_repost")) {
                        callbacks.get("wall_repost").onResult(object);
                    }
                    break;
                }
                case "wall_reply_new": {
                    if (callbacks.containsKey("wall_reply_new")) {
                        callbacks.get("wall_reply_new").onResult(object);
                    }
                    break;
                }
                case "wall_reply_edit": {
                    if (callbacks.containsKey("wall_reply_edit")) {
                        callbacks.get("wall_reply_edit").onResult(object);
                    }
                    break;
                }
                case "wall_reply_restore": {
                    if (callbacks.containsKey("wall_reply_restore")) {
                        callbacks.get("wall_reply_restore").onResult(object);
                    }
                    break;
                }
                case "wall_reply_delete": {
                    if (callbacks.containsKey("wall_reply_delete")) {
                        callbacks.get("wall_reply_delete").onResult(object);
                    }
                    break;
                }
                case "board_post_new": {
                    if (callbacks.containsKey("board_post_new")) {
                        callbacks.get("board_post_new").onResult(object);
                    }
                    break;
                }
                case "board_post_edit": {
                    if (callbacks.containsKey("board_post_edit")) {
                        callbacks.get("board_post_edit").onResult(object);
                    }
                    break;
                }
                case "board_post_restore": {
                    if (callbacks.containsKey("board_post_restore")) {
                        callbacks.get("board_post_restore").onResult(object);
                    }
                    break;
                }
                case "board_post_delete": {
                    if (callbacks.containsKey("board_post_delete")) {
                        callbacks.get("board_post_delete").onResult(object);
                    }
                    break;
                }
                case "market_comment_new": {
                    if (callbacks.containsKey("market_comment_new")) {
                        callbacks.get("market_comment_new").onResult(object);
                    }
                    break;
                }
                case "market_comment_edit": {
                    if (callbacks.containsKey("market_comment_edit")) {
                        callbacks.get("market_comment_edit").onResult(object);
                    }
                    break;
                }
                case "market_comment_restore": {
                    if (callbacks.containsKey("market_comment_restore")) {
                        callbacks.get("market_comment_restore").onResult(object);
                    }
                    break;
                }
                case "market_comment_delete": {
                    if (callbacks.containsKey("market_comment_delete")) {
                        callbacks.get("market_comment_delete").onResult(object);
                    }
                    break;
                }
                case "group_leave": {
                    if (callbacks.containsKey("group_leave")) {
                        callbacks.get("group_leave").onResult(object);
                    }
                    break;
                }
                case "group_join": {
                    if (callbacks.containsKey("group_join")) {
                        callbacks.get("group_join").onResult(object);
                    }
                    break;
                }
                case "poll_vote_new": {
                    if (callbacks.containsKey("poll_vote_new")) {
                        callbacks.get("poll_vote_new").onResult(object);
                    }
                    break;
                }
                case "group_officers_edit": {
                    if (callbacks.containsKey("group_officers_edit")) {
                        callbacks.get("group_officers_edit").onResult(object);
                    }
                    break;
                }
                case "group_change_settings": {
                    if (callbacks.containsKey("group_change_settings")) {
                        callbacks.get("group_change_settings").onResult(object);
                    }
                    break;
                }
                case "group_change_photo": {
                    if (callbacks.containsKey("group_change_photo")) {
                        callbacks.get("group_change_photo").onResult(object);
                    }
                    break;
                }
            }
        }
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}