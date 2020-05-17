package com.petersamokhin.vksdk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VK access token response e.g. for receiving the code
 */
@Serializable
data class VkAccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String?,
    @SerialName("expires_in")
    val expiresIn: Int?,
    @SerialName("user_id")
    val userId: Int?
)