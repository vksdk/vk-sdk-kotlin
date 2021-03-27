package com.example.vkbot;

import com.petersamokhin.vksdk.core.api.VkApi;
import com.petersamokhin.vksdk.core.client.VkApiClient;
import com.petersamokhin.vksdk.core.http.HttpClient;
import com.petersamokhin.vksdk.core.http.Parameters;
import com.petersamokhin.vksdk.core.model.VkSettings;
import com.petersamokhin.vksdk.core.model.event.MessageNew;
import com.petersamokhin.vksdk.core.model.objects.Message;
import com.petersamokhin.vksdk.core.utils.java.SuspendCallback;
import com.petersamokhin.vksdk.http.VkOkHttpClient;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.FlowCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaBot {
    public void start(final int clientId, @NotNull final String accessToken) {
        if (accessToken.equals("abcdef123456..."))
            throw new RuntimeException("Please, replace dummy access_token with yours in Launcher.kt");

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
        // for this, you can use the wrapper — SuspendCallback
        vkApiClient.get(
            "users.get",
            Parameters.of("user_id", "1"),
            SuspendCallback.assemble(jsonElement -> {
                System.out.println("users.get - onResult - " + jsonElement);
            }, throwable -> {
                System.out.println("users.get - onError - " + throwable);
            })
        );

        vkApiClient.onMessage(event -> { // Woo-hoo! SAM! As you can see, callbacks are more pretty in Java.
            new Message()
                .peerId(event.getMessage().getPeerId())
                .text("Hello, world!")
                .sendFrom(vkApiClient)
                .execute(SuspendCallback.empty()); // workaround for Java calls of suspend functions
        });

        vkApiClient.api()

        // And, of course, to chat bot be working,
        // we must start the long polling loop.
        // Do it at the end of your method,
        // or call in the background thread or coroutine.
        vkApiClient.startLongPolling(SuspendCallback.empty()); // blocks the thread
    }

    public static void main(final String[] args) {
        final int groupId = 151083290;
        final String groupAccessToken = "abcdef123456...";

        final JavaBot bot = new JavaBot();
        bot.start(groupId, groupAccessToken);
    }
}