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
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.error.VkSdkInitiationException
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkAccessTokenResponse
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import com.petersamokhin.vksdk.core.model.objects.Message
import com.petersamokhin.vksdk.core.utils.assembleCallback
import com.petersamokhin.vksdk.core.utils.assembleEventCallback
import com.petersamokhin.vksdk.core.utils.defaultJson
import com.petersamokhin.vksdk.core.utils.runBlocking
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
class VkApiClient(
    private val id: Int,
    token: String,
    private val type: Type,
    private val settings: VkSettings
) {
    internal val json = defaultJson()
    private val api = VkApi(settings.httpClient, settings.apiVersion, token, settings.defaultParams)
    private val batchRequestExecutor = BatchRequestExecutor(token, settings)
    private var botsLongPollApi = VkBotsLongPollApi(id, api, settings.backgroundDispatcher)
    private val uploader = VkApiUploader(api, json)
    private val flowsWrapper = VkApiClientFlows(this, settings.backgroundDispatcher)

    /**
     * Start the long polling, [https://vk.com/dev/bots_longpoll](documentation).
     * Messages API for regular users is restricted.
     * @see [https://vk.com/dev/using_longpoll](this) and [https://vk.com/dev/messages_api](this)
     */
    @JvmOverloads
    fun startLongPolling(restart: Boolean = false, settings: VkBotsLongPollApi.Settings = VkBotsLongPollApi.Settings()) {
        if (type == Type.User) {
            throw UnsupportedActionException()
        }

        if (restart) {
            botsLongPollApi = VkBotsLongPollApi(id, api, this.settings.backgroundDispatcher)
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
    fun flows() = flowsWrapper

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
     * Handle each `message_new` event using listener
     */
    fun onMessage(block: (MessageNew) -> Unit) {
        botsLongPollApi.registerListener(MessageNew.TYPE, assembleEventCallback(block))
    }

    /**
     * Handle each event using listener. Object is raw JSON
     */
    fun onEachEvent(listener: EventCallback<RawEvent>) {
        botsLongPollApi.registerListener(RawEvent.TYPE, listener)
    }

    /**
     * Handle each event using listener. Object is raw JSON
     */
    fun onEachEvent(block: (RawEvent) -> Unit) {
        botsLongPollApi.registerListener(RawEvent.TYPE, assembleEventCallback(block))
    }

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     */
    fun sendMessage(message: Message): VkRequest {
        if (type == Type.User) {
            throw UnsupportedActionException()
        }

        return api.call("messages.send", message.buildParams(json))
    }

    /**
     * Send message. Not available for [VkApiClient.Type.User]
     */
    fun sendMessage(block: Message.() -> Unit): VkRequest {
        if (type == Type.User) {
            throw UnsupportedActionException()
        }

        val message = Message()
        block(message)
        return sendMessage(message)
    }

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @return API request wrapper
     */
    @JvmOverloads
    fun call(method: String, params: Parameters = Parameters()): VkRequest {
        return api.call(method, params)
    }

    /**
     * Call some API method and receive the response string
     */
    @JvmOverloads
    fun call(method: String, params: Parameters = Parameters(), batch: Boolean = true, callback: Callback<JsonElement>) {
        if (batch) {
            call(BatchRequestItem(call(method, params), callback))
        } else {
            api.call(method, params).enqueue(object : Callback<String> {
                override fun onResult(result: String) = callback.onResult(json.parseJson(result))
                override fun onError(error: Exception) = callback.onError(error)
            })
        }
    }

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @param batch If true, request will be put into execute queue, [https://vk.com/dev/execute]
     * @param onResult Successful result callback
     * @param onError Error callback
     */
    @JvmOverloads
    fun call(
        method: String,
        params: Parameters = Parameters(),
        batch: Boolean = true,
        onResult: (JsonElement) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        call(method, params, batch, assembleCallback(onResult, onError))
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request API request wrapper
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     * @param callback Callback to handle the result or an error
     */
    @JvmOverloads
    fun call(request: VkRequest, batch: Boolean = true, callback: Callback<JsonElement>) {
        call(request.method, request.params, batch, callback)
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request API request wrapper
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     * @param onResult Successful result callback
     * @param onError Error callback
     */
    @JvmOverloads
    fun call(
        request: VkRequest,
        batch: Boolean = true,
        onResult: (JsonElement) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        call(request, batch, assembleCallback(onResult, onError))
    }

    /**
     * Put some API call into queue
     */
    fun call(item: BatchRequestItem) {
        batchRequestExecutor.enqueue(item)
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param items Wrappers of the requests and callbacks
     */
    fun call(vararg items: BatchRequestItem) {
        call(items.toList())
    }

    /**
     * Put some API call into queue
     */
    fun call(items: Collection<BatchRequestItem>) {
        batchRequestExecutor.enqueue(items)
    }

    /**
     * Type of VK client
     *
     * @property User Usual user, can't use messages API
     * @property Community Community (group or public community)
     */
    enum class Type {
        User, Community
    }

    data class AppInfo(
        val clientId: Int,
        val clientSecret: String,
        val redirectUri: String = DEFAULT_REDIRECT_URI
    ) {
        companion object {
            const val KEY_CLIENT_ID = "client_id"
            const val KEY_CLIENT_SECRET = "client_secret"
            const val KEY_REDIRECT_URI = "redirect_uri"
        }
    }

    companion object {
        private const val ACCESS_TOKEN_URL = "https://oauth.vk.com/access_token"
        private const val KEY_CODE = "code"
        const val DEFAULT_REDIRECT_URI = "https://oauth.vk.com/blank.html"

        /**
         * Useful method for server-side operations.
         * Receive the code from the client and do something on his behalf.
         *
         * @param code VK code, https://vk.com/dev/authcode_flow_user
         * @param app Information about your app (client)
         * @param settings VK API Client settings
         * @return VK API client.
         */
        @OptIn(ExperimentalStdlibApi::class)
        @JvmStatic
        fun fromCode(code: String, app: AppInfo, settings: VkSettings): VkApiClient {
            val query = paramsOf(
                AppInfo.KEY_CLIENT_ID to app.clientId,
                AppInfo.KEY_CLIENT_SECRET to app.clientSecret,
                AppInfo.KEY_REDIRECT_URI to app.redirectUri,
                KEY_CODE to code
            ).buildQuery()

            val tokenResponse = settings.httpClient.getSync("$ACCESS_TOKEN_URL?$query")?.let {
                defaultJson().parse(VkAccessTokenResponse.serializer(), it.bodyString())
            } ?: throw VkSdkInitiationException("VK Client from code")

            return VkApiClient(
                id = tokenResponse.userId
                    ?: throw VkResponseException("VK access_token from code error: user_id is null"),
                token = tokenResponse.accessToken
                    ?: throw VkResponseException("VK access_token from code error: access_token is null"),
                type = Type.User,
                settings = settings
            )
        }
    }
}

