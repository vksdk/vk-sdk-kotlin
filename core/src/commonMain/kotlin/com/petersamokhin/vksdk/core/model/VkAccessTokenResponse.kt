package com.petersamokhin.vksdk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VK access token response e.g. for receiving the code
 */
@Serializable
public data class VkAccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
    @SerialName("user_id")
    val userId: Int? = null,
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null
)