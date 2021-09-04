package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.api.VkApi.ParametersKeys.ACCESS_TOKEN
import com.petersamokhin.vksdk.core.api.VkApi.ParametersKeys.VERSION
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.Response
import com.petersamokhin.vksdk.core.model.VkLongPollServerResponse
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.core.utils.contentOrNullSafe
import com.petersamokhin.vksdk.core.utils.jsonObjectOrNullSafe
import kotlinx.serialization.json.Json

/**
 * Class for interaction with the VK API
 */
public class VkApi internal constructor(
    private val httpClient: HttpClient,
    private val version: String,
    private val token: String,
    private val defaultParams: Parameters = Parameters()
) {
    /**
     * Call some API method and receive the response string
     */
    public fun call(method: String, params: Parameters = Parameters()): VkRequest =
        VkRequest(method, params.applyRequired(), httpClient)

    /**
     * Upload whatever you want wherever you can
     */
    internal suspend fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        json: Json,
        params: Parameters,
        items: List<UploadableContent>
    ): String {
        val uploadUrl = call(methodGetUploadUrl, params)
            .execute().let {
                json.parseToJsonElement(it).jsonObjectOrNullSafe
                    ?.get("response")?.jsonObjectOrNullSafe
                    ?.get("upload_url")?.contentOrNullSafe
            } ?: throw VkResponseException(methodGetUploadUrl)

        val uploadResult = httpClient.postMultipart(
            uploadUrl = uploadUrl,
            items = items
        ).let {
            if (it.isSuccessful()) {
                it.bodyString().let { responseString ->
                    json.parseToJsonElement(responseString).jsonObjectOrNullSafe
                }
            } else {
                null
            }
        } ?: throw VkResponseException("$methodGetUploadUrl -> upload")

        val saveParams = Parameters()

        uploadResult.keys.forEach { key ->
            saveParams.put(key, uploadResult[key]?.contentOrNullSafe)
        }

        return call(
            method = methodSave,
            params = saveParams
        ).execute()
    }

    internal suspend fun getLongPollUpdates(
        serverInfo: VkLongPollServerResponse,
        wait: Int
    ): Response =
        httpClient.get("${serverInfo.server}?act=a_check&key=${serverInfo.key}&ts=${serverInfo.ts}&wait=$wait")

    private fun Parameters.applyRequired() = apply {
        put(ACCESS_TOKEN, token)
        put(VERSION, version)
        putAll(defaultParams)
    }

    /**
     * Basic parameters keys
     *
     * @property VERSION API version
     * @property ACCESS_TOKEN Access token
     */
    public object ParametersKeys {
        public const val VERSION: String = "v"
        public const val ACCESS_TOKEN: String = "access_token"
    }

    /**
     * @property DEFAULT_VERSION Default VK API version
     * @property BASE_URL Base VK API URL
     * @property API_CALL_INTERVAL Base time to divide by the count of requests per second
     * @property EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED Constant value to disable batchc requests executor
     * @property CHAT_ID_PREFIX Chat ID prefix constant value
     */
    @Suppress("unused")
    public companion object {
        internal const val API_CALL_INTERVAL = 1133L

        /**
         * https://vk.com/dev/versions
         *
         * [String] because `5.13` and `5.130` are the different versions
         */
        public const val DEFAULT_VERSION: String = "5.131"
        public const val BASE_URL: String = "https://api.vk.com/method"

        public const val EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED: Int = -1

        public const val CHAT_ID_PREFIX: Int = 2000000000
    }
}