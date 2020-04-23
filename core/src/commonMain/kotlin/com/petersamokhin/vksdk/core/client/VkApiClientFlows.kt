package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import com.petersamokhin.vksdk.core.model.objects.Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.CoroutineContext

/**
 * Coroutines Flow wrapper of the VK API client
 *
 * @property client Wrapped client
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VkApiClientFlows(
    private val client: VkApiClient,
    private val backgroundDispatcher: CoroutineDispatcher
): CoroutineScope {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("VkApiClientFlows::exceptionHandler::error = $throwable")
    }
    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job + exceptionHandler

    /**
     * Handle each `message_new` event in flow
     */
    fun onMessage(): Flow<MessageNew> {
        return callbackFlow {
            client.onMessage(object: EventCallback<MessageNew> {
                override fun onEvent(event: MessageNew) {
                    launch { send(event) }
                }
            })

            awaitClose()
        }
    }

    /**
     * Handle each event in flow. Object is raw JSON
     */
    fun onEachEvent(): Flow<RawEvent> {
        return callbackFlow {
            client.onEachEvent(object: EventCallback<RawEvent> {
                override fun onEvent(event: RawEvent) {
                    launch { send(event) }
                }
            })

            awaitClose()
        }
    }

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @return API request wrapper
     */
    fun call(method: String, params: Parameters = Parameters()): Flow<String> {
        return callbackFlow {
            client.call(method, params).enqueue(object: Callback<String> {
                override fun onResult(result: String) {
                    launch {
                        send(result)
                        close()
                    }
                }

                override fun onError(error: Exception) {
                    close(error)
                }
            })

            awaitClose()
        }
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     */
    fun call(method: String, params: Parameters = Parameters(), batch: Boolean = true): Flow<JsonElement> {
        return callbackFlow {
            client.call(method, params, batch, object: Callback<JsonElement> {
                override fun onResult(result: JsonElement) {
                    launch {
                        send(result)
                        close()
                    }
                }

                override fun onError(error: Exception) {
                    close(error)
                }
            })

            awaitClose()
        }
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request Request info
     */
    fun call(request: VkRequest, batch: Boolean = true): Flow<JsonElement> {
        return callbackFlow {
            client.call(request, batch, object: Callback<JsonElement> {
                override fun onResult(result: JsonElement) {
                    launch {
                        send(result)
                        close()
                    }
                }

                override fun onError(error: Exception) {
                    close(error)
                }
            })

            awaitClose()
        }
    }

    /**
     * Send message and receive the response string, parsed into JsonElement
     */
    fun sendMessage(message: Message): Flow<JsonElement> {
        return call(client.sendMessage(message), true)
    }

    /**
     * Send message and receive the response string, parsed into JsonElement
     */
    fun sendMessage(block: Message.() -> Unit): Flow<JsonElement> {
        return call(client.sendMessage(block), true)
    }
}