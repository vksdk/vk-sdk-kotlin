package com.petersamokhin.vksdk.core.http

import com.petersamokhin.vksdk.core.error.VkResponseException

/**
 * Minimal required information about the HTTP request response.
 *
 * @property code HTTP response code
 * @property body [ByteArray] content of the HTTP response
 */
data class Response(
    val code: Int,
    val body: ByteArray?
) {

    /**
     * Response body string
     *
     * @throws VkResponseException If body is null
     * @return String if response is successful
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun bodyString(): String {
        return body?.decodeToString() ?: throw VkResponseException()
    }

    /**
     * Is this response successful based on the HTTP code
     *
     * @return True if [code] == [HTTP_CODE_OK]
     */
    fun isSuccessful() = code == HTTP_CODE_OK

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Response) return false

        if (code != other.code) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }


    companion object {
        /**
         * OK http code constant
         */
        const val HTTP_CODE_OK = 200
    }
}