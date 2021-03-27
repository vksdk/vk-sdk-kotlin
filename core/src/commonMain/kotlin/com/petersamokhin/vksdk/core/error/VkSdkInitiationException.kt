package com.petersamokhin.vksdk.core.error

/**
 * Some error when some SDK part is not initiated
 *
 * @property message Error message
 * @property cause Error cause
 */
public class VkSdkInitiationException(
    source: String = "Some SDK part",
    override val message: String? = "$source $ERROR_MESSAGE",
    override val cause: Throwable? = null
): VkException(message, cause) {
    public companion object {
        private const val ERROR_MESSAGE = "is not initiated or disabled, already cancelled or not active"
    }
}