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
            api(libs.mongodb.driver.kotlin.coroutine)
        }

        jvmTest.dependencies {
            implementation(project(":ff4k-contract-test"))
            implementation(project.dependencies.platform(libs.testcontainers.bom))
            implementation(libs.testcontainers.mongodb)
        }
    }
}
