plugins {
    alias(libs.plugins.kotlin.jvm)
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
    alias(libs.plugins.kotlin.serialization)
}

sourceSets {
    main {
        kotlin.srcDir(project(":ff4k-store-sql-common").file("src/main/kotlin"))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    api(project(":ff4k-core"))

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)

    // Test dependencies
    testImplementation(project(":ff4k-contract-test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.postgresql)
}
