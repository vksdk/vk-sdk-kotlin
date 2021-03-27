buildscript {
    repositories {
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath(kotlin("gradle-plugin", version = "1.4.30"))
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
        maven {
            // Change to your path, if you want to test your custom library version
            url = uri("file:/Users/petersamokhin/Projects/vksdk/vk-sdk-kotlin/build/localMaven")
        }
    }
}