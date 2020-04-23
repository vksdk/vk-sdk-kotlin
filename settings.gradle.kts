rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "vk-sdk-kotlin"

include(
    "core",

    "http-clients:jvm-okhttp-http-client",
    "http-clients:common-ktor-http-client"

    // Do NOT include ":examples:*" here;
    // they are individual projects.
    // To test, use `./gradlew publish` and test repository `./build/localMaven`
)