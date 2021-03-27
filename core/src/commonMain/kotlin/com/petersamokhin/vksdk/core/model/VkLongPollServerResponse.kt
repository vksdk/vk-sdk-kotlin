package com.petersamokhin.vksdk.core.model

import kotlinx.serialization.Serializable

/**
 * VK Long Poll server info for the first call, or after an error.
 *
 * @property key Required parameter
 * @property server Required parameter
 * @property ts Required parameter
 */
@Serializable
public data class VkLongPollServerResponse(
    val key: String,
    val server: String,
    val ts: String
)