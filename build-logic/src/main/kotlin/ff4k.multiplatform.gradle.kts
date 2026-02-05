import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.diffplug.spotless")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(
        libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()
    )

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.findBundle("kotest").get())
            }
        }

        val jvmSharedMain by creating {
            dependsOn(commonMain)
        }

        val jvmSharedTest by creating {
            dependsOn(commonTest)
        }
    }
}

dependencies {
    "jvmSharedMainCompileOnly"(kotlin("stdlib"))
    "jvmSharedMainCompileOnly"(libs.findLibrary("kotlinx-coroutines-core").get())
    "jvmSharedTestCompileOnly"(kotlin("stdlib"))
    "jvmSharedTestCompileOnly"(libs.findLibrary("kotlinx-coroutines-core").get())
    "jvmSharedTestCompileOnly"(libs.findLibrary("kotlinx-coroutines-test").get())
    "jvmSharedTestImplementation"(libs.findLibrary("kotest-runner-junit5").get())
}

apply(plugin = "ff4k.jvm")
apply(plugin = "ff4k.android")
apply(plugin = "ff4k.ios")

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_filename" to "disabled",
                "ktlint_standard_no-unused-imports" to "enabled"
            )
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_no-unused-imports" to "enabled"
            )
        )
    }
}
