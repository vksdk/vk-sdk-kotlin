package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.BatchRequestExecutor
import com.petersamokhin.vksdk.core.api.BatchRequestResult
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.CoroutineContext

/**
 * Coroutines Flow wrapper of the VK API client
 *
 * @property client Wrapped client
 */
public class VkApiClientFlows internal constructor(
    private val client: VkApiClient,
    private val batchRequestExecutor: BatchRequestExecutor,
    private val backgroundDispatcher: CoroutineDispatcher
) : CoroutineScope {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("VkApiClientFlows::exceptionHandler::error = $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job + exceptionHandler

    /**
     * Handle each `message_new` event in flow
     */
    @ExperimentalCoroutinesApi
    public fun onMessage(): Flow<MessageNew> =
        callbackFlow {
            val listener: EventCallback<MessageNew> = EventCallback(::trySend)
            client.onMessage(listener)

            awaitClose { client.unregisterListener(listener) }
        }

    /**
     * Handle each event in flow. Object is raw JSON
     */
    @ExperimentalCoroutinesApi
    public fun onEachEvent(): Flow<RawEvent> =
        callbackFlow {
            val listener: EventCallback<RawEvent> = EventCallback(::trySend)
            client.onEachEvent(listener)

            awaitClose { client.unregisterListener(listener) }
        }

    /**
     * Handle each batch request result
     */
    @FlowPreview
    @ExperimentalCoroutinesApi
    public fun onBatchRequestResult(): Flow<BatchRequestResult> =
        batchRequestExecutor.observeResults()

    public fun clear(): Unit =
        job.cancel()
}