# FF4K - Feature Flags for Kotlin

<div align="center">

[![CI](https://github.com/yonatankarp/ff4k/actions/workflows/ci.yml/badge.svg)](https://github.com/yonatankarp/ff4k/actions/workflows/ci.yml)
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-17-orange.svg?logo=openjdk)](https://openjdk.org/)
[![GitHub release](https://img.shields.io/github/v/release/yonatankarp/ff4k)](https://github.com/yonatankarp/ff4k/releases)
[![CodeRabbit Reviews](https://img.shields.io/coderabbit/prs/github/yonatankarp/ff4k?utm_source=oss&utm_medium=github&utm_campaign=yonatankarp%2Fff4k&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)](https://coderabbit.ai)

### Code Quality

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=coverage)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=bugs)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)

</div>

FF4K is a Kotlin Multiplatform (KMP) implementation of the popular [FF4J](https://ff4j.org/) (Feature Flipping for Java) library. It brings robust feature flagging capabilities to the Kotlin ecosystem, supporting multiplatform projects.

## Features

- **Kotlin Multiplatform**: Designed to work across different platforms supported by Kotlin.
- **Type-safe Properties**: Strongly typed property definitions (String, Int, Boolean, etc.).
- **Serialization Support**: Built-in support for `kotlinx.serialization`.

## Usage

### 1. Initialization & Configuration

Use the `ff4k` DSL to configure the library. This allows you to define features and properties in a structured, type-safe way.

```kotlin
import com.yonatankarp.ff4k.dsl.core.ff4k

val ff4k = ff4k {
    // Define features
    features {
        feature("dark-mode") {
            isEnabled = true
            description = "Enable dark mode theme"
            group = "ui-experiments"
        }

        feature("beta-dashboard") {
            isEnabled = false
            permissions("ADMIN", "BETA_USER")
        }
    }

    // Define properties
    properties {
        property("max-retries") {
            value = 3
            description = "Maximum API retry attempts"
            readOnly = true
        }

        property("api-url") {
            value = "https://api.example.com"
        }
    }
}
```

### 2. Custom Stores & Auto-Create

You can configure storage backends and behavior via the `ff4k` function arguments.

```kotlin
val ff4k = ff4k(
    autoCreate = true, // Auto-create missing features as disabled
    featureStore = InMemoryFeatureStore(), // Default
    propertyStore = InMemoryPropertyStore() // Default
) {
    // ... configuration block
}
```

### 3. Checking Feature Flags

Use the idiomatic `ifEnabled` and `ifEnabledOrElse` functions for cleaner conditional logic.

```kotlin
// Execute a block if the feature is enabled
ff4k.ifEnabled("dark-mode") {
    enableDarkMode()
}

// Execute one block if enabled, another if disabled
ff4k.ifEnabledOrElse("dark-mode",
    enabled = { enableDarkMode() },
    disabled = { enableLightMode() }
)
```

### 4. Retrieving Properties

Access properties safely with type conversion.

```kotlin
// Retrieve property object and access its value
val retries: Int? = ff4k.property<Int>("max-retries")?.value
val apiUrl: String? = ff4k.property<String>("api-url")?.value
```

### 5. Managing Groups

Enable or disable entire groups of features.

```kotlin
// Enable all features in the 'ui-experiments' group
ff4k.enableGroup("ui-experiments")

// Disable all features in the 'ui-experiments' group
ff4k.disableGroup("ui-experiments")
```

## License

This project is licensed under the Apache License 2.0.
