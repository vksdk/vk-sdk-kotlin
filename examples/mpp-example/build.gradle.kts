buildscript {
    repositories {
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.0")
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
        maven {
            // Change to your path
            url = uri("file:/Users/petersamokhin/Projects/vksdk/vk-sdk-kotlin/build/localMaven")
        }
    }
}