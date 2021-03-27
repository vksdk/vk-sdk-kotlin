package com.petersamokhin.vksdk.core.model.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://vk.com/dev/bots_docs_3
 */
@Serializable
public data class Keyboard(
    val buttons: List<List<Button>> = emptyList(),
    @SerialName("one_time")
    val oneTime: Boolean = false,
    val inline: Boolean = false,
    @SerialName("author_id")
    val authorId: Int? = null
) {
    @Serializable
    public data class Button(
        val action: Action,
        val color: Color? = null
    ) {
        @Serializable
        public enum class Color {
            @SerialName("primary")
            PRIMARY,
            @SerialName("secondary")
            SECONDARY,
            @SerialName("negative")
            NEGATIVE,
            @SerialName("positive")
            POSITIVE
        }

        @Serializable
        public data class Action(
            val type: Type,
            val label: String? = null,
            val payload: String? = null,
            val hash: String? = null,
            val link: String? = null,
            @SerialName("app_id")
            val appId: Int? = null,
            @SerialName("owner_id")
            val ownerId: Int? = null
        ) {
            @Serializable
            public enum class Type {
                @SerialName("text")
                TEXT,
                @SerialName("open_link")
                OPEN_LINK,
                @SerialName("location")
                LOCATION,
                @SerialName("vk_pay")
                VK_PAY,
                @SerialName("vk_apps")
                VK_APPS,
                @SerialName("open_photo")
                OPEN_PHOTO
            }
        }
    }
}