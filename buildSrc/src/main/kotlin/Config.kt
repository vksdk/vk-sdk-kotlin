object Config {
    object Versions {
        object Kotlin {
            const val kotlin = "1.5.30"
            const val coroutines = "1.5.2"
            const val serialization = "1.2.2"
        }
        object Plugin {
            const val androidGradle = "3.6.1"
            const val publish = "0.17.0"

            // https://github.com/Kotlin/dokka/issues/819
            // const val dokka = "0.9.18" // "0.10.1"

            const val dokka = "1.5.0"
        }
        object Test {
            const val junit = "4.12"
        }

        const val okhttp = "4.9.1"
        const val ktor = "1.6.3"
        const val stately = "1.1.10"
        const val statelyNew = "1.1.10-a1"
    }
}