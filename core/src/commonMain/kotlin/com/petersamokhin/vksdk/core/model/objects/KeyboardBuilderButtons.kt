package com.petersamokhin.vksdk.core.model.objects

@KeyboardDslMarker
public class TextButton(
    private val label: String,
    private val color: Keyboard.Button.Color,
    private var payload: String?
) {
    internal fun build(): Keyboard.Button = Keyboard.Button(
        action = Keyboard.Button.Action(
            type = Keyboard.Button.Action.Type.TEXT,
            label = label,
            payload = payload
        ),
        color = color
    )
}

@KeyboardDslMarker
public class LocationButton(
    private var payload: String?
) {
    internal fun build(): Keyboard.Button = Keyboard.Button(
        action = Keyboard.Button.Action(
            type = Keyboard.Button.Action.Type.LOCATION,
            payload = payload
        )
    )
}

@KeyboardDslMarker
public class VkPayButton(
    private val hash: String,
    private var payload: String?
) {
    internal fun build(): Keyboard.Button = Keyboard.Button(
        action = Keyboard.Button.Action(
            type = Keyboard.Button.Action.Type.VK_PAY,
            payload = payload,
            hash = hash
        )
    )
}

@KeyboardDslMarker
public class VkAppsButton(
    private val label: String,
    private val appId: Int,
    private var ownerId: Int?,
    private var hash: String?,
    private var payload: String?
) {
    internal fun build(): Keyboard.Button = Keyboard.Button(
        action = Keyboard.Button.Action(
            type = Keyboard.Button.Action.Type.VK_APPS,
            label = label,
            appId = appId,
            ownerId = ownerId,
            payload = payload,
            hash = hash
        )
    )
}

@KeyboardDslMarker
public class OpenLinkButton(
    private val label: String,
    private val link: String,
    private var payload: String?
) {
    internal fun build(): Keyboard.Button = Keyboard.Button(
        action = Keyboard.Button.Action(
            type = Keyboard.Button.Action.Type.OPEN_LINK,
            label = label,
            link = link,
            payload = payload
        )
    )
}