package com.petersamokhin.vksdk.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Runs a new coroutine and **blocks** the current thread _interruptibly_ until its completion.
 * There is no implementation for JS, so also no implementation in the common module.
 *
 * @see [https://github.com/Kotlin/kotlinx.coroutines/issues/672]
 *
 * @param context Coroutine context
 * @param block Block to execute
 */
public expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T?
): T?

/**
 * Adds [element] into to this channel, **blocking** the caller while this channel [Channel.isFull],
 * or throws exception if the channel [Channel.isClosedForSend] (see [Channel.close] for details).
 *
 * This is a way to call [Channel.send] method inside a blocking code using [runBlocking],
 * so this function should not be used from coroutine.
 */
public expect fun <E> SendChannel<E>.sendBlocking(element: E)