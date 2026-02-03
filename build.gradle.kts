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
        .filter { "test" !in it.name }
        .filter { "ff4k-bom" !in it.name }
        .forEach { kover(it) }

    // Documentation
    subprojects
        .filter { it.name.startsWith("ff4k-") }
        .filter { "ff4k-bom" !in it.name }
        .forEach { dokka(it) }
}
