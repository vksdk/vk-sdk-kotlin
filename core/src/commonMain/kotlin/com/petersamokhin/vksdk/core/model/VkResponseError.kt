package com.petersamokhin.vksdk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Default error when calling VK API
 *
 * @property errorCode Error code, https://vk.com/dev/errors
 * @property errorMsg Error message
 */
@Serializable
public data class VkResponseError(
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null
)