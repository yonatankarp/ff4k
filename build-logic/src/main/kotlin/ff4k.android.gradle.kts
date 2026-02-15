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
                // Only depend on jvmSharedTest if not using standalone JUnit4 tests
                // Modules that need JUnit4+Robolectric should override this in their build.gradle.kts
                if (project.findProperty("androidUnitTest.useKotest") != "false") {
                    jvmSharedTest?.let { dependsOn(it) }
                }
                dependencies {
                    implementation(libs.findLibrary("robolectric").get())
                }
            }
        }
    }

    extensions.configure<LibraryExtension> {
        namespace = "com.yonatankarp.${project.name.replace("-", ".")}"
        compileSdk = 36
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

    // Configure Android unit tests to use JUnit4 when Kotest is disabled
    if (project.findProperty("androidUnitTest.useKotest") == "false") {
        tasks.withType<Test>().matching { it.name.contains("UnitTest") }.configureEach {
            useJUnit()
        }
    }
}
