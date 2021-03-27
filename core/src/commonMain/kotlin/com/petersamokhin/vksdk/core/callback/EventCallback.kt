package com.petersamokhin.vksdk.core.callback

/**
 * Callback for Bots LongPoll API
 */
public fun interface EventCallback<in T: Any> {
    /**
     * Handle event
     *
     * @param event Next item
     */
    public fun onEvent(event: T)
}