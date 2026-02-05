import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

configure<DokkaExtension> {
    moduleName.set(project.name)

    val version = project.version.toString()
    val gitReference = when {
        version.endsWith("-SNAPSHOT") -> "main"
        else -> "v$version"
    }

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(
                uri("https://github.com/yonatankarp/ff4k/tree/$gitReference/${project.name}/src")
            )
            remoteLineSuffix.set("#L")
        }
    }

    dokkaPublications.configureEach {
        suppressInheritedMembers.set(false)
    }
}
