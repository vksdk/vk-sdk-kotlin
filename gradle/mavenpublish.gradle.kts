apply(plugin = "org.gradle.maven-publish")
apply(plugin = "org.gradle.signing")

tasks.register<Jar>("emptySourcesJar") {
    @Suppress("DEPRECATION")
    classifier = "sources"
}

tasks.register<Jar>("emptyJavadocJar") {
    @Suppress("DEPRECATION")
    classifier = "javadoc"
}

configure<PublishingExtension> {
    gradle.taskGraph.whenReady {
        configure<SigningExtension> {
            isRequired = isReleaseBuild() && gradle.taskGraph.hasTask("uploadArchives")
            sign(publications)
        }
    }

    when {
        project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
            publications.all(::configureMultiplatformPublication)
        }
        project.findProperty("CREATE_JVM_PUBLICATION")?.toString() == "true" -> {
            configureJvmPublication(publications)
        }
        else -> {
            /* Do nothing, e.g. for example projects */
        }
    }

    repositories {
        if (getRepositoryUsername()?.isEmpty() == true && getRepositoryPassword()?.isNotEmpty() == true) {
            project.logger.warn("publish: maven")
            maven {
                val isRelease = isReleaseBuild()
                url = uri(if (isRelease) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl())
                credentials {
                    username = getRepositoryUsername()
                    password = getRepositoryPassword()
                }
            }
        } else {
            project.logger.warn("publish: maven: skip (no repository login or password were provided)")
        }
        maven {
            val testRepoUri = "file://${rootProject.buildDir}/localMaven"
            project.logger.warn("publish: test: repo: $testRepoUri")

            name = "test"
            url = uri(testRepoUri)
        }
    }
}

/**
 * Default configuration for Multiplatform projects
 */
fun configureMultiplatformPublication(publication: Publication) {
    (publication as MavenPublication).apply {
        artifact(tasks["emptyJavadocJar"])
        configurePom(pom)

        if (name == "kotlinMultiplatform") {
            afterEvaluate {
                artifact(tasks["emptySourcesJar"])
            }
        }
    }
}

/**
 * Default configuration for simple JVM projects
 */
fun configureJvmPublication(publications: PublicationContainer) {
    @Suppress("UnstableApiUsage")
    configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()
    }

    publications.create<MavenPublication>("jvm") {
        artifactId = project.property("POM_ARTIFACT_ID").toString()
        from(components["java"])
        configurePom(pom, packaging = project.property("POM_PACKAGING").toString())
    }
}

/**
 * Default POM configuration for each project
 *
 * @param packaging If necessary
 */
fun configurePom(pom: org.gradle.api.publish.maven.MavenPom, packaging: String? = null) {
    pom.apply {
        name.set(project.property("POM_NAME").toString())
        description.set(project.property("POM_DESCRIPTION").toString())
        url.set(project.property("POM_URL").toString())
        licenses {
            license {
                name.set(project.property("POM_LICENCE_NAME").toString())
                url.set(project.property("POM_LICENCE_URL").toString())
                distribution.set(project.property("POM_LICENCE_DIST").toString())
            }
        }
        developers {
            developer {
                id.set(project.property("POM_DEVELOPER_ID").toString())
                name.set(project.property("POM_DEVELOPER_NAME").toString())
            }
        }
        scm {
            url.set(project.property("POM_SCM_URL").toString())
            connection.set(project.property("POM_SCM_CONNECTION").toString())
            developerConnection.set(project.property("POM_SCM_DEV_CONNECTION").toString())
        }
    }
}

/**
 * @return When it is needed to upload release artifacts
 */
fun isReleaseBuild(): Boolean {
    return !project.property("VERSION_NAME").toString().contains("SNAPSHOT")
}

fun getRepositoryUsername(): String? {
    return System.getenv("ORG_GRADLE_PROJECT_SONATYPE_NEXUS_USERNAME")
}

fun getRepositoryPassword(): String? {
    return System.getenv("ORG_GRADLE_PROJECT_SONATYPE_NEXUS_PASSWORD")
}

fun getReleaseRepositoryUrl(): String {
    return project.property("REPOSITORY_URL_MAVEN_STAGING_DEFAULT").toString()
}

fun getSnapshotRepositoryUrl(): String {
    return project.property("REPOSITORY_URL_MAVEN_SNAPSHOT_DEFAULT").toString()
}

// exclude projects from dokka task
if (project.name !in listOf("stately-embedded")) {
    apply(from = "$rootDir/gradle/dokka.gradle")
}