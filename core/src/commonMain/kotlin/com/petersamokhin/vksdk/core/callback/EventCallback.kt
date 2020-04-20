package com.petersamokhin.vksdk.core.callback

/**
 * Callback for Bots LongPoll API
 */
interface EventCallback<in T: Any> {
    /**
     * Handle event
     *
     * @param event Next item
     */
    fun onEvent(event: T)
}