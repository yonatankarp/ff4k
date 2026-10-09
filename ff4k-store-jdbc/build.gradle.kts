plugins {
    id("ff4k.multiplatform")
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(project(":ff4k-core"))
        }

        jvmTest.dependencies {
            implementation(project(":ff4k-contract-test"))
            implementation(libs.sqlite.jdbc)
            implementation(project.dependencies.platform(libs.testcontainers.bom))
            implementation(libs.bundles.testcontainers)
        }
    }
}
