import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata

plugins {
    base
}

tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
    onlyIf { false }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
    onlyIf { false }
}
