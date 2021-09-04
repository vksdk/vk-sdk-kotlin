package com.petersamokhin.vksdk.core.model.objects

import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.utils.intValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Message to be sent
 *
 * [https://vk.com/dev/messages.send]
 */
public class Message(
    public var userId: Int? = null,
    public var peerId: Int? = null,
    public var domain: String? = null,
    public var chatId: Int? = null,
    public var userIds: String? = null,
    public var randomId: Long? = null,
    public var message: String? = null,
    public var lat: Double? = null,
    public var long: Double? = null,
    public var attachment: String? = null,
    public var replyTo: Int? = null,
    public var forwardMessages: String? = null,
    public var stickerId: Int? = null,
    public var groupId: Int? = null,
    public var keyboard: Keyboard? = null,
    public var payload: String? = null,
    public var dontParseLinks: Int? = null,
    public var disableMentions: Int? = null,
    public var intent: String? = null,
    public var template: MessageCarouselTemplate? = null
) {
    public fun intent(v: String): Message {
        intent = v
        return this
    }

    public fun payload(v: String): Message {
        payload = v
        return this
    }

    public fun keyboard(v: Keyboard): Message {
        keyboard = v
        return this
    }

    public fun template(v: MessageCarouselTemplate): Message {
        template = v
        return this
    }

    public fun groupId(v: String): Message {
        groupId = v.toInt()
        return this
    }

    public fun groupId(v: Int): Message {
        groupId = v
        return this
    }

    public fun stickerId(v: String): Message {
        stickerId = v.toInt()
        return this
    }

    public fun stickerId(v: Int): Message {
        stickerId = v
        return this
    }

    public fun sendFrom(client: VkApiClient): VkRequest {
        return client.sendMessage(this)
    }

    public fun replyTo(v: String): Message {
        replyTo = v.toInt()
        return this
    }

    public fun replyTo(v: Int): Message {
        replyTo = v
        return this
    }

    public fun attachment(v: String): Message {
        attachment = v
        return this
    }

    public fun longitude(v: Double): Message {
        long = v
        return this
    }

    public fun longitude(v: String): Message {
        long = v.toDouble()
        return this
    }

    public fun latitude(v: Double): Message {
        lat = v
        return this
    }

    public fun latitude(v: String): Message {
        lat = v.toDouble()
        return this
    }

    public fun text(v: String): Message {
        message = v
        return this
    }

    public fun chatId(v: String): Message {
        chatId = v.toInt()
        return this
    }

    public fun chatId(v: Int): Message {
        chatId = v
        return this
    }

    public fun peerId(v: String): Message {
        peerId = v.toInt()
        return this
    }

    public fun peerId(v: Int): Message {
        peerId = v
        return this
    }

    public fun domain(v: String): Message {
        domain = v
        return this
    }

    public fun userIds(vararg ids: Int): Message {
        userIds = ids.joinToString(",")
        return this
    }

    public fun userIds(vararg ids: String): Message {
        userIds = ids.joinToString(",")
        return this
    }

    public fun userIds(ids: Iterable<Int>): Message {
        userIds = ids.joinToString(",")
        return this
    }

    public fun forwardMessages(vararg ids: Int): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    public fun forwardMessages(vararg ids: String): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    public fun forwardMessages(ids: Iterable<Int>): Message {
        forwardMessages = ids.joinToString(",")
        return this
    }

    public fun attachment(items: Iterable<String>): Message {
        attachment = items.joinToString(",")
        return this
    }

    public fun dontParseLinks(value: Boolean): Message {
        dontParseLinks = value.intValue
        return this
    }

    public fun disableMentions(value: Boolean): Message {
        disableMentions = value.intValue
        return this
    }

    @OptIn(ExperimentalSerializationApi::class)
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