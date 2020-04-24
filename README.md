# VK SDK Kotlin
![cover](docs/images/cover.png)

Unofficial VK.com SDK based written in Kotlin.
Based on Kotlin Multiplatform project, but has Java-friendly API.

The primary goal of the library is to cover most of the possible needs but in an abstract way.
You can't find here a hundred of pre-defined data classes for each API method, but you can write the highly customizable solution.

## Minimal example of the echo-chatbot

### Kotlin
```kotlin
// From here: https://vk.com/club151083290 take the ID
val groupId = 151083290

// Read more: https://vk.com/dev/access_token
val accessToken = "abcdef123456..."

// There are two http clients available for now: 
// JVM-only OkHttp-based
// and common ktor-based
val vkHttpClient = VkOkHttpClient()

val client = VkApiClient(groupId, accessToken, VkApiClient.Type.Community, VkSettings(vkHttpClient))

client.onMessage { messageEvent ->
    client.sendMessage {
        peerId = messageEvent.message.peerId
        message = "Hello, World!"

        // You can use stickers, replies, location, etc.
        // All of the message parameters are supported.
    }.execute()
}

client.startLongPolling()
```

### Java
```java
int groupId = 151083290;
String accessToken = "abcdef123456...";
HttpClient vkHttpClient = new VkOkHttpClient();

VkApiClient client = new VkApiClient(groupId, accessToken, VkApiClient.Type.Community, new VkSettings(vkHttpClient));

client.onMessage(event -> {
    new Message()
        .peerId(event.getMessage().getPeerId())
        .text("Hello, world!")
        .sendFrom(vkApiClient)
        .execute();
});

client.startLongPolling();
```

## What this library can do

- [Bots Long Poll API](https://vk.com/dev/bots_longpoll)
  - `MessageNew` for `message_new` event
  - `JsonElement` for all the other events