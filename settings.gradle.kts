pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ff4k"

include(
    ":ff4k-bom",
    ":ff4k-contract-test",
    ":ff4k-core",
    ":ff4k-store-sqlite",
    ":ff4k-store-jdbc",
    ":ff4k-store-r2dbc",
    ":ff4k-store-sql-common",
)
