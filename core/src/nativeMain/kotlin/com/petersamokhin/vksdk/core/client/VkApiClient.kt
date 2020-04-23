package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.BatchRequestItem
import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.utils.assembleCallback
import kotlinx.serialization.json.JsonElement

/**
 * VK API Client
 *
 * @property id Client (user or community) ID
 * @param token VK access token: https://vk.com/dev/access_token
 * @property type Client type, because some APIs can be used only by the user/community
 * @param settings Client settings, such as an API version
 */
actual class VkApiClient actual constructor(
    id: Int,
    token: String,
    type: Type,
    settings: VkSettings
) : VkApiClientCommon(id, token, type, settings) {
    private val flowsWrapper = VkApiClientFlows(this, settings.backgroundDispatcher)

    /**
     * Coroutines Flow wrapper
     */
    override fun flows() = flowsWrapper

    /**
     * Start the long polling, [https://vk.com/dev/bots_longpoll](documentation).
     * Messages API for regular users is restricted.
     * @see [https://vk.com/dev/using_longpoll](this) and [https://vk.com/dev/messages_api](this)
     */
    actual fun startLongPolling(restart: Boolean, settings: VkBotsLongPollApi.Settings) {
        super.startLongPollingCommon(restart, settings)
    }

    /**
     * Call some API method and receive the response string
     */
    actual fun call(method: String, params: Parameters): VkRequest {
        return super.callCommon(method, params)
    }

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     */
    actual fun call(
        method: String,
        params: Parameters,
        batch: Boolean,
        callback: Callback<JsonElement>
    ) {
        if (batch) {
            super.callBatchCommon(BatchRequestItem(call(method, params), callback))
        } else {
            super.callCommon(method, params, callback)
        }
    }

    /**
     * Call some API method and receive the response string, parsed into [JsonElement]
     *
     * @param request API request wrapper
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     * @param callback Callback to handle the result or an error
     */
    actual fun call(
        request: VkRequest,
        batch: Boolean,
        callback: Callback<JsonElement>
    ) {
        if (batch) {
            super.callBatchCommon(BatchRequestItem(request, callback))
        } else {
            super.callCommon(request.method, request.params, callback)
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
    actual fun call(
        method: String,
        params: Parameters,
        batch: Boolean,
        onResult: (JsonElement) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (batch) {
            super.callBatchCommon(BatchRequestItem(call(method, params), assembleCallback(onResult, onError)))
        } else {
            super.callCommon(method, params, assembleCallback(onResult, onError))
        }
    }

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request API request wrapper
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     * @param onResult Successful result callback
     * @param onError Error callback
     */
    actual fun call(
        request: VkRequest,
        batch: Boolean,
        onResult: (JsonElement) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (batch) {
            super.callBatchCommon(BatchRequestItem(request, assembleCallback(onResult, onError)))
        } else {
            super.callCommon(request.method, request.params, assembleCallback(onResult, onError))
        }
    }

    /**
     * Call some API method and receive the response string, parsed into [JsonElement]
     *
     * @param item Wrapper of the request and callback
     */
    actual fun call(item: BatchRequestItem) {
        super.callBatchCommon(item)
    }

    /**
     * Call some API method and receive the response string, parsed into [JsonElement]
     *
     * @param items Wrappers of the requests and callbacks
     */
    actual fun call(vararg items: BatchRequestItem) {
        super.callBatchCommon(items.toList())
    }

    /**
     * Call some API method and receive the response string, parsed into [JsonElement]
     *
     * @param items Wrappers of the requests and callbacks
     */
    actual fun call(items: Collection<BatchRequestItem>) {
        super.callBatchCommon(items)
    }

    /**
     * Type of VK client
     *
     * @property User Usual user, can't use messages API
     * @property Community Community (group or public community)
     */
    actual enum class Type {
        User, Community
    }
}