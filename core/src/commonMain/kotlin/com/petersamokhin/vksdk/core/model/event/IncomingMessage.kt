package com.petersamokhin.vksdk.core.model.event

import com.petersamokhin.vksdk.core.api.VkApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * VK incoming message object, e.g. for Long Poll events
 *
 * [https://vk.com/dev/objects/message]
 */
@Serializable
public data class IncomingMessage(
    val id: Int,
    val date: Int,
    @SerialName("peer_id")
    val peerId: Int,
    @SerialName("from_id")
    val fromId: Int,
    val text: String,
    @SerialName("random_id")
    val randomId: Int,
    val ref: String? = null,
    @SerialName("ref_source")
    val refSource: String? = null,
    val attachments: List<JsonElement>,
    val important: Boolean,
    val geo: Geo? = null,
    val payload: String? = null,
    val keyboard: Keyboard? = null,
    @SerialName("fwd_messages")
    val fwdMessages: List<MessagePartial>,
    @SerialName("reply_message")
    val replyMessage: JsonElement? = null,
    val action: Action? = null,
    @SerialName("is_hidden")
    val isHidden: Boolean? = null
) {
    @Serializable
    public data class Action(
        val type: String,
        @SerialName("member_id")
        val memberId: Int? = null,
        val text: String? = null,
        val email: String? = null,
        val photo: ChatPhoto? = null
    ) {
        @Suppress("unused")
        public enum class Type {
            CHAT_PHOTO_UPDATE,
            CHAT_PHOTO_REMOVE,
            CHAT_CREATE,
            CHAT_TITLE_UPDATE,
            CHAT_KICK_USER,
            CHAT_PIN_MESSAGE,
            CHAT_UNPIN_MESSAGE,
            CHAT_INVITE_USER_BY_LINK
        }

        @Serializable
        public data class ChatPhoto(
            @SerialName("photo_50")
            val photo50: String? = null,
            @SerialName("photo_100")
            val photo100: String? = null,
            @SerialName("photo_200")
            val photo200: String? = null
        )
    }

    @Serializable
    public data class Keyboard(
        @SerialName("one_time")
        val oneTime: Boolean,
        val inline: Boolean,
        val buttons: List<Button>
    ) {
        @Serializable
        public data class Button(
            val action: JsonElement? = null,
            val color: String
        ) {
            @Suppress("unused")
            public enum class Color {
                PRIMARY,
                SECONDARY,
                NEGATIVE,
                POSITIVE
            }
        }
    }

    @Serializable
    public data class Geo(
        val type: String,
        val coordinates: List<Double>? = null,
        val place: Place? = null
    ) {
        @Serializable
        public data class Place(
            val id: Int? = null,
            val title: String? = null,
            val latitude: Double? = null,
            val longitude: Double? = null,
            val created: Int? = null,
            val icon: String? = null,
            val country: String? = null,
            val city: String? = null
        )
    }

    public fun isFromChat(): Boolean =
        peerId > VkApi.CHAT_ID_PREFIX
}

/**
 * Forwarded messages, edited messages, etc
 */
@Serializable
public data class MessagePartial(
    val id: Int,
    val date: Int,
    @SerialName("from_id")
    val fromId: Int,
    @SerialName("random_id")
    val randomId: Int? = null,
    val text: String,
    val attachments: List<JsonElement>,
    @SerialName("conversation_message_id")
    val conversationMessageId: Int,
    @SerialName("peer_id")
    val peerId: Int,
    val out: Int? = null,
    @SerialName("update_time")
    val updateTime: Int? = null
)