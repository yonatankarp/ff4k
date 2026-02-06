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
    "ff4k-bom",
    "ff4k-contract-test",
    "ff4k-core",
    "ff4k-store-sqldelight",
)
