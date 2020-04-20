@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.petersamokhin.vksdk.core.model.objects

import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.utils.intValue
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import kotlin.random.Random

/**
 * Message to be sent
 *
 * [https://vk.com/dev/messages.send]
 */
class Message(
    var userId: Int? = null,
    var peerId: Int? = null,
    var domain: String? = null,
    var chatId: String? = null,
    var userIds: String? = null,
    var randomId: Long? = null,
    var message: String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var attachment: String? = null,
    var replyTo: Int? = null,
    var forwardMessages: String? = null,
    var stickerId: Int? = null,
    var groupId: Int? = null,
    var keyboard: Keyboard? = null,
    var payload: String? = null,
    var dontParseLinks: Int? = null,
    var disableMentions: Int? = null,
    var intent: String? = null,
    val template: MessageCarouselTemplate? = null
) {
    fun sendFrom(client: VkApiClient): VkRequest {
        return client.sendMessage(this)
    }

    fun userIds(vararg ids: Int): Message {
        userIds = ids.joinToString(",")
        return this
    }

    fun userIds(vararg ids: String): Message {
        userIds = ids.joinToString(",")
        return this
    }

    fun userIds(ids: Iterable<Int>): Message {
        userIds = ids.joinToString(",")
        return this
    }

    fun forwardMessages(vararg ids: Int): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    fun forwardMessages(vararg ids: String): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    fun forwardMessages(ids: Iterable<Int>): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    fun attachment(items: Iterable<String>): Message {
        attachment = items.joinToString(",")
        return this
    }

    fun dontParseLinks(value: Boolean): Message {
        dontParseLinks = value.intValue
        return this
    }

    fun disableMentions(value: Boolean): Message {
        disableMentions = value.intValue
        return this
    }

    @OptIn(ImplicitReflectionSerializer::class)
    internal fun buildParams(json: Json) = Parameters().apply {
        userId?.also { put("user_id", it) }
        peerId?.also { put("peer_id", it) }
        domain?.also { put("domain", it) }
        chatId?.also { put("chat_id", it) }
        userIds?.also { put("user_ids", it) }
        message?.also { put("message", it) }
        lat?.also { put("lat", it) }
        long?.also { put("long", it) }
        attachment?.also { put("attachment", it) }
        replyTo?.also { put("reply_to", it) }
        forwardMessages?.also { put("forward_messages", it) }
        stickerId?.also { put("sticker_id", it) }
        groupId?.also { put("group_id", it) }
        keyboard?.also { put("keyboard", json.stringify(it)) }
        template?.also { put("template", json.stringify(it)) }
        payload?.also { put("payload", it) }
        dontParseLinks?.also { put("dont_parse_links", it) }
        disableMentions?.also { put("disable_mentions", it) }
        intent?.also { put("intent", it) }
        put("random_id", randomId ?: Random.nextLong(0, Long.MAX_VALUE))
    }
}