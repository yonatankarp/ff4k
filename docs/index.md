# FF4K - Feature Flags for Kotlin

FF4K is a Kotlin Multiplatform port of the ideas behind [FF4J](https://ff4j.org/): feature flags, flipping strategies and typed properties behind a small, coroutine-first API.

## Key Features

- **Small API**: one `FF4k` entry point, plain data classes, suspend functions.
- **Flipping Strategies**: gradual rollouts, user targeting and time windows with [built-in strategies](usage/strategies.md), composable with `and`, `or` and `!`.
- **Typed Properties**: `Property<T>` values for String, Int, Long, Double, Boolean, Instant, LocalDate and LocalDateTime.
- **JSON configuration**: load features and properties from a file with `kotlinx.serialization`.
- **Pluggable stores**: in-memory and [SQLite](stores/sqlite.md) out of the box, [custom stores](customization/index.md) verified by contract tests.

> **Alpha.** FF4K is pre-1.0. Public APIs, the JSON format and store schemas change between releases without a deprecation cycle.

## Installation

Use the BOM so all FF4K modules share one version:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.yonatankarp:ff4k-bom:<version>"))
    implementation("com.yonatankarp:ff4k-core")
}
```

Or pin each module: `implementation("com.yonatankarp:ff4k-core:<version>")`.

## Quick Start

```kotlin
suspend fun main() {
    val ff4k = FF4k()
    ff4k.features.put(Feature("dark-mode", enabled = true))

    if (ff4k.check("dark-mode")) {
        // ...
    }
}
```

Continue with the [Basics](usage/basics.md) guide.

## License

Apache License 2.0.
