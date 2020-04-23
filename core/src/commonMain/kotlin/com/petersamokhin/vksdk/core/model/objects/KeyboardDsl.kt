@file:Suppress("unused")

package com.petersamokhin.vksdk.core.model.objects

@DslMarker
annotation class KeyboardDslMarker

/**
 * Use keyboard DSL to build the keyboard
 */
fun keyboard(
    oneTime: Boolean = false,
    builder: KeyboardDslBuilder.() -> Unit
): Keyboard = Keyboard(
    oneTime = oneTime,
    buttons = KeyboardDslBuilder().apply(builder).rows
)

/**
 * Use keyboard DSL to build the keyboard
 */
fun inlineKeyboard(
    builder: KeyboardDslBuilder.() -> Unit
): Keyboard = Keyboard(
    inline = true,
    buttons = KeyboardDslBuilder().apply(builder).rows
)

/**
 * [https://vk.com/dev/bots_docs_3]
 */
@KeyboardDslMarker
class KeyboardDslBuilder {
    internal val rows: MutableList<List<Keyboard.Button>> = mutableListOf()

    fun row(block: RowDslBuilder.() -> Unit) {
        rows += RowDslBuilder().apply(block).buttons
    }

    fun locationButton(payload: String? = null, block: LocationButton.() -> Unit = { }) {
        rows += listOf(LocationButton(payload).apply(block).build())
    }

    fun vkPayButton(hash: String, payload: String? = null, block: VkPayButton.() -> Unit = { }) {
        rows += listOf(VkPayButton(hash, payload).apply(block).build())
    }

    fun vkAppButton(
        label: String,
        appId: Int,
        ownerId: Int? = null,
        hash: String? = null,
        payload: String? = null,
        block: VkAppsButton.() -> Unit = { }
    ) {
        rows += listOf(VkAppsButton(label, appId, ownerId, hash, payload).apply(block).build())
    }

    fun openLinkButton(
        label: String,
        link: String,
        payload: String? = null,
        block: OpenLinkButton.() -> Unit = { }
    ) {
        rows += listOf(OpenLinkButton(label, link, payload).apply(block).build())
    }
}

@KeyboardDslMarker
class RowDslBuilder {
    internal val buttons: MutableList<Keyboard.Button> = mutableListOf()

    fun primaryButton(label: String, payload: String? = null, block: TextButton.() -> Unit = { }) {
        addButton(label, payload, block, Keyboard.Button.Color.PRIMARY)
    }

    fun secondaryButton(label: String, payload: String? = null, block: TextButton.() -> Unit = { }) {
        addButton(label, payload, block, Keyboard.Button.Color.SECONDARY)
    }

    fun negativeButton(label: String, payload: String? = null, block: TextButton.() -> Unit = { }) {
        addButton(label, payload, block, Keyboard.Button.Color.NEGATIVE)
    }

    fun positiveButton(label: String, payload: String? = null, block: TextButton.() -> Unit = { }) {
        addButton(label, payload, block, Keyboard.Button.Color.POSITIVE)
    }

    private fun addButton(label: String, payload: String?, block: TextButton.() -> Unit, color: Keyboard.Button.Color) {
        buttons += TextButton(label, color, payload).apply(block).build()
    }
}