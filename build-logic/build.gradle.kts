plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.maven.publish.gradle.plugin)
    implementation(libs.kotest.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
}