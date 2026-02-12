plugins {
    id("ff4k.multiplatform")
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":ff4k-core"))
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.coroutines.test)
            api(libs.bundles.bignum)
            api(libs.bundles.kotest)
        }
    }
}

/*
 * This module provides contract tests for other modules to consume
 * It doesn't have its own tests, so disable failing on no discovered tests.
 */
tasks.withType<Test> {
    failOnNoDiscoveredTests = false
}

/*
 * Disable coverage verification - this module has no tests of its own,
 * coverage is measured when other modules use these contract tests.
 */
kover {
    disable()
}
