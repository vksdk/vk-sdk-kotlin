package com.petersamokhin.bots.sdk;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.clients.User;
import com.petersamokhin.bots.sdk.objects.Message;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiSettings;

/**
 * Created by PeterSamokhin on 06/08/2017 01:21
 */
public class Main {

    public static void main(String... args) {

        Group group = new Group(151083290, "0cb6de1b33ab9079e38bede6e188d5b089f25eeebb4d714562c356aa9a82c3d0b1dc37720145526308b6c");
        User user = new User(62802565, "19b3eeb87626741169b7c3f0aa8311dad7ac31c27b7e8f8647a0ca54dbe22bbc05e730ff779fb60f67c88");

        group.onSimpleTextMessage(message ->
                new Message()
                        .from(group)
                        .to(message.authorId())
                        .text("Что-то скучновато буковки читать. Картинку кинь лучше.")
                        .send()
        );

        group.onPhotoMessage(message ->
                new Message()
                        .from(group)
                        .to(message.authorId())
                        .text("Уже лучше. Но я тоже так могу. Что дальше?")
                        .photo("/Users/PeterSamokhin/Desktop/topoviy_mem.png")
                        .send()
        );

        group.onVoiceMessage(message ->
                new Message()
                        .from(group)
                        .to(message.authorId())
                        .text("Не охота мне голосовые твои слушать.")
                        .doc("https://vk.com/doc62802565_447117479")
                        .send()
        );
    }
}
