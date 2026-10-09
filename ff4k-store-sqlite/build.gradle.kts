plugins {
    id("ff4k.multiplatform")
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("SqliteDatabase") {
            packageName.set("com.yonatankarp.ff4k.store.sqldelight.sqlite")
            dialect(libs.sqldelight.dialect.sqlite)
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
        }

        commonTest.dependencies {
            implementation(project(":ff4k-contract-test"))
        }

        jvmTest.dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }
    }
}
