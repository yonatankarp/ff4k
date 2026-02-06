plugins {
    id("ff4k.multiplatform")
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("FF4kDatabase") {
            packageName.set("com.yonatankarp.ff4k.store.sqldelight")
            generateAsync.set(true)
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":ff4k-core"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(project(":ff4k-contract-test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }
    }
}
