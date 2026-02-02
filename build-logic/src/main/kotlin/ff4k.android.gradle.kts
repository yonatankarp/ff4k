import java.io.File
import java.util.Properties
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val androidSdkAvailable: Boolean by lazy {
    val env = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (env != null && File(env).exists()) return@lazy true
    val localProperties = project.rootProject.file("local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        localProperties.inputStream().use { properties.load(it) }
        val sdkDir = properties.getProperty("sdk.dir")
        return@lazy sdkDir != null && File(sdkDir).exists()
    }
    false
}

if (androidSdkAvailable) {
    apply(plugin = "com.android.library")

    configure<KotlinMultiplatformExtension> {
        androidTarget {
            publishLibraryVariants("release")
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        sourceSets {
            val jvmSharedMain = findByName("jvmSharedMain")
            val jvmSharedTest = findByName("jvmSharedTest")

            named("androidMain") {
                jvmSharedMain?.let { dependsOn(it) }
            }

            named("androidUnitTest") {
                jvmSharedTest?.let { dependsOn(it) }
            }
        }
    }

    extensions.configure<LibraryExtension> {
        namespace = "com.yonatankarp.${project.name.replace("-", ".")}"
        compileSdk = 34
        defaultConfig {
            minSdk = 24
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }
        sourceSets {
            named("test") {
                resources.srcDir("src/commonTest/resources")
            }
        }
    }
}
