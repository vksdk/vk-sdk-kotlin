package com.petersamokhin.vksdk.core.error

/**
 * Some error during the VK response parsing or handling
 *
 * @property message Error message
 * @property cause Error cause
 */
class VkResponseException(
    method: String = "Some method",
    override val message: String? = "$method: $ERROR_MESSAGE",
    override val cause: Throwable? = null
): VkException(message, cause) {
    companion object {
        private const val ERROR_MESSAGE = "Error occurred during the VK response parsing or handling"
    }
}