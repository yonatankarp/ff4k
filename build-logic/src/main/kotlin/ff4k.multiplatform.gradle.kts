import com.android.build.gradle.LibraryExtension
import java.io.File
import java.util.Properties
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.diffplug.spotless")
}

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

if (androidSdkAvailable) {
    apply(plugin = "com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(
        libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()
    )

    if (androidSdkAvailable) {
        androidTarget {
            publishLibraryVariants("release")
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    jvm()

    if (appleTargetsAvailable) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val jvmSharedMain by creating {
            dependsOn(commonMain.get())
        }

        val jvmSharedTest by creating {
            dependsOn(commonTest.get())
        }

        jvmMain {
            dependsOn(jvmSharedMain)
        }

        if (androidSdkAvailable) {
            val androidMain by getting {
                dependsOn(jvmSharedMain)
            }

            named("androidUnitTest") {
                dependsOn(jvmSharedTest)
            }
        }

        jvmTest {
            dependsOn(jvmSharedTest)
        }

        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findBundle("kotest").get())
        }
    }
}

// Add JVM stdlib visibility for jvmShared source sets (IDE support)
dependencies {
    "jvmSharedMainCompileOnly"(kotlin("stdlib"))
    "jvmSharedMainCompileOnly"(libs.findLibrary("kotlinx-coroutines-core").get())
    "jvmSharedTestCompileOnly"(kotlin("stdlib"))
    "jvmSharedTestCompileOnly"(libs.findLibrary("kotlinx-coroutines-core").get())
    "jvmSharedTestCompileOnly"(libs.findLibrary("kotlinx-coroutines-test").get())
    "jvmSharedTestImplementation"(libs.findLibrary("kotest-runner-junit5").get())
}

if (androidSdkAvailable) {
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

// Configure native test tasks to find test resources
val resourcesDir = project.file("src/commonTest/resources")
tasks.withType<KotlinNativeTest> {
    environment("FF4K_RESOURCES_PATH", resourcesDir.absolutePath)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_filename" to "disabled"
            )
        )
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint()
    }
}
