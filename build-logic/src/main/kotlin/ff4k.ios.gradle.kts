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
    }
}
