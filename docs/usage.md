# Usage Guide

This guide covers the core concepts and usage patterns of FF4K.

## Initialization

The entry point for FF4K is the `ff4k` DSL function. It allows you to configure features and properties and returns an `FF4k` instance.

```kotlin
import com.yonatankarp.ff4k.dsl.core.ff4k

val ff4k = ff4k {
    // Configuration block
}
```

### Configuration Options

The `ff4k` function accepts several optional arguments to customize behavior:

```kotlin
val ff4k = ff4k(
    autoCreate = true,                // Automatically create missing features (default: false)
    featureStore = InMemoryFeatureStore(), // Custom feature store (default: InMemory)
    propertyStore = InMemoryPropertyStore() // Custom property store (default: InMemory)
) {
    // ...
}
```

- **`autoCreate`**: If set to `true`, querying a feature that doesn't exist will automatically create it in the store (disabled by default).
- **`featureStore`**: The backend storage for feature flags.
- **`propertyStore`**: The backend storage for properties.

## Defining Features

Features are boolean flags that can be toggled on or off. You can define them within the `features` block.

```kotlin
ff4k {
    features {
        // Minimal definition
        feature("simple-feature")

        // Detailed definition
        feature("advanced-feature") {
            isEnabled = true
            description = "Controls the new dashboard layout"
            group = "ui-beta"
            permissions("ADMIN", "BETA_TESTER")
        }
    }
}
```

### Feature Attributes

- **`uid`**: Unique identifier for the feature.
- **`isEnabled`**: Initial state of the feature (default: `false`).
- **`description`**: Human-readable description.
- **`group`**: Group name for organizing features.
- **`permissions`**: List of roles/permissions required to access the feature.

## Defining Properties

Properties are key-value pairs that can store configuration data. They are strongly typed.

```kotlin
ff4k {
    properties {
        // String property
        property("api-url") {
            value = "https://api.example.com"
        }

        // Integer property with constraints
        property("max-retries") {
            value = 3
            description = "Max API retries"
            fixedValues(1, 3, 5) // Value must be one of these
            readOnly = true // Prevent runtime modification
        }
    }
}
```

## Checking Features

There are several ways to check the status of a feature.

### Boolean Check

The `check` method returns a boolean indicating if the feature is enabled.

```kotlin
if (ff4k.check("dark-mode")) {
    enableDarkMode()
}
```

### Functional Style

The `ifEnabled` and `ifEnabledOrElse` extension functions provide a more functional approach.

```kotlin
// Execute only if enabled
ff4k.ifEnabled("dark-mode") {
    enableDarkMode()
}

// Execute one or the other
ff4k.ifEnabledOrElse("dark-mode",
    enabled = { 
        println("Dark mode is ON") 
    },
    disabled = { 
        println("Dark mode is OFF") 
    }
)
```

## Managing Groups

You can perform operations on entire groups of features.

```kotlin
// Enable all features in the 'ui-beta' group
ff4k.enableGroup("ui-beta")

// Disable all features in the 'ui-beta' group
ff4k.disableGroup("ui-beta")
```

## Runtime Modification

If your store supports it (like the default `InMemoryFeatureStore`), you can modify features at runtime.

```kotlin
// Enable a feature
ff4k.enable("my-feature")

// Disable a feature
ff4k.disable("my-feature")
```
