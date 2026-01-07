import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

configure<DokkaExtension> {
    moduleName.set(project.name)

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(
                uri("https://github.com/yonatankarp/ff4k/tree/main/${project.name}/src")
            )
            remoteLineSuffix.set("#L")
        }
    }

    dokkaPublications.configureEach {
        suppressInheritedMembers.set(false)
    }
}
