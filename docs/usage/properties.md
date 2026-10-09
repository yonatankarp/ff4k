# Properties

A `Property<T>` is a named, typed value with an optional description.

```kotlin
ff4k.properties.put(Property("max-retries", 3, description = "Retries"))
ff4k.properties.put(Property("launch", Instant.parse("2026-06-01T00:00:00Z")))

val retries: Int? = ff4k.property("max-retries")   // null when missing or not an Int
val raw: Property<Any>? = ff4k.properties.get("max-retries")

ff4k.properties.getAll()
ff4k.properties.delete("max-retries")
```

## Supported value types

Properties serialize to `{"name", "type", "value", "description"}`. The `type` field selects the Kotlin type:

| `type`          | Kotlin type                   | JSON value |
|-----------------|-------------------------------|------------|
| `string`        | `String`                      | string     |
| `int`           | `Int`                         | number     |
| `long`          | `Long`                        | number     |
| `double`        | `Double`                      | number     |
| `boolean`       | `Boolean`                     | boolean    |
| `instant`       | `kotlin.time.Instant`         | ISO-8601 string |
| `localDate`     | `kotlinx.datetime.LocalDate`  | ISO-8601 string |
| `localDateTime` | `kotlinx.datetime.LocalDateTime` | ISO-8601 string |

In-memory stores accept any value type. For JSON configuration and persistent stores, any other value type must be a `@Serializable` class registered under `polymorphic(Any::class)`; its serial name becomes the `type`:

```kotlin
@Serializable
@SerialName("version")
data class Version(val major: Int, val minor: Int)

val json = Json {
    serializersModule = ff4kSerializersModule + SerializersModule {
        polymorphic(Any::class) { subclass(Version::class) }
    }
}
val configuration: FF4kConfiguration = json.decodeFromString(text)   // {"name": "min", "type": "version", "value": {"major": 1, "minor": 2}}
val featureStore = SqliteFeatureStore(driver, json)
```

Scalar wrappers (value classes, enums) cannot be registered this way because `kotlinx.serialization` only allows class-shaped values in `polymorphic(Any::class)`.

## Feature properties

A feature can carry its own properties:

```kotlin
val feature = Feature("dark-mode", properties = listOf(Property("contrast", 0.8)))
feature.property("contrast")?.value   // 0.8
```
