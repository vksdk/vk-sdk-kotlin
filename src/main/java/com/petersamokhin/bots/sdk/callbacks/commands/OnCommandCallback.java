package com.petersamokhin.bots.sdk.callbacks.commands;

import com.petersamokhin.bots.sdk.objects.Message;

/**
 * Custom message callback
 */
public interface OnCommandCallback {

    void OnCommand(Message message);
}
