package com.petersamokhin.vksdk.core.error

/**
 * Base error
 *
 * @property message Error message
 * @property cause Error cause
 */
public open class VkException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)