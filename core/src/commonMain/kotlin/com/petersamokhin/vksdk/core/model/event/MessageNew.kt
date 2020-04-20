package com.petersamokhin.vksdk.core.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageNew(
    val message: IncomingMessage,
    @SerialName("client_info")
    val clientInfo: ClientInfo
) {
    @Serializable
    data class ClientInfo(
        @SerialName("button_actions")
        val buttonActions: List<String>? = null,
        val keyboard: Boolean? = null,
        val carousel: Boolean? = null,
        @SerialName("inline_keyboard")
        val inlineKeyboard: Boolean? = null,
        @SerialName("lang_id")
        val langId: Int? = null
    )

    companion object {
        const val TYPE = "message_new"
    }
}