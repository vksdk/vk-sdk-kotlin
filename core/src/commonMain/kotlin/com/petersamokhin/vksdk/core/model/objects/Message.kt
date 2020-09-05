package com.petersamokhin.vksdk.core.model.objects

import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.utils.intValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    var chatId: Int? = null,
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
    var template: MessageCarouselTemplate? = null
) {
    fun intent(v: String): Message {
        intent = v
        return this
    }

    fun payload(v: String): Message {
        payload = v
        return this
    }

    fun keyboard(v: Keyboard): Message {
        keyboard = v
        return this
    }

    fun template(v: MessageCarouselTemplate): Message {
        template = v
        return this
    }

    fun groupId(v: String): Message {
        groupId = v.toInt()
        return this
    }

    fun groupId(v: Int): Message {
        groupId = v
        return this
    }

    fun stickerId(v: String): Message {
        stickerId = v.toInt()
        return this
    }

    fun stickerId(v: Int): Message {
        stickerId = v
        return this
    }

    fun sendFrom(client: VkApiClient): VkRequest {
        return client.sendMessage(this)
    }

    fun replyTo(v: String): Message {
        replyTo = v.toInt()
        return this
    }

    fun replyTo(v: Int): Message {
        replyTo = v
        return this
    }

    fun attachment(v: String): Message {
        attachment = v
        return this
    }

    fun longitude(v: Double): Message {
        long = v
        return this
    }

    fun longitude(v: String): Message {
        long = v.toDouble()
        return this
    }

    fun latitude(v: Double): Message {
        lat = v
        return this
    }

    fun latitude(v: String): Message {
        lat = v.toDouble()
        return this
    }

    fun text(v: String): Message {
        message = v
        return this
    }

    fun chatId(v: String): Message {
        chatId = v.toInt()
        return this
    }

    fun chatId(v: Int): Message {
        chatId = v
        return this
    }

    fun peerId(v: String): Message {
        peerId = v.toInt()
        return this
    }

    fun peerId(v: Int): Message {
        peerId = v
        return this
    }

    fun domain(v: String): Message {
        domain = v
        return this
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
        keyboard?.also { put("keyboard", json.encodeToString(it)) }
        template?.also { put("template", json.encodeToString(it)) }
        payload?.also { put("payload", it) }
        dontParseLinks?.also { put("dont_parse_links", it) }
        disableMentions?.also { put("disable_mentions", it) }
        intent?.also { put("intent", it) }
        put("random_id", randomId ?: Random.nextLong(0, Long.MAX_VALUE))
    }
}