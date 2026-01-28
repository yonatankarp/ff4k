import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.diffplug.spotless")
    id("com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(
        libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()
    )

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

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

        androidMain {
            dependsOn(jvmSharedMain)
        }

        jvmTest {
            dependsOn(jvmSharedTest)
        }

        named("androidUnitTest") {
            dependsOn(jvmSharedTest)
        }

        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
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
    "jvmSharedTestCompileOnly"(libs.findLibrary("kotlin-test").get())
}

android {
    namespace = "org.ff4k.${project.name.replace("-", ".")}"
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
