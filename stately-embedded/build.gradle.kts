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
            }
        }
        val commonTest by getting {}

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk7", Config.Versions.Kotlin.kotlin))
            }
        }
        val jvmTest by getting {}

        val nativeMain by creating {
            dependsOn(commonMain)

            dependencies {

            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }

        val pthreadMain by creating {
            dependsOn(nativeMain)
        }

        val mingwMain by creating {
            dependsOn(nativeMain)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    listOf("iosX64", "iosArm32", "iosArm64", "tvosX64", "tvosArm64", "watchosX86", "watchosArm32", "watchosArm64", "macosX64")
        .forEach {
            targetFromPreset(presets[it], it) {
                compilations["main"].source(sourceSets["darwinMain"])
                compilations["test"].source(sourceSets["nativeTest"])
            }
        }

    listOf("linuxX64"/*, "linuxArm32Hfp", "linuxMips32"*/)
        .forEach {
            targetFromPreset(presets[it], it) {
                compilations["main"].source(sourceSets["pthreadMain"])
                compilations["test"].source(sourceSets["nativeTest"])
            }
        }

    targetFromPreset(presets["mingwX64"], "mingwX64") {
        compilations["main"].source(sourceSets["mingwMain"])
        compilations["test"].source(sourceSets["nativeTest"])
    }
}

apply(from = "${rootDir}/gradle/mavenpublish.gradle")