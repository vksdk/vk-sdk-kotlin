plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Config.Versions.Kotlin.kotlin
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":stately-embedded"))
                implementation(kotlin("stdlib-common", Config.Versions.Kotlin.kotlin))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Config.Versions.Kotlin.coroutines}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Config.Versions.Kotlin.serialization}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common", Config.Versions.Kotlin.kotlin))
                implementation(kotlin("test-annotations-common", Config.Versions.Kotlin.kotlin))
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.Kotlin.coroutines}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Config.Versions.Kotlin.serialization}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test", Config.Versions.Kotlin.kotlin))
                implementation(kotlin("test-junit", Config.Versions.Kotlin.kotlin))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Config.Versions.Kotlin.coroutines}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:${Config.Versions.Kotlin.serialization}")
            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }

        val linuxMain by creating {
            dependsOn(nativeMain)
        }

        val mingwMain by creating {
            dependsOn(nativeMain)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    listOf(
        "iosX64", "iosArm32", "iosArm64", "tvosX64", "tvosArm64", "watchosX86", "watchosArm32", "watchosArm64", "macosX64"
    ).forEach {
        targetFromPreset(presets[it], it) {
            compilations["main"].source(sourceSets["darwinMain"])
            compilations["test"].source(sourceSets["nativeTest"])
        }
    }

    listOf(
        // https://github.com/Kotlin/kotlinx.coroutines/issues/855
        // "linuxArm32Hfp", "linuxMips32",
        "linuxX64"
    ).forEach {
        targetFromPreset(presets[it], it) {
            compilations["main"].source(sourceSets["linuxMain"])
            compilations["test"].source(sourceSets["nativeTest"])
        }
    }

    targetFromPreset(presets["mingwX64"], "mingwX64") {
        compilations["main"].source(sourceSets["mingwMain"])
        compilations["test"].source(sourceSets["nativeTest"])
    }
}

apply(from = "$rootDir/gradle/mavenpublish.gradle")