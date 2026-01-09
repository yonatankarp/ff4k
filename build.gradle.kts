plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

dependencies {
    // Code coverage
    subprojects
        .filter { it.name.startsWith("ff4k-") }
        .filter { it.name.contains("test").not() }
        .forEach { kover(it) }

    // Documentation
    subprojects
        .filter { it.name.startsWith("ff4k-") }
        .forEach { dokka(it) }
}
