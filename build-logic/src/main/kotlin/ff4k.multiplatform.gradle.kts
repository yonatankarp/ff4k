import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.diffplug.spotless")
    id("io.kotest")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// shortcut: jvm is the only target for now; add androidTarget()/iosX64() here when a consumer needs them
kotlin {
    jvmToolchain(
        libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()
    )

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findBundle("kotest").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("kotest-runner-junit5").get())
        }
    }
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
