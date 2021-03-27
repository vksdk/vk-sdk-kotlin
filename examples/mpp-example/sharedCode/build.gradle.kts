import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.4.30"
    id("com.android.library")
}

/**
 * Configuration for androidMain of shared code,
 * then it will be the android library, not the simple java module.
 */
android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
        JavaVersion.VERSION_1_8.toString().also {
            kotlinOptions.jvmTarget = it
            if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                sourceCompatibility = it
                targetCompatibility = it
            }
        }
    }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        // device
        // System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64

        // emulator
        // else -> ::iosX64

        // if there are only arm64 dependencies
        // else -> ::iosArm64

        // if there are only X64 dependencies
        else -> ::iosX64
    }

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "SharedCode"
                @Suppress("SuspiciousCollectionReassignment")
                freeCompilerArgs += listOf("-Xobjc-generics", "-Xg0")
            }
        }
    }

    android()

    val vkSdkKotlinVersion = "0.0.8-SNAPSHOT"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.petersamokhin.vksdk:core:$vkSdkKotlinVersion")
                implementation("com.petersamokhin.vksdk:http-client-common-ktor:$vkSdkKotlinVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                implementation("io.ktor:ktor-client-core:1.5.2")
            }
        }
        val commonTest by getting {}

        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
                implementation("io.ktor:ktor-client-android:1.5.2")
            }
        }
        val androidTest by getting {}

        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native") {
                    version {
                        strictly("1.4.3-native-mt")
                    }
                }
                implementation("io.ktor:ktor-client-ios:1.5.2")
            }
        }
        val iosTest by getting {}

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

/**
 * Pack common code into framework for iOS
 */
val packForXcode by tasks.creating(Sync::class) {
    group = "build"

    // selecting the right configuration for the iOS framework depending on the Xcode environment variables
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)

    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)

    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\nexport 'JAVA_HOME=${System.getProperty("java.home")}'\ncd '${rootProject.rootDir}'\n./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}

tasks.getByName("build").dependsOn(packForXcode)