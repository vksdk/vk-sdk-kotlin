package com.petersamokhin.vksdk.core.client

import com.petersamokhin.vksdk.core.api.BatchRequestItem
import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.model.VkSettings
import kotlinx.serialization.json.JsonElement

/**
 * VK API Client
 *
 * @property id Client (user or community) ID
 * @param token VK access token: https://vk.com/dev/access_token
 * @property type Client type, because some APIs can be used only by the user/community
 * @param settings Client settings, such as an API version
 */
expect class VkApiClient(
    id: Int,
    token: String,
    type: Type,
    settings: VkSettings
): VkApiClientCommon {

    /**
     * Start the long polling, [https://vk.com/dev/bots_longpoll](documentation).
     * Messages API for regular users is restricted.
     * @see [https://vk.com/dev/using_longpoll](this) and [https://vk.com/dev/messages_api](this)
     */
    fun startLongPolling(restart: Boolean = false, settings: VkBotsLongPollApi.Settings = VkBotsLongPollApi.Settings())

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @return API request wrapper
     */
    fun call(method: String, params: Parameters = Parameters()): VkRequest

    /**
     * Call some API method and receive the response string
     *
     * @param method API method, e.g. `users.get`
     * @param params Parameters, e.g. `user_id`: `Parameters.of("user_id", 1)`
     * @param batch If true, request will be put into execute queue, [https://vk.com/dev/execute]
     */
    fun call(method: String, params: Parameters = Parameters(), batch: Boolean = true, callback: Callback<JsonElement>)

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param request API request wrapper
     * @param batch If true, request will be put into execute queue, https://vk.com/dev/execute
     * @param callback Callback to handle the result or an error
     */
    fun call(request: VkRequest, batch: Boolean = true, callback: Callback<JsonElement>)

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param item Wrapper of the request and callback
     */
    fun call(item: BatchRequestItem)

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param items Wrappers of the requests and callbacks
     */
    fun call(vararg items: BatchRequestItem)

    /**
     * Call some API method and receive the response string, parsed into JsonElement
     *
     * @param items Wrappers of the requests and callbacks
     */
    fun call(items: Collection<BatchRequestItem>)

    /**
     * Type of VK client
     *
     * @property User Usual user, can't use messages API
     * @property Community Community (group or public community)
     */
    enum class Type {
        User, Community
    }
}