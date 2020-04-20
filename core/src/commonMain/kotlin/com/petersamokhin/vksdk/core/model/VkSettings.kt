package com.petersamokhin.vksdk.core.model

import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.Parameters

/**
 * Settings
 *
 * @property httpClient HTTP client to make network calls
 * @property apiVersion VK API version, https://vk.com/dev/versions
 * @property defaultParams Default query params to be applied to every API call, e.g. `lang`
 * @property maxExecuteRequestsPerSecond Provide [VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED] as a value to disable using of this. Make up to 25 VK API calls with only one network call, https://vk.com/dev/execute.
 *
 * @author Peter Samokhin, https://petersamokhin.com
 */
expect class VkSettings(
    httpClient: HttpClient,
    apiVersion: Double = VkApi.DEFAULT_VERSION,
    defaultParams: Parameters = Parameters(),
    maxExecuteRequestsPerSecond: Int = VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED
) {
    val httpClient: HttpClient
    val defaultParams: Parameters
    val maxExecuteRequestsPerSecond: Int
    val apiVersion: Double
}