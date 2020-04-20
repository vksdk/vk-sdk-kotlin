package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.BatchRequestExecutor
import com.petersamokhin.vksdk.core.api.BatchRequestItem
import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.api.VkApiUploader
import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.error.UnsupportedActionException
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.io.FileOnDisk
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import com.petersamokhin.vksdk.core.model.objects.Message
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.core.utils.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement

/**
 * VK API client: abstract, common.
 *
 * Abstract because it is not possible for now
 * to have the default methods in expect classes
 *
 * @property id Client ID
 * @param token Access Token
 * @property type Type of the client: [VkApiClient.Type.User] or [VkApiClient.Type.Community]
 */
abstract class VkApiClientCommon(
    private val id: Int,
    token: String,
    private val type: VkApiClient.Type,
    settings: VkSettings
) {
    internal val json = Json(JsonConfiguration.Stable.copy(encodeDefaults = false, ignoreUnknownKeys = true))
    private val api = VkApi(settings.httpClient, settings.apiVersion, token, settings.defaultParams)
    private val batchRequestExecutor = BatchRequestExecutor(token, settings)
    private var botsLongPollApi = VkBotsLongPollApi(id, api)
    private val uploader = VkApiUploader(api, json)

    /**
     * Start long poll loop logic.
     *
     * @param restart Is reinit needed
     * @param settings Long polling settings
     */
    protected fun startLongPollingCommon(restart: Boolean, settings: VkBotsLongPollApi.Settings) {
        if (type == VkApiClient.Type.User) {
            throw UnsupportedActionException()
        }

        if (restart) {
            botsLongPollApi = VkBotsLongPollApi(id, api)
        }

        runBlocking {
            botsLongPollApi.startPolling(settings).join()
        }
    }

    /**
     * Wrapper for files uploading
     */
    fun uploader() = uploader

    /**
     * Use the coroutines Flow wrapper
     */
    abstract fun flows(): VkApiClientFlows

    /**
     * Stop the long polling loop
     */
    fun stopLongPolling() {
        botsLongPollApi.stopPolling()
    }

    /**
     * Clear all the long polling event listeners
     */
    fun clearLongPollListeners() = botsLongPollApi.clearListeners()

    /**
     * Handle each `message_new` event using listener
     */
    fun onMessage(listener: EventCallback<MessageNew>) {
        botsLongPollApi.registerListener(MessageNew.TYPE, listener)
    }

    /**
     * Handle each event using listener. Object is raw JSON
     */
    fun onEachEvent(listener: EventCallback<RawEvent>) {
        botsLongPollApi.registerListener(RawEvent.TYPE, listener)
    }

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     */
    fun sendMessage(message: Message): VkRequest {
        if (type == VkApiClient.Type.User) {
            throw UnsupportedActionException()
        }

        return api.call("messages.send", message.buildParams(json))
    }

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     */
    fun sendMessage(block: Message.() -> Unit): VkRequest {
        if (type == VkApiClient.Type.User) {
            throw UnsupportedActionException()
        }

        val message = Message()
        block(message)
        return sendMessage(message)
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     */
    protected fun callCommon(method: String, params: Parameters): VkRequest {
        return api.call(method, params)
    }

    /**
     * Call some API method and receive the response string
     */
    protected fun callCommon(method: String, params: Parameters, callback: Callback<JsonElement>) {
        api.call(method, params).enqueue(object: Callback<String> {
            override fun onResult(result: String) = callback.onResult(json.parseJson(result))
            override fun onError(error: Exception) = callback.onError(error)
        })
    }

    /**
     * Put some API call into queue
     */
    protected fun callBatchCommon(item: BatchRequestItem) {
        batchRequestExecutor.enqueue(item)
    }

    /**
     * Put some API call into queue
     */
    protected fun callBatchCommon(items: Collection<BatchRequestItem>) {
        batchRequestExecutor.enqueue(items)
    }
}

