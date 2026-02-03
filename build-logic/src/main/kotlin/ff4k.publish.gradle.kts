import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id("ff4k.publish-commons")
    id("org.jetbrains.dokka")
}

ext.set("FF4K_POM_DESCRIPTION", "FF4K - Kotlin Multiplatform feature flag library")

mavenPublishing {
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        configure(
            KotlinMultiplatform(
                javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                sourcesJar = true,
            )
        )
    }
}