package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.Response
import com.petersamokhin.vksdk.core.utils.assembleCallback

/**
 * Network request to the VK API
 *
 * @property method Method, e.g. `users.get`
 * @property params Call params, e.g. from [Parameters.of]: `Parameters.of("user_id", "1")`
 */
data class VkRequest(
    val method: String,
    val params: Parameters,
    private val httpClient: HttpClient
) {
    /**
     * Execute request synchronously
     *
     * @return Plain response text
     * @throws VkException If some error occurred, then exception is thrown
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun execute(): String {
        val result = httpClient.postSync(
            "${VkApi.BASE_URL}/$method",
            params.buildQuery().encodeToByteArray(),
            ContentType.FormUrlEncoded
        )

        if (result?.isSuccessful() == true && result.body != null) {
            return result.body.decodeToString()
        } else {
            throw VkResponseException()
        }
    }

    /**
     * Execute request asynchronously
     *
     * @param callback Get plain response text or an error
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun enqueue(callback: Callback<String>) {
        httpClient.post(
            "${VkApi.BASE_URL}/$method",
            params.buildQuery().encodeToByteArray(),
            ContentType.FormUrlEncoded,
            object : Callback<Response> {
                override fun onResult(result: Response) {
                    if (result.isSuccessful() && result.body != null) {
                        callback.onResult(result.body.decodeToString())
                    } else {
                        callback.onError(VkResponseException())
                    }
                }

                override fun onError(error: Exception) = callback.onError(VkException(cause = error))
            }
        )
    }

    /**
     * Execute request asynchronously
     *
     * @param onResult Successful result callback
     * @param onError Error callback
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun enqueue(onResult: (String) -> Unit = {}, onError: (Exception) -> Unit = {}) {
        enqueue(assembleCallback(onResult, onError))
    }
}