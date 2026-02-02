import java.io.File
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

val appleTargetsAvailable: Boolean by lazy {
    val localProperties = project.rootProject.file("local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        localProperties.inputStream().use { properties.load(it) }
        if (properties.getProperty("ff4k.include.apple") == "false") return@lazy false
    }
    val osName = System.getProperty("os.name")
    if (!osName.contains("Mac", ignoreCase = true)) return@lazy false
    File("/usr/bin/xcrun").exists()
}

if (appleTargetsAvailable) {
    configure<KotlinMultiplatformExtension> {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    val resourcesDir = project.file("src/commonTest/resources")
    tasks.withType<KotlinNativeTest> {
        environment("FF4K_RESOURCES_PATH", resourcesDir.absolutePath)
    }
}
