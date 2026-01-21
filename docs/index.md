# FF4K - Feature Flags for Kotlin

FF4K is a Kotlin Multiplatform (KMP) implementation of the popular [FF4J](https://ff4j.org/) (Feature Flipping for Java) library. It brings robust feature flagging capabilities to the Kotlin ecosystem, supporting multiplatform projects.

## Key Features

- **Kotlin Multiplatform**: Designed to work across different platforms supported by Kotlin (JVM, Android, Native, etc.).
- **Type-safe Properties**: Strongly typed property definitions (String, Int, Boolean, etc.).
- **Serialization Support**: Built-in support for `kotlinx.serialization`.
- **Extensible**: Easily implement custom storage backends (Redis, SQL, etc.).
- **DSL**: Intuitive Domain Specific Language for configuration.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-core:<version>")
}
```

## Quick Start

```kotlin
import com.yonatankarp.ff4k.dsl.core.ff4k

suspend fun main() {
    val ff4k = ff4k {
        features {
            feature("dark-mode") {
                isEnabled = true
                description = "Enable dark mode theme"
            }
        }
    }

    // Check feature status
    ff4k.ifEnabled("dark-mode") {
        // ...
    }
}
```

Check out the [Usage](usage.md) guide for more details.

## License

This project is licensed under the Apache License 2.0.
