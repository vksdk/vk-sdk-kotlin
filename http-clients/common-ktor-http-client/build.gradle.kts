plugins {
    kotlin("multiplatform")
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
                implementation(project(":core"))
                implementation("io.ktor:ktor-client-core:${Config.Versions.ktor}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common", Config.Versions.Kotlin.kotlin))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:1.4.0")
                implementation("io.ktor:ktor-client-logging-jvm:1.4.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core-js:${Config.Versions.ktor}")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
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