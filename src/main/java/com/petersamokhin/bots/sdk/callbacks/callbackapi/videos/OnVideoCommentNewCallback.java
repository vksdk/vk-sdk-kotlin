package com.petersamokhin.bots.sdk.callbacks.callbackapi.videos;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import org.json.JSONObject;

/**
 * See more: <a href="https://vk.com/dev/callback_api">link</a>.
 */
public interface OnVideoCommentNewCallback extends Callback {

    void callback(JSONObject object);
}