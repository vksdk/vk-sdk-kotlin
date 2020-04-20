rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "vk-sdk-kotlin"

include(
    "core",

    "http-clients:jvm-okhttp-http-client",
    "http-clients:common-ktor-http-client",

    "stately-embedded",

    "examples:jvm-kotlin-example",
    "examples:jvm-only-java-example"
)
