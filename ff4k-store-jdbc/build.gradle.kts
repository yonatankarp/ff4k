plugins {
    alias(libs.plugins.kotlin.jvm)
    id("ff4k.publish")
    id("ff4k.coverage")
    id("ff4k.documentation")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

fun registerDatabase(
    name: String,
    dialect: Provider<MinimalExternalModuleDependency>,
) {
    val folder = name.lowercase()
    sqldelight {
        databases {
            create("${name}Database") {
                packageName.set("com.yonatankarp.ff4k.store.sqldelight.$folder.jdbc")
                val commonProject = project(":ff4k-store-sql-common")
                srcDirs(commonProject.layout.projectDirectory.dir("src/main/sqldelight/$folder"))
                this.dialect(dialect)
                generateAsync.set(false)
            }
        }
    }
}

registerDatabase(name = "Postgres", dialect = libs.sqldelight.dialect.postgresql)
registerDatabase(name = "Mysql", dialect = libs.sqldelight.dialect.mysql)

dependencies {
    api(project(":ff4k-core"))
    implementation(libs.sqldelight.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.driver.jdbc)
}