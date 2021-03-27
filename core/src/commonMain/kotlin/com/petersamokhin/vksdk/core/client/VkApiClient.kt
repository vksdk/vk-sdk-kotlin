package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.*
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.client.VkApiClient.Type
import com.petersamokhin.vksdk.core.client.VkApiClient.Type.Community
import com.petersamokhin.vksdk.core.client.VkApiClient.Type.User
import com.petersamokhin.vksdk.core.error.UnsupportedActionException
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.error.VkSdkInitiationException
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkAccessTokenResponse
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import com.petersamokhin.vksdk.core.model.objects.Message
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * VK API client: abstract, common.
 *
 * Abstract because it is not possible for now
 * to have the default methods in expect classes
 *
 * @property id Client ID
 * @param token Access Token
 * @property type Type of the client: [Type.User] or [Type.Community]
 */
public class VkApiClient(
    public val id: Int,
    public val token: String,
    public val type: Type,
    public val settings: VkSettings
) {
    private val api = VkApi(settings.httpClient, settings.apiVersion, token, settings.defaultParams)
    private val batchRequestExecutor = BatchRequestExecutor(token, settings, settings.json)
    private var botsLongPollApi = VkBotsLongPollApi(id, api, settings.backgroundDispatcher, settings.json)
    private val uploader = VkApiUploader(api, settings.json)
    private val flowsWrapper = VkApiClientFlows(this, batchRequestExecutor, settings.backgroundDispatcher)

    /**
     * Start the long polling, [https://vk.com/dev/bots_longpoll](documentation).
     * Messages API for regular users is restricted.
     * @see [https://vk.com/dev/using_longpoll](this) and [https://vk.com/dev/messages_api](this)
     */
    @JvmOverloads
    public suspend fun startLongPolling(
        restart: Boolean = false,
        settings: VkBotsLongPollApi.Settings = VkBotsLongPollApi.Settings()
    ) {
        if (type == User) {
            throw UnsupportedActionException()
        }

        if (restart) {
            with(botsLongPollApi) {
                clearListeners()
                stopPolling()
            }

            botsLongPollApi = VkBotsLongPollApi(id, api, this.settings.backgroundDispatcher, this.settings.json)
        }

        botsLongPollApi.startPolling(settings).join()
    }

    /**
     * Exposed internal API object
     */
    public fun api(): VkApi =
        api

    /**
     * Wrapper for files uploading
     */
    public fun uploader(): VkApiUploader =
        uploader

    /**
     * Wrapper for coroutines flows
     */
    public fun flows(): VkApiClientFlows =
        flowsWrapper

    /**
     * Stop the long polling loop
     */
    public fun stopLongPolling() {
        botsLongPollApi.stopPolling()
    }

    /**
     * Clear all the long polling event listeners
     */
    public fun clearLongPollListeners(): Unit =
        botsLongPollApi.clearListeners()

    /**
     * Handle each `message_new` event using listener
     */
    public fun onMessage(listener: EventCallback<MessageNew>) {
        botsLongPollApi.registerListener(MessageNew.TYPE, listener)
    }

    /**
     * Handle each `message_new` event using listener
     */
    public fun onMessage(block: (MessageNew) -> Unit) {
        botsLongPollApi.registerListener(MessageNew.TYPE, block)
    }

    /**
     * Handle each event using listener. Object is raw JSON
     */
    public fun onEachEvent(listener: EventCallback<RawEvent>) {
        botsLongPollApi.registerListener(RawEvent.TYPE, listener)
    }

    /**
     * Handle each event using listener. Object is raw JSON
     */
    public fun onEachEvent(block: (RawEvent) -> Unit) {
        botsLongPollApi.registerListener(RawEvent.TYPE, block)
    }

    /**
     * Remove Callback API listener
     */
    public fun unregisterListener(listener: EventCallback<*>) {
        botsLongPollApi.unregisterListener(listener)
    }

    /**
     * Create a [VkRequest]
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @return API request wrapper, which is not yet executed!
     */
    @JvmOverloads
    public fun call(method: String, params: Parameters = Parameters()): VkRequest =
        api.call(method, params)

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     *
     * @return API request wrapper, which is not yet executed!
     */
    public fun sendMessage(message: Message): VkRequest {
        if (type == User) {
            throw UnsupportedActionException()
        }

        return api.call("messages.send", message.buildParams(settings.json))
    }

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     *
     * @return API request wrapper, which is not yet executed!
     */
    public fun sendMessage(block: Message.() -> Unit): VkRequest {
        if (type == User) {
            throw UnsupportedActionException()
        }

        val message = Message()
        block(message)
        return sendMessage(message)
    }

    /**
     * Call some API method and receive the response string as [JsonElement].
     * Executes the request!
     */
    @JvmOverloads
    public suspend fun get(method: String, params: Parameters = Parameters()): JsonElement =
        api.call(method, params).execute().let(settings.json::parseToJsonElement)

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request API request wrapper
     * @param callback Callback to handle the result or an error
     *
     * Executes the request!
     */
    @JvmOverloads
    public fun get(request: VkRequest, callback: Callback<JsonElement> = Callback.empty()) {
        get(BatchRequestItem(request, callback))
    }

    /**
     * Put some API call into queue.
     *
     * Executes the request!
     */
    public fun get(item: BatchRequestItem) {
        batchRequestExecutor.enqueue(item)
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param items Wrappers of the requests and callbacks
     *
     * Executes the request!
     */
    public fun get(vararg items: BatchRequestItem) {
        get(items.toList())
    }

    /**
     * Put some API call into queue.
     *
     * Executes the requests!
     */
    public fun get(items: Collection<BatchRequestItem>) {
        if (items.isNotEmpty()) {
            batchRequestExecutor.enqueue(items)
        }
    }

    /**
     * Type of VK client
     *
     * @property User Usual user, can't use messages API
     * @property Community Community (group or public community)
     */
    public enum class Type {
        User, Community
    }

    /**
     * https://vk.com/apps?act=manage
     */
    public data class AppInfo(
        val clientId: Int,
        val clientSecret: String,
        val redirectUri: String = DEFAULT_REDIRECT_URI
    ) {
        public companion object {
            public const val KEY_CLIENT_ID: String = "client_id"
            public const val KEY_CLIENT_SECRET: String = "client_secret"
            public const val KEY_REDIRECT_URI: String = "redirect_uri"
        }
    }

    public companion object {
        private const val ACCESS_TOKEN_URL = "https://oauth.vk.com/access_token"
        private const val KEY_CODE = "code"
        public const val DEFAULT_REDIRECT_URI: String = "https://oauth.vk.com/blank.html"

        /**
         * Useful method for server-side operations.
         * Receive the code from the client and do something on his behalf.
         *
         * @param code VK code, https://vk.com/dev/authcode_flow_user
         * @param app Information about your app (client)
         * @param settings VK API Client settings
         * @return VK API client.
         */
        @JvmStatic
        public suspend fun fromCode(code: String, app: AppInfo, settings: VkSettings): VkApiClient {
            val query = paramsOf(
                AppInfo.KEY_CLIENT_ID to app.clientId,
                AppInfo.KEY_CLIENT_SECRET to app.clientSecret,
                AppInfo.KEY_REDIRECT_URI to app.redirectUri,
                KEY_CODE to code
            ).buildQuery()

            val tokenResponse = try {
                settings.httpClient.get("$ACCESS_TOKEN_URL?$query").let {
                    settings.json.decodeFromString(VkAccessTokenResponse.serializer(), it.bodyString())
                }
            } catch (e: Throwable) {
                throw VkSdkInitiationException("VK Client from code", cause = e)
            }

            return VkApiClient(
                id = tokenResponse.userId
                    ?: throw VkResponseException("VK access_token from code error: user_id is null"),
                token = tokenResponse.accessToken
                    ?: throw VkResponseException("VK access_token from code error: access_token is null"),
                type = User,
                settings = settings
            )
        }
    }
}

