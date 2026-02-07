import com.yonatankarp.ff4k.buildlogic.areAppleTargetsAvailable
import com.yonatankarp.ff4k.buildlogic.isAndroidSdkAvailable

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
        create("SqliteDatabase") {
            packageName.set("com.yonatankarp.ff4k.store.sqldelight.sqlite")
            srcDirs("src/commonMain/sqldelight/sqlite")
            dialect(libs.sqldelight.dialect.sqlite)
            generateAsync.set(true)
        }
        create("PostgresDatabase") {
            packageName.set("com.yonatankarp.ff4k.store.sqldelight.postgres")
            srcDirs("src/jvmMain/sqldelight/postgres")
            dialect(libs.sqldelight.dialect.postgresql)
            generateAsync.set(true)
        }
        create("MysqlDatabase") {
            packageName.set("com.yonatankarp.ff4k.store.sqldelight.mysql")
            srcDirs("src/jvmMain/sqldelight/mysql")
            dialect(libs.sqldelight.dialect.mysql)
            generateAsync.set(true)
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":ff4k-core"))
            api(libs.sqldelight.runtime)
            api(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.jdbc)
        }

        commonTest.dependencies {
            implementation(project(":ff4k-contract-test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }

        if (isAndroidSdkAvailable()) {
            named("androidUnitTest") {
                dependencies {
                    implementation(libs.sqldelight.driver.android)
                }
            }
        }

        if (areAppleTargetsAvailable()) {
            appleTest.dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
    }
}