import com.vanniktech.maven.publish.JavaPlatform

plugins {
    `java-platform`
    id("ff4k.publish-commons")
}

ext.set("FF4K_POM_DESCRIPTION", "FF4K - Bill of Materials")

mavenPublishing {
    configure(JavaPlatform())
}

dependencies {
    constraints {
        rootProject.subprojects
            .filter { it.name.startsWith("ff4k-") && it != project }
            .forEach { api(it) }
    }
}
