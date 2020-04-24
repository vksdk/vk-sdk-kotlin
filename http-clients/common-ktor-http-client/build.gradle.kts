@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common", Config.Versions.Kotlin.kotlin))
                implementation(project(":core"))
                implementation("io.ktor:ktor-client-core-native:${Config.Versions.ktor}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common", Config.Versions.Kotlin.kotlin))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8", Config.Versions.Kotlin.kotlin))
                implementation("io.ktor:ktor-client-core-jvm:${Config.Versions.ktor}")
            }
        }
        val jvmTest by getting {
            dependencies {

            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    listOf("iosX64", "iosArm32", "iosArm64", "tvosX64", "tvosArm64", "watchosX86", "watchosArm32", "watchosArm64", "macosX64", "mingwX64")
        .forEach {
            targetFromPreset(presets[it], it) {
                compilations["main"].source(sourceSets["nativeMain"])
                compilations["test"].source(sourceSets["nativeTest"])
            }
        }
}

apply(from = "${rootDir}/gradle/mavenpublish.gradle")