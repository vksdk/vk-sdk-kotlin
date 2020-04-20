package com.petersamokhin.vksdk.core.model.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageCarouselTemplate(
    val elements: List<Element>
) {
    var type = "carousel"

    @Serializable
    data class Element(
        val title: String? = null,
        val description: String? = null,
        @SerialName("photo_id")
        val photoId: String? = null,
        val buttons: List<Keyboard.Button>? = null,
        val action: Keyboard.Button.Action? = null
    )
}