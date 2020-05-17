package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.callback.Callback
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
class VkApi internal constructor(
    private val httpClient: HttpClient,
    private val version: Double,
    private val token: String,
    private val defaultParams: Parameters = Parameters()
) {
    /**
     * Call some API method and receive the response string
     */
    fun call(method: String, params: Parameters = Parameters()): VkRequest {
        return VkRequest(method, params.applyRequired(), httpClient)
    }

    /**
     * Upload whatever you want wherever you can
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        json: Json,
        params: Parameters,
        items: List<UploadableContent>
    ): String {
        val uploadUrl = call(methodGetUploadUrl, params)
            .execute().let {
                json.parseJson(it).jsonObjectOrNullSafe?.get("response")?.jsonObjectOrNullSafe?.get("upload_url")?.contentOrNullSafe
            } ?: throw VkResponseException(methodGetUploadUrl)

        val uploadResult = httpClient.postMultipartSync(
            uploadUrl,
            items
        )?.let {
            if (it.isSuccessful()) {
                it.bodyString().let { responseString ->
                    json.parseJson(responseString).jsonObjectOrNullSafe
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
            methodSave, saveParams
        ).execute()
    }

    /**
     * Upload whatever you want wherever you can
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        json: Json,
        params: Parameters,
        items: List<UploadableContent>,
        callback: Callback<String>
    ) {
        call(methodGetUploadUrl, params)
            .enqueue(object: Callback<String> {
                override fun onResult(result: String) {
                    val uploadUrl = json.parseJson(result).jsonObjectOrNullSafe?.get("response")?.jsonObjectOrNullSafe?.get("upload_url")?.contentOrNullSafe
                        ?: throw VkResponseException(methodGetUploadUrl)

                    httpClient.postMultipart(
                        uploadUrl,
                        items,
                        object: Callback<Response> {
                            override fun onResult(result: Response) {
                                val uploadResult = result.let {
                                    if (it.isSuccessful()) {
                                        it.bodyString().let { responseString ->
                                            json.parseJson(responseString).jsonObjectOrNullSafe
                                        }
                                    } else {
                                        null
                                    }
                                } ?: throw VkResponseException("$methodGetUploadUrl -> upload")

                                val saveParams = Parameters()

                                uploadResult.keys.forEach { key ->
                                    saveParams.put(key, uploadResult[key]?.contentOrNullSafe)
                                }

                                call(methodSave, saveParams).enqueue(callback)
                            }

                            override fun onError(error: Exception) = callback.onError(error)
                        }
                    )
                }

                override fun onError(error: Exception) = callback.onError(error)
            })
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal fun getLongPollUpdates(
        serverInfo: VkLongPollServerResponse,
        wait: Int
    ): Response? {
        return httpClient.getSync("${serverInfo.server}?act=a_check&key=${serverInfo.key}&ts=${serverInfo.ts}&wait=$wait")
    }

    private fun Parameters.applyRequired() = apply {
        put(ParametersKeys.ACCESS_TOKEN, token)
        put(ParametersKeys.VERSION, version)
        putAll(defaultParams)
    }

    /**
     * Basic parameters keys
     *
     * @property VERSION API version
     * @property ACCESS_TOKEN Access token
     */
    object ParametersKeys {
        const val VERSION = "v"
        const val ACCESS_TOKEN = "access_token"
    }

    /**
     * @property DEFAULT_VERSION Default VK API version
     * @property BASE_URL Base VK API URL
     * @property API_CALL_INTERVAL Base time to divide by the count of requests per second
     * @property EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED Constant value to disable batchc requests executor
     * @property CHAT_ID_PREFIX Chat ID prefix constant value
     */
    @Suppress("unused")
    companion object {
        const val DEFAULT_VERSION = 5.103
        const val BASE_URL = "https://api.vk.com/method"

        internal const val API_CALL_INTERVAL = 1133L
        const val EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED = -1

        const val CHAT_ID_PREFIX = 2000000000
    }
}