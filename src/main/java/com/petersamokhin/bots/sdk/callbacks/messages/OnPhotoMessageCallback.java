package com.petersamokhin.bots.sdk.callbacks.messages;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.objects.Message;

/**
 * Callback for message with some kind of attachments,
 * or if message has no attachments
 */
public interface OnPhotoMessageCallback extends Callback {

    void onPhotoMessage(Message message);
}
