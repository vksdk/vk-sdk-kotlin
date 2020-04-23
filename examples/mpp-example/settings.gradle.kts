enableFeaturePreview("GRADLE_METADATA")

rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "mpp-example-vk-sdk-kotlin"

include(
	":androidApp",
	":sharedCode"
)