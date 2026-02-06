import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.yonatankarp.ff4k.buildlogic.isAndroidSdkAvailable

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

if (isAndroidSdkAvailable()) {
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
                dependencies {
                    implementation(libs.findLibrary("robolectric").get())
                }
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
