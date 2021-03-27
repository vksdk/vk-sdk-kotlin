package com.petersamokhin.vksdk.core.error

/**
 * Some error when called type of functionality is currently not supported
 *
 * @property message Error message
 * @property cause Error cause
 */
public class UnsupportedActionException(
    override val message: String? = ERROR_MESSAGE,
    override val cause: Throwable? = null
) : VkException(message = message) {
    public companion object {
        private const val ERROR_MESSAGE = "This type of functionality is not supported by this type of client, or with this API version, etc."
    }
}