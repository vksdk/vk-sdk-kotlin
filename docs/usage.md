# Usage

See the [example projects](https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples):

- Most detailed example, written in Kotlin:
    - [https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/jvm-kotlin-example](https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/jvm-kotlin-example)
- Minimal example without Kotlin in dependencies:
    - [https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/jvm-only-java-example](https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/jvm-only-java-example)
- Minimal multiplatform app example (single activity on Android and SwiftUI on iOS):
    - [https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/mpp-example](https://github.com/vksdk/vk-sdk-kotlin/tree/master/examples/mpp-example)

### Artifacts
List of all available artifacts:

```groovy
implementation "com.petersamokhin.vksdk:http-client-jvm-okhttp:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-jvm:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-jvm:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-js:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-js:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-iosX64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-iosX64:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-iosArm32:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-iosArm32:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-iosArm64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-iosArm64:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-tvosX64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-tvosX64:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-tvosArm64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-tvosArm64:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-watchosX86:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-watchosX86:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-watchosArm32:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-watchosArm32:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-watchosArm64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-watchosArm64:$vkSdkVersion"

implementation "com.petersamokhin.vksdk:core-macosX64:$vkSdkVersion"
implementation "com.petersamokhin.vksdk:http-client-common-ktor-macosX64:$vkSdkVersion"
```

## Settings

### VkSettings
```kotlin
val vkClientSettings = VkSettings(
    // HTTP client is required
    httpClient = httpClient,                    

    // Default is [VkApi.DEFAULT_VERSION], 5.122 for 0.0.7
    // See: https://vk.com/dev/versions
    apiVersion = 5.122,                         
    
    // Default params are empty
    defaultParams = paramsOf("lang" to "en"),   

    // Default is 3. Provide [VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED] to disable the `execute` queue loop
    maxExecuteRequestsPerSecond = 3,

    // Default is Dispatchers.Default from the kotlin coroutines common module
    // See the mutiplatform example and iOS note for ktor
    backgroundDispatcher = Dispatchers.Default, 

    json = Json { /* your configuration */ }
)
```

## Initialization
This client is used in all the snippets in next sections.
```kotlin
val client = VkApiClient(
    // User or Community id
    id = 151083290,
    
    // VK API access token. Must have `offline` scope to not have the expire time
    // More: https://vk.com/dev/access_token
    token = "abcdef123456...",
 
    // Working with the messages API available only for Communities
    type = VkApiClient.Type.Community,

    // See the previous snippet
    settings = vkSettings
)
```

Also, for server-side applications, useful to do something on user behalf, but it is not safe to send the access token from client.
[Authorization code flow](https://vk.com/dev/authcode_flow_user) can help here.
```kotlin
// From here: https://vk.com/apps?act=manage
// Choose the app and get the ID and the other info from app settings
// App ID, Secure key, etc.
val appInfo = AppInfo(
    clientId = 123456789,
    clientSecret = "abcdef12345...",
    redirectUri = "https://oauth.vk.com/blank.html"
)

val client = VkApiClient.fromCode(
    // Code from the client
    code = "abcdef12345...",
    
    // App info, see above
    app = appInfo,
 
    // See the previous snippets; settings for the client
    // Client always is the User for the Code flow
    settings = vkSettings
)
```

## API requests

!!! info "Note about the responses"
    Usually VK responses contain `response` field with the requested data. 
    But, when you are using the `execute` API method, each object from usual `response` field is placed into array.
    So, in this case, if you use `client.call` with `batch = true`, the unwrapped object will be returned into the callback.
    If you made the call without putting into the batch queue, the wrapped object is returned.
    Remember this when you will parse the responses.

You can reuse the requests in this way:

```kotlin
// Request information
val request = client.call("users.get", paramsOf("user_id" to 1))
```

### Synchronous call
```kotlin
// It is always the string, you should parse it by yourself
val response: String = request.execute()
```

### Asynchronous call
```kotlin
request.enqueue(object : Callback<String> {
    override fun onResult(result: String) {
        // It is always the string, you should parse it by yourself
    }
    
    override fun onError(error: Exception) {
        println("Some error occurred:")
        error.printStackTrace()
    }
})
```

### Batch requests
Requests from the batch queue made as soon as possible based on count of requests in the queue.
Using this you cannot exceed the limit of requests per second of VK API.
The callback uses `JsonElement` because VK `execute` method returning the array of the responses, it is parsed and necessary element is returned to the callback.
It can be `false` in case of an error.

Pass the callback:
```kotlin
client.call(request, batch = true, object : Callback<JsonElement> {
   override fun onResult(result: String) {
       // It is always the JsonElement, you should parse it by yourself
   }
   
   override fun onError(error: Exception) {
       println("Some error occurred:")
       error.printStackTrace()
   }
})
```

Or use two callbacks
```kotlin
client.call(request, batch = true, onResult = { result: JsonElement ->
    // It is always the JsonElement, you should parse it by yourself
}, onError = { error: Exception ->
    println("Some error occurred:")
    error.printStackTrace()
})
```

Also, you can pass parameters, not only the request object
```kotlin
client.call("users.get", paramsOf("user_id" to 1), batch = true, /* callbacks */)
```

Do as many requests as you want
```kotlin
for (i in 0 until 500) {
    client.call("users.get", paramsOf("user_id" to 1), batch = true, /* callbacks */)
    // without any delay() !
}
```

Pass all of them in one time:
```kotlin
val requests: List<BatchRequestItem> = listOf(1, 2, 3, 4, 5)
    .map { userId -> client.call("users.get", paramsOf("user_id" to userId)) }
    .map { request -> BatchRequestItem(request, object: Callback<JsonElement> { /* */ }) }

client.call(requests)
```

### Flows for requests
All the functionality based on the `Flows` is available from the delegate:

```kotlin
val flows: VkApiClientFlows = client.flows()
```

Other things are the same:
```kotlin
client.flows()
    .call(request, batch = true) // or provide the method name and the parameters
    .onEach { result: JsonElement -> /* do something */ }
    .catch { e -> /* do something */ }
    .flowOn(Dispatchers.IO)
    .launchIn(lifecycleScope)
```

## Send messages
Use the DSL:
```kotlin
client.sendMesage {
    peerId = 12345678901
    message = "Hello, World!"
    attachment = "photo1234_5678,photo1234_1234"
    
}.execute()
```

Or use the builder:
```kotlin
val message = Message()
    .peerId(12345678901)
    .text("Hello, World!")

// Then send
val request = client.send(message)

// or else in this way
val request = message.sendFrom(client)

// Don't forget this!
request.execute() // or use flows, callbacks and whatever you want
```

### Keyboard
Use the DSL:
```kotlin
client.sendMessage {
    peerId = 12345678901
    message = "Hello, World!"
    keyboard = keyboard(oneTime = true) { // or `inlineKeyboard` for inline
       row {
           primaryButton("Blue pill") {
               payload = "{}" // etc.
           }
           negativeButton("Red pill")
       }
   }
}.execute()
```

### Attachments
All the functionality needed for the uploading is available from the delegate:
```kotlin
val uploader: VkApiUploader = client.uploader()
```

Simply attach the image:
```kotlin
val peerId = 12345678901

val imageAttachmentString = client.uploader().uploadPhotoForMessage(
    peerId,
    // or FileOnDisk(path = "/Users/you/Documents/img.png")
    // or ByteArray
    url = "https://example.com/image.png"
)
// imageAttachmentString is something like 'photo987654321_1234'

client.sendMessage {
    peerId = necessaryPeerId
    attachment = imageAttachmentString
}.execute()
```

Attach whatever you want, e.g audio message:
```kotlin
val necessaryPeerId = 12345678901
val attachmentType = "audio_message"

// VK's process of attachments uploading is boring
// and covered fully by this method,
// but responses are dynamic and can not be serialized in one time for all cases
// See: https://vk.com/dev/upload_files
val docAttachmentString = client.uploader().uploadContent(
    "docs.getMessagesUploadServer", // First step: method for retrieving of the `upload_url`
    "docs.save",                    // Last step: save the attachment on the VK server (can you VK guys do it under the hood?)
    params = paramsOf("type" to attachmentType, "peer_id" to necessaryPeerId),
    items = listOf(
        UploadableContent.File(
            fieldName = "file",
            // Provide these params but don't worry about their values;
            // but without them you will receive the error response.
            fileName = "doesn't matter, but VK API is not stable; fileName must not be empty",
            mediaType = "also doesn't matter",
            file = FileOnDisk("/Users/petersamokhin/Desktop/test.mp3")
        )
    )
).let { response: String ->
    // Request is synchronous. You should parse the response by yourself
    retrieveAttachment(it, attachmentType)
}

client.sendMessage {
    peerId = necessaryPeerId
    // or docAttachmentString, based on your implementation of retrieveAttachment methodd
    attachment = "doc${docAttachmentString}"
}.execute()
```

Types of `items`:
**Bytes** array:
```kotlin
val byteArray: ByteArray = retrieveBytesFromSomewhere()

val bytesItem: UploadableContent = UploadableContent.Bytes(
   fieldName = "file",
   fileName = "something.txt",
   mediaType = "text/plain",
   bytes = byteArray
)
```

**File** from the filesystem (only available for JVM, darwin and JS):
```kotlin
val fileItem: UploadableContent = UploadableContent.File(
    fieldName = "file",
    fileName = "test.mp3",
    mediaType = "audio/mpeg",
    file = FileOnDisk("/Users/petersamokhin/Desktop/test.mp3")
)
```

Something by **URL**:
```kotlin
val urlItem: UploadableContent = UploadableContent.Url(
    fieldName = "file",
    fileName = "image.png",
    mediaType = "image/png",
    url = "https://example.com/image.png"
)
```

## React on events
Use the [Bots Long Poll API](https://vk.com/dev/bots_longpoll).

!!! info "Start listening for the events"
    The method for starting the listening is blocking, so do not put it at the start of your code or wrap it with the async block.
    
Start listening for the events:
```kotlin
val settings = VkBotsLongPollApi.Settings(
    wait = 25,   // defaut value; recommended by the VK 
    maxFails = 5 // Use VkBotsLongPollApi.Settings.IGNORE_FAILS to ignore all of the errors
)

// All of the params are optional; don't forget about the note above
client.startLongPolling(restart = false, settings = settings)
```

Listen for new messages:
```kotlin
client.onMessage { event: MessageNew ->
    if (event.message.isFromChat()) {
        client.sendMessage {
            peerId = event.message.peerId
            message = "Hello, chat"
        }.execute()
    } else {
        client.sendMessage {
            peerId = event.message.peerId
            message = "Sorry, I ignore personal conversations."
        }.execute()
    }
}
```

Listen for all types of the events:
```kotlin
client.onEachEvent { event: JsonElement ->
    // You should parse the event
}
```

And, of course, `Flow` example:
```kotlin
client.flows().onMessage()
    .flatMapLatest {
        if (it.message.isFromChat()) {
            client.flows().sendMessage {
                peerId = it.message.peerId
                message = "Hello, chat"
            } // don't need to execute, etc., because flow is switch-mapped
        } else {
            client.flows().sendMessage {
                peerId = it.message.peerId
                message = "Sorry, I ignore personal conversations."
            } // don't need to execute, etc., because flow is switch-mapped
        }
    }
    .flowOn(Dispatchers.IO)
    .launchIn(this)
```

## HTTP clients

See: [https://vksdk.github.io/vk-sdk-kotlin/http-clients/](https://vksdk.github.io/vk-sdk-kotlin/http-clients/)