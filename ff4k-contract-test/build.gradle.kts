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
            api(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        androidMain {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}
