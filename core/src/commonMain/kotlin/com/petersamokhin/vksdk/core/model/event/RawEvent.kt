package com.petersamokhin.vksdk.core.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RawEvent(
    val type: String,
    val `object`: JsonObject,
    @SerialName("group_id")
    val groupId: Int
) {
    companion object {
        const val TYPE = "VK_KOTLIN_SDK_RAW_LONG_POLL_EVENT"
    }
}