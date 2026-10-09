# Configuration

`FF4kConfiguration` is a serializable snapshot of features and properties. Load it from JSON and hand it to `FF4k`, which seeds in-memory stores from it.

```kotlin
val configuration = FF4kConfiguration.fromJson(File("ff4k.json").readText())
val ff4k = FF4k(configuration)
```

## JSON format

```json
{
  "features": [
    {
      "id": "dark-mode",
      "enabled": true,
      "description": "Enable dark mode theme",
      "group": "ui",
      "permissions": ["ADMIN"],
      "strategy": { "type": "userPercentage", "percentage": 10 },
      "properties": [
        { "name": "contrast", "type": "double", "value": 0.8 }
      ]
    }
  ],
  "properties": [
    { "name": "max-retries", "type": "int", "value": 3, "description": "Retries" }
  ]
}
```

Every field of a feature except `id` is optional. A strategy is identified by its `type` (see [Strategies](strategies.md)); a property by its `type` (see [Properties](properties.md)).

## Exporting

```kotlin
val json = FF4kConfiguration(
    features = ff4k.features.getAll(),
    properties = ff4k.properties.getAll(),
).toJson()
```

## Custom strategies in JSON

`ff4kJson` is the `Json` instance used by `fromJson` and `toJson`. To parse your own strategies, build a `Json` whose `serializersModule` extends `ff4kSerializersModule` and call it directly:

```kotlin
val json = Json {
    serializersModule = ff4kSerializersModule + SerializersModule {
        polymorphic(FlippingStrategy::class) { subclass(MyStrategy::class) }
    }
}
val configuration: FF4kConfiguration = json.decodeFromString(text)
```
