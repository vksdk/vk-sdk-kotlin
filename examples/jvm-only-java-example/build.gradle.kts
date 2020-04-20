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

repositories {
    maven {
        url = uri("${rootDir}/build/localMaven")
    }
}

dependencies {
    // We don't want to use Kotlin, so it's not listed in our dependencies.
    implementation(kotlin("stdlib-jdk7", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    val vkSdkKotlinVersion = "0.0.1-SNAPSHOT"

    // Module "core" is required.
    // `project(":core")` only available if your project and the SDK are in the same Gradle project.
    // Prefer to use "com.petersamokhin.vksdk:core-${platform}:${vkSdkKotlinVersion}"
    implementation("com.petersamokhin.vksdk:core-jvm:$vkSdkKotlinVersion")

    // One of the HTTP clients is required.
    // OkHttp client is available only for the JVM.
    implementation("com.petersamokhin.vksdk:http-client-jvm-okhttp:$vkSdkKotlinVersion")

    // If your project is not JVM-based, or you simply want to use ktor.
    // In this case, `ktor-client` is required. You can use any.
    // implementation("com.petersamokhin.vksdk:http-client-common-ktor-jvm:$vkSdkKotlinVersion")
    // implementation("io.ktor:ktor-client-cio:${Config.Versions.ktor}")
}