package com.petersamokhin.bots.sdk.callbacks.callbackapi.photos;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import org.json.JSONObject;

/**
 * See more: <a href="https://vk.com/dev/callback_api">link</a>.
 */
public interface OnPhotoCommentEditCallback extends Callback {

    void callback(JSONObject object);
}