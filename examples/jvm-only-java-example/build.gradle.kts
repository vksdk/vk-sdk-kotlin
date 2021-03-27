buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", "1.4.30"))
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven {
            // Change to your path
            url = uri("file:/Users/petersamokhin/Projects/vksdk/vk-sdk-kotlin/build/localMaven")
        }
    }
}