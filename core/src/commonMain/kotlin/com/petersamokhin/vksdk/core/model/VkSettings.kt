package com.petersamokhin.vksdk.core.model

import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.utils.defaultJson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmOverloads

/**
 * Settings
 *
 * @property httpClient HTTP client to make network calls
 * @property apiVersion VK API version, https://vk.com/dev/versions
 * @property defaultParams Default query params to be applied to every API call, e.g. `lang`
 * @property maxExecuteRequestsPerSecond Provide [VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED] as a value to disable using of this. Make up to 25 VK API calls with only one network call, https://vk.com/dev/execute.
 * @property backgroundDispatcher Coroutine dispatcher to run tasks in a background
 *
 * @author Peter Samokhin, https://petersamokhin.com
 */
public data class VkSettings @JvmOverloads constructor(
    val httpClient: HttpClient,
    val apiVersion: String = VkApi.DEFAULT_VERSION,
    val defaultParams: Parameters = Parameters(),
    val maxExecuteRequestsPerSecond: Int = 3,
    val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val json: Json = defaultJson()
) {
    /**
     * Another constructor for using `Double` API version.
     * Be careful as e.g. `5.13` and `5.130` are the different versions.
     */
    public constructor(
        httpClient: HttpClient,
        apiVersion: Double,
        defaultParams: Parameters = Parameters(),
        maxExecuteRequestsPerSecond: Int = 3,
        backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
        json: Json = defaultJson()
    ): this(httpClient, apiVersion.toString(), defaultParams, maxExecuteRequestsPerSecond, backgroundDispatcher, json)
}