object Config {
    object Versions {
        object Kotlin {
            const val kotlin = "1.3.72"
            const val coroutines = "1.3.5"
            const val serialization = "0.20.0"
        }
        object Plugin {
            const val androidGradle = "3.6.1"
            const val publish = "0.11.1"

            // https://github.com/Kotlin/dokka/issues/819
            const val dokka = "0.9.18" // "0.10.1"
        }
        object Test {
            const val junit = "4.12"
        }

        const val moshi = "1.9.2"
        const val gson = "2.8.6"
        const val okhttp = "4.5.0"
        const val ktor = "1.3.2"
        const val stately = "1.0.2"
        const val statelyNew = "1.0.2-a4"
    }
}