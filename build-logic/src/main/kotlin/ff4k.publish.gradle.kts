import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name.set(project.name)
        description.set("FF4K - Kotlin Multiplatform feature flag library")
        url.set("https://github.com/yonatankarp/ff4k")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("yonatankarp")
                name.set("Yonatan Karp-Rudin")
                email.set("yonvata@gmail.com")
                url.set("https://github.com/yonatankarp")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/yonatankarp/ff4k.git")
            developerConnection.set("scm:git:ssh://git@github.com/yonatankarp/ff4k.git")
            url.set("https://github.com/yonatankarp/ff4k")
        }
    }
}
