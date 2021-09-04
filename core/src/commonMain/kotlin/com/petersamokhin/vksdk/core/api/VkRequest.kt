package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.Parameters

/**
 * Network request to the VK API
 *
 * @property method Method, e.g. `users.get`
 * @property params Call params, e.g. from [Parameters.of]: `Parameters.of("user_id", "1")`
 */
public data class VkRequest(
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
    public suspend fun execute(): String {
        val result = httpClient.post(
            url = "${VkApi.BASE_URL}/$method",
            body = params.buildQuery().encodeToByteArray(),
            bodyContentType = ContentType.FormUrlEncoded
        )

        if (result.isSuccessful() && result.body != null) {
            return result.bodyString()
        } else {
            throw VkResponseException()
        }
    }
}