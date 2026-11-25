pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ff4k"

include(
    ":ff4k-bom",
    ":ff4k-contract-test",
    ":ff4k-core",
    ":ff4k-store-jdbc",
    ":ff4k-store-mongodb",
    ":ff4k-store-sqlite",
)
