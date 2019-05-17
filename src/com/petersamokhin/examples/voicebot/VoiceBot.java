package com.petersamokhin.examples.voicebot;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;

import java.net.URLEncoder;

/**
 * Voice Bot
 * - any simple text message to voice message
 */
public class VoiceBot {

    public static void main(String[] args) {

        Group group = new Group(151083290, "access_token");

        // Yandex SpeechKit API key
        // http://developer.tech.yandex.ru/keys/
        String yandexKey = "key";

        String url = "https://tts.voicetech.yandex.net/generate?format=mp3&lang=ru&speaker=zahar&key=" + yandexKey + "&text=";

        // Voice all text messages
        group.onSimpleTextMessage(message -> {
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .sendVoiceMessage(url + URLEncoder.encode(message.getText()));
        });

        // Send error for other messages
        group.onMessage(message -> {
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .text("Sorry, please send me the message that contains only text. I will voice this message.")
                    .send();
        });
    }
}
