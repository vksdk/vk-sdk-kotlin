@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Config.Versions.Kotlin.kotlin
}

kotlin {
    explicitApiWarning()

    jvm()

    js {
        nodejs()

        compilations.all {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
                metaInfo = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.Kotlin.coroutines}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Config.Versions.Kotlin.serialization}")

                implementation("co.touchlab:stately-common:${Config.Versions.stately}")
                implementation("co.touchlab:stately-concurrency:${Config.Versions.stately}")
                implementation("co.touchlab:stately-isolate:${Config.Versions.statelyNew}")
                implementation("co.touchlab:stately-iso-collections:${Config.Versions.statelyNew}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test", Config.Versions.Kotlin.kotlin))
                implementation(kotlin("test-annotations-common", Config.Versions.Kotlin.kotlin))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit", Config.Versions.Kotlin.kotlin))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js", Config.Versions.Kotlin.kotlin))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
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
            languageSettings.optIn("kotlin.RequiresOptIn")
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