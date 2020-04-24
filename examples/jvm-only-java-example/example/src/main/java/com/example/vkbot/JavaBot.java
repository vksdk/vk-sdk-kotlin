package com.example.vkbot;

import com.petersamokhin.vksdk.core.api.VkApi;
import com.petersamokhin.vksdk.core.callback.Callback;
import com.petersamokhin.vksdk.core.client.VkApiClient;
import com.petersamokhin.vksdk.core.http.HttpClient;
import com.petersamokhin.vksdk.core.http.Parameters;
import com.petersamokhin.vksdk.core.model.VkSettings;
import com.petersamokhin.vksdk.core.model.objects.Message;
import com.petersamokhin.vksdk.http.VkOkHttpClient;
import kotlinx.serialization.json.JsonElement;
import org.jetbrains.annotations.NotNull;

public class JavaBot {
    public void start(final int clientId, @NotNull final String accessToken) {
        if (accessToken.equals("abcdef123456...")) throw new RuntimeException("Please, replace dummy access_token with yours in Launcher.kt");

        // OkHttp client is available only for JVM
        final HttpClient httpClient = new VkOkHttpClient();

        // Woo-hoo! We can use different constructors. Thanks @JvmOverloads
        final VkSettings vkSettings = new VkSettings(httpClient,
                VkApi.DEFAULT_VERSION,
                // Woo-hoo! @JvmStatic
                Parameters.of("lang", "en"),
                3
        );

        final VkApiClient vkApiClient = new VkApiClient(clientId, accessToken, VkApiClient.Type.Community, vkSettings);

        // Using of the kotlin coroutines flow will be not so convenient, but still possible;
        // otherwise, you can use sync and async calls with callbacks.
        vkApiClient.call("users.get", Parameters.of("user_id", "1"), false, new Callback<JsonElement>() {
            @Override
            public void onResult(@NotNull final JsonElement jsonElement) {
                // Parsing of POJOs is not so easy using kotlinx.serialization
                // Of course, you can use GSON, Moshi, etc.
                System.out.println(jsonElement);
            }

            @Override
            public void onError(@NotNull Exception e) {
                e.printStackTrace();
            }
        });

        // Woo-hoo! SAM! As you can see, callbacks are more pretty in Java.
        vkApiClient.onMessage(event -> {
            new Message()
                    .peerId(event.getMessage().getPeerId())
                    .text("Hello, world!")
                    .sendFrom(vkApiClient)
                    .execute();
        });

        // And, of course, to chat bot be working,
        // we must start the long polling loop.
        // Do it at the end of your method,
        // or call in the background thread or coroutine.
        vkApiClient.startLongPolling();
    }

    public static void main(final String[] args) {
        final int groupId = 151083290;
        final String groupAccessToken = "abcdef123456...";

        final JavaBot bot = new JavaBot();
        bot.start(groupId, groupAccessToken);
    }
}
