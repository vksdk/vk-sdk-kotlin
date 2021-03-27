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
    // We don't want to use Kotlin, so it's not listed in our dependencies.
    // implementation(kotlin("stdlib-jdk8", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    val vkSdkKotlinVersion = "0.0.8-SNAPSHOT"

    // Module "core" is required.
    // `project(":core")` only available if your project and the SDK are in the same Gradle project.
    // Prefer to use "com.petersamokhin.vksdk:core-${platform}:${vkSdkKotlinVersion}"
    implementation("com.petersamokhin.vksdk:core-jvm:$vkSdkKotlinVersion")

    // One of the HTTP clients is required.
    // OkHttp client is available only for the JVM.
    implementation("com.petersamokhin.vksdk:http-client-jvm-okhttp:$vkSdkKotlinVersion")

    // If your project is not JVM-based, or you simply want to use ktor.
    // implementation("com.petersamokhin.vksdk:http-client-common-ktor-jvm:$vkSdkKotlinVersion")

    // In this case, `ktor-client` is required. You can use any.
    // implementation("io.ktor:ktor-client-cio:1.5.2")
}