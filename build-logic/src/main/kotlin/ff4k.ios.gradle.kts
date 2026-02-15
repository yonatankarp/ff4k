import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import com.yonatankarp.ff4k.buildlogic.areAppleTargetsAvailable

if (areAppleTargetsAvailable()) {
    configure<KotlinMultiplatformExtension> {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    val resourcesDir = project.file("src/commonTest/resources")
    tasks.withType<KotlinNativeTest> {
        environment("FF4K_RESOURCES_PATH", resourcesDir.absolutePath)
        // Disable report generation due to Gradle/Kotest bug with long test names
        // https://github.com/kotest/kotest/issues/4184
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }
}
