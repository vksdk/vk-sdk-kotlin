package com.petersamokhin.bots.sdk.callbacks.messages;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.objects.Message;

/**
 * Callback for message with some kind of attachments,
 * or if message has no attachments
 */
public interface OnAudioMessageCallback extends Callback {

    void onAudioMessage(Message message);
}

