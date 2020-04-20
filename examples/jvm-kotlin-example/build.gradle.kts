plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

dependencies {
    // We want to use Kotlin
    implementation(kotlin("stdlib-jdk7", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    // Module "core" is required.
    // `project(":core")` only available if your project and the SDK are in the same Gradle project.
    // Prefer to use "com.petersamokhin.vksdk:core:${vk_sdk_kotlin_version}"
    implementation(project(":core"))

    // One of the HTTP clients is required.
    // OkHttp client is available only for the JVM.
    implementation(project(":http-clients:jvm-okhttp-http-client"))

    // If your project is not JVM-based, or you simply want to use ktor.
    // In this case, `ktor-client` is required. You can use any.
    implementation(project(":http-clients:common-ktor-http-client"))
    implementation("io.ktor:ktor-client-cio:1.3.2")
}