buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", "1.5.30"))
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven {
            // Change to your path for testing, if it is necessary
            url = uri("file:/Users/petersamokhin/Projects/vksdk/vk-sdk-kotlin/build/localMaven")
        }
    }
}