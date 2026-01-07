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
    "ff4k-cache",
    "ff4k-core",
    "ff4k-dsl",
    "ff4k-test",
    "ff4k-web",
)
