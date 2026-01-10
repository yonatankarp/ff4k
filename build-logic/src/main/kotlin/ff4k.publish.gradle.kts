import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    `maven-publish`
    signing
}

plugins.withId("org.jetbrains.kotlin.multiplatform") {
    configure<KotlinMultiplatformExtension> {
        jvm {
            withSourcesJar()
        }

        linuxX64 {
            withSourcesJar()
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = project.group.toString()
        version = project.version.toString()

        pom {
            name.set(project.name)
            description.set("FF4K - Kotlin Multiplatform feature flag library")
            url.set("https://github.com/yonatankarp/ff4k")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("yonatankarp")
                    name.set("Yonatan Karp-Rudin")
                    email.set("yonvata@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/yonatankarp/ff4k.git")
                developerConnection.set("scm:git:ssh://git@github.com/yonatankarp/ff4k.git")
                url.set("https://github.com/yonatankarp/ff4k")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/yonatankarp/ff4k")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")

    val hasSigningKeys = signingKey != null && signingPassword != null

    if (hasSigningKeys) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }

    isRequired = hasSigningKeys && !version.toString().endsWith("SNAPSHOT")
}
