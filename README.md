# VK SDK Kotlin
![cover](docs/images/cover.png)

Unofficial VK.com SDK based written in Kotlin.
Based on Kotlin Multiplatform project, but have Java-friendly API.

## Minimal example bot

### Kotlin
```kotlin
// From here: https://vk.com/club151083290
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
```

### Java
```java
int groupId = 151083290
String accessToken = "abcdef123456..."
HttpClient vkHttpClient = VkOkHttpClient()

VkApiClient client = VkApiClient(groupId, accessToken, VkApiClient.Type.Community, new VkSettings(vkHttpClient))

client.onMessage(messageEvent -> {
    new Message()
        .peerId(event.getMessage().getPeerId())
        .text("Hello, world!")
        .sendFrom(vkApiClient)
        .execute();
})
```