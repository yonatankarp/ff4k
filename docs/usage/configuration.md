# Configuration

FF4K supports loading feature flags and properties from configuration files, making it easy to manage your application's state without changing code.

## The Configuration Object

The `FF4kConfiguration` class represents the complete state of your FF4K instance, including:
- **Settings**: Global behavior like `autoCreate`.
- **Features**: Initial feature flags.
- **Properties**: Initial configuration properties.

## Loading from JSON

FF4K provides a `JsonFF4kConfigurationParser` to load configurations from JSON files or resources.

### Example JSON

```json
{
  "settings": {
    "autoCreate": true
  },
  "features": {
    "dark-mode": {
      "uid": "dark-mode",
      "isEnabled": true,
      "description": "Enable dark mode UI"
    }
  },
  "properties": {
    "api-timeout": {
      "type": "int",
      "name": "api-timeout",
      "value": 5000
    }
  }
}
```

### Loading Resources

You can load a configuration file bundled with your application using `parseConfigurationResource`.

```kotlin
val parser = JsonFF4kConfigurationParser()
val config = parser.parseConfigurationResource("ff4k_config.json")
```

**Platform Specifics:**
- **JVM/Android**: Loads from the classpath (e.g., `src/main/resources`).
- **Native**: Searches in `$FF4K_RESOURCES_PATH/<path>`, then the current directory, then `resources/<path>`.

### Loading Files

You can load a configuration file from the filesystem using `parseConfigurationFile`.

```kotlin
val parser = JsonFF4kConfigurationParser()
val config = parser.parseConfigurationFile("/etc/ff4k/config.json")
```

This method supports absolute paths and relative paths (resolved against the current working directory). Tilde (`~`) expansion is supported for user home directories.

**Note:**
- Tilde expansion only supports `~` and `~/...` (not `~username`).
- An `IllegalArgumentException` is thrown if the home directory cannot be determined.
- Paths containing directory traversal sequences (`..`) are rejected and will cause an `IllegalArgumentException`.

## Initializing FF4K

Once you have an `FF4kConfiguration` object, you can use it to initialize your stores.

```kotlin
val parser = JsonFF4kConfigurationParser()
val config = parser.parseConfigurationResource("ff4k.json")

val ff4k = ff4k(
    featureStore = InMemoryFeatureStore(config),
    propertyStore = InMemoryPropertyStore(config),
    autoCreate = config.settings.autoCreate
)
```

### Auditing Source

You can optionally specify the `source` parameter when initializing `FF4k` to indicate the origin of API calls. This is useful for auditing or logging purposes in your application logic.

```kotlin
val ff4k = ff4k(
    source = FF4k.Source.WebApi // or KotlinApi, EmbeddedServlet, Ssh
) {
    // ...
}
```

This value is informational and does not affect feature evaluation logic.

## Exporting Configuration

You can also export the current configuration state to a JSON string, which is useful for debugging or persisting runtime changes.

```kotlin
suspend fun main() {
    val config = FF4kConfiguration(
        settings = FF4kSettings(autoCreate = ff4k.autoCreate),
        features = ff4k.features(),
        properties = ff4k.properties()
    )

    val json = parser.export(config)
    File("snapshot.json").writeText(json)
}
```
