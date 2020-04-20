plugins {
    java
    kotlin("jvm")
}

tasks.jar {
    archiveBaseName.set("vksdk-kotlin-http-client-jvm-okhttp")
}

dependencies {
    implementation(kotlin("stdlib-jdk7", Config.Versions.Kotlin.kotlin))
    implementation("com.squareup.okhttp3:okhttp:${Config.Versions.okhttp}")
    implementation(project(":core"))

    testImplementation(kotlin("test", Config.Versions.Kotlin.kotlin))
    testImplementation(kotlin("test-junit", Config.Versions.Kotlin.kotlin))
}

apply(from = "${rootDir}/gradle/mavenpublish.gradle")