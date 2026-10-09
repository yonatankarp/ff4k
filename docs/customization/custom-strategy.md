# Custom Flipping Strategy

A strategy is a `fun interface`, so a lambda is enough at runtime:

```kotlin
val goldOnly = FlippingStrategy { _, context -> context["tier"] == "gold" }
ff4k.features.put(Feature("vip-lounge", enabled = true, strategy = goldOnly))
```

To store the strategy in JSON or a persistent store, make it a `@Serializable` class with a `@SerialName` and register it in a `SerializersModule` that extends `ff4kSerializersModule`:

```kotlin
@Serializable
@SerialName("tier")
data class TierStrategy(val tiers: Set<String>) : FlippingStrategy {
    override suspend fun evaluate(feature: Feature, context: Map<String, Any>): Boolean =
        context["tier"]?.toString() in tiers
}

val json = Json {
    serializersModule = ff4kSerializersModule + SerializersModule {
        polymorphic(FlippingStrategy::class) { subclass(TierStrategy::class) }
    }
}

val configuration: FF4kConfiguration = json.decodeFromString(text)
val featureStore = SqliteFeatureStore(driver, json)
```

Custom strategies compose with the built-in ones: `TierStrategy(setOf("gold")) and PercentageStrategy(50)`.
