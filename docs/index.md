# FF4K - Feature Flags for Kotlin

FF4K is a Kotlin Multiplatform (KMP) implementation of the popular [FF4J](https://ff4j.org/) (Feature Flipping for Java) library. It brings robust feature flagging capabilities to the Kotlin ecosystem, supporting multiplatform projects.

## Key Features

- **Kotlin Multiplatform**: Designed to work across different platforms supported by Kotlin (JVM, Android, Native, etc.).
- **Type-safe Properties**: Strongly typed property definitions (String, Int, Boolean, etc.).
- **Flipping Strategies**: Gradual rollouts, A/B testing, and user targeting with [built-in strategies](usage/strategies.md).
- **Serialization Support**: Built-in support for `kotlinx.serialization`.
- **Extensible**: Easily implement custom storage backends (Redis, SQL, etc.) and [custom strategies](customization/custom-strategy.md).
- **DSL**: Intuitive domain-specific language for configuration.

## Installation

### Using Bill of Materials (BOM) - Recommended

The BOM ensures that all FF4K modules are using compatible versions.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Import the BOM
    implementation(platform("com.yonatankarp:ff4k-bom:<version>"))

    // Add dependencies without versions
    implementation("com.yonatankarp:ff4k-core")
}
```

### Manual Versioning

Alternatively, you can specify the version for each module directly:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.yonatankarp:ff4k-core:<version>")
}
```

## Quick Start

```kotlin
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

Check out the [Usage](usage/basics.md) guide for more details.

## License

This project is licensed under the Apache License 2.0.
