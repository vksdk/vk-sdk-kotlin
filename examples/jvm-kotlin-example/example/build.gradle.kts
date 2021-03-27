plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.30"
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

dependencies {
    // We want to use Kotlin
    implementation(kotlin("stdlib-jdk8", "1.4.31"))

    val vkSdkKotlinVersion = "0.0.8-SNAPSHOT"

    // Module "core" is required.
    // `project(":core")` only available if your project and the SDK are in the same Gradle project.
    // Prefer to use "com.petersamokhin.vksdk:core:${vkSdkKotlinVersion}"
    implementation("com.petersamokhin.vksdk:core:$vkSdkKotlinVersion")

    // One of the HTTP clients is required.
    // OkHttp client is available only for the JVM.
    implementation("com.petersamokhin.vksdk:http-client-jvm-okhttp:$vkSdkKotlinVersion")

    // If your project is not JVM-based, or you simply want to use ktor.
    implementation("com.petersamokhin.vksdk:http-client-common-ktor:$vkSdkKotlinVersion")

    // In this case, `ktor-client` is required. You can use any.
    implementation("io.ktor:ktor-client-cio:1.5.2")
    implementation("io.ktor:ktor-client-logging-jvm:1.5.2")
}