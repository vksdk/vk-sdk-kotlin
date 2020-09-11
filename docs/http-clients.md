# HTTP clients

Library uses [the abstract HTTP client](https://vksdk.github.io/vk-sdk-kotlin/0.0.x/core/com.petersamokhin.vksdk.core.http/-http-client/), you can write your own implementation, which will be the best for your needs, e.g. for your platform.

There are some implementations, which you can already add to your dependencies and use, see the sections below.
All the client artifacts are uploaded to the Maven Central Repository.

### Abstract HTTP client configuration
All parameters are optional.
Configuration for clients is also optional, but may be useful.
```kotlin
val httpClientConfig: HttpClientConfig = HttpClientConfig(
    connectTimeout = 30_000,
    readTimeout = 30_000,
    defaultHeaders = mapOf("User-Agent" to "VK SDK Kotlin/0.0.x")
)
```
Usually, this configuration is provided into HTTP client constructor.

!!! info "Note about the logging"
    It is barely possible to help you without seeing the VK responses, so you need to log it by yourself.
    For this, you can provide overrided client and configure this using the logging-interceptor for OkHttp, or the logging feature for ktor.

### Abstract HTTP client methods
To implement this interface, you should override some simple methods:

[**`HttpClient.kt`**](https://github.com/vksdk/vk-sdk-kotlin/blob/master/core/src/commonMain/kotlin/com/petersamokhin/vksdk/core/http/HttpClient.kt)
```kotlin
/**
 * Abstract HTTP client
 */
interface HttpClient {    
    /**
     * Apply client configuration
     *
     * @param config Configuration, such as read and connect timeout, etc
     */
    fun applyConfig(config: HttpClientConfig)
    
    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @return Response
     */
    fun getSync(url: String): Response?

    /* And there are some other methods */
}
```

Where [**`Response.kt`**](https://github.com/vksdk/vk-sdk-kotlin/blob/master/core/src/commonMain/kotlin/com/petersamokhin/vksdk/core/http/Response.kt) is very simple:
```kotlin
data class Response(
    val code: Int,
    val body: ByteArray?
) {
    /* Some pre-defined methods */
}
```

## JVM: OkHttp-based client
[OkHttp](https://github.com/square/okhttp) is available **only** for JVM, so you can use this client e.g. in Android or in the simple Java project.

### Install

```groovy
// also, core module is required
implementation "com.petersamokhin.vksdk:http-client-jvm-okhttp:$vkSdkVersion"
```

### Use
```kotlin
// HTTP client configuration is optional, see the snippet above
val httpClient: HttpClient = VkOkHttpClient(httpClientConfig)
```

The HTTP client instance is needed for the `VkApiClient` initialization, but, of course, you can use it too.

## Common: ktor-based client
[ktor](https://github.com/ktorio/ktor) is a Kotlin-based HTTP client (and also contains some other modules, e.g. server), so it can be used on any plaltform where the Kotlin can be used.

### Install

As for the `core`, if the Gradle Metadata is enabled, you can simply add this this to your dependencies:
```kotlin
implementation "com.petersamokhin.vksdk:http-client-common-ktor:$vkSdkVersion"
```

Or provide the platform name otherwise. See the Artifacts section [here](https://vksdk.github.io/vk-sdk-kotlin/usage/).

And, one more dependency is required: you should choose any ktor client, e.g. `CIO`:
```kotlin
implementation("io.ktor:ktor-client-cio:1.3.2")
```

### Use
You can provide ktor HttpClient in constructor:
```kotlin
val httpClient: HttpClient = VkKtorHttpClient(
            coroutineContext = Dispatchers.IO /* + job */,
            overrideClient = HttpClient(CIO) {
                engine {
                    requestTimeout = 30_000L
                }
            }
        )
```

Otherwise, override `createEngineWithConfig` method: 
```kotlin
class CioKtorHttpClient: VkKtorHttpClient(
    coroutineContext = Dispatchers.IO /* + job */
) {
    override fun createEngineWithConfig(config: HttpClientConfig): HttpClientEngine? {
        return CIO.create {
            engine {
                requestTimeout = 30_000L
            }
        }
    }
}
```

And then all the things are the same.

So, now you can use this client e.g. in the common module of your multiplatform project, etc.
Of course, as the OkHttp-based client, this ktor client can also be used in Java-only project. 