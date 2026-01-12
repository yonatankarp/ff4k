plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.sonarqube)
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

sonar {
    properties {
        property("sonar.organization", "yonatankarp")
        property("sonar.projectKey", "yonatankarp_ff4k")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")

        // Exclude function naming convention rule for test files
        property("sonar.issue.ignore.multicriteria", "e1,e2")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "kotlin:S100")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*Test.kt")
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "kotlin:S100")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*Contract.kt")
    }
}
