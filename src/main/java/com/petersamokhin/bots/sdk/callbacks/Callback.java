package com.petersamokhin.bots.sdk.callbacks;

import com.petersamokhin.bots.sdk.objects.Message;

/**
 * Abstract class for event callbacks
 * You can override only necessary methods to get events
 */
public abstract class Callback {

    public Callback() {}

    /**
     * onMessage callback will be called on every incoming messages.
     *
     * @param message Message object
     * @see Message
     */
    public void onMessage(Message message) {
    }

    /**
     * onPhotoMessage callback will be called
     * on every messages that contains at least one photo.
     *
     * @param message Message object
     * @see Message
     */
    public void onPhotoMessage(Message message) {
    }

    /**
     * onVideoMessage callback will be called
     * on every messages that contains at least one video.
     *
     * @param message Message object
     * @see Message
     */
    public void onVideoMessage(Message message) {
    }

    /**
     * onAudioMessage callback will be called
     * on every messages that contains at least one audio (not voice message).
     *
     * @param message Message object
     * @see Message
     */
    public void onAudioMessage(Message message) {
    }

    /**
     * onDocMessage callback will be called
     * on every messages that contains at least one doc.
     *
     * @param message Message object
     * @see Callback#onGifMessage(Message) use this for gifs, because for them onDocCallback won't be called.
     * @see Message
     */
    public void onDocMessage(Message message) {
    }

    /**
     * onWallMessage callback will be called
     * on every messages that contains at least one wall post.
     *
     * @param message Message object
     * @see Message
     */
    public void onWallMessage(Message message) {
    }

    /**
     * onStickerMessage callback will be called
     * on every messages that contains sticker.
     *
     * @param message Message object
     * @see Message
     */
    public void onStickerMessage(Message message) {
    }

    /**
     * onLinkMessage callback will be called
     * on every messages that contains link.
     *
     * @param message Message object
     * @see Message
     */
    public void onLinkMessage(Message message) {
    }

    /**
     * onVoiceMessage callback will be called
     * on every voice message.
     *
     * @param message Message object
     * @see Message
     */
    public void onVoiceMessage(Message message) {
    }

    /**
     * onGifMessage callback will be called
     * on every messages that contains at least one gif.
     *
     * @param message Message object
     * @see Callback#onDocMessage(Message) gifs are docs too, but for them will be called this callback, not onDoc
     * @see Message
     */
    public void onGifMessage(Message message) {
    }

    /**
     * onSimpleTextMessage callback will be called
     * on every messages that contains only text and no attachments.
     *
     * @param message Message object
     * @see Message
     */
    public void onSimpleTextMessage(Message message) {
    }

    /**
     * onTyping callback will be called
     * when users start typing.
     * More: <a href="https://vk.com/dev/using_longpoll">link</a>
     *
     * @param user_id Message object
     * @see Message
     */
    public void onTyping(Integer user_id) {
    }
}
