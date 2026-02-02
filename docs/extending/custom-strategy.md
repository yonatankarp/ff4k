# Implementing a Flipping Strategy

To create a custom flipping strategy, implement the `FlippingStrategy` interface. For an overview
of built-in strategies, see [Flipping Strategies](../usage/strategies.md).

```kotlin
@Serializable
@SerialName("timeBasedStrategy")
data class TimeBasedStrategy(
    val startHour: Int,
    val endHour: Int
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext
    ): Boolean {
        val currentHour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
        return currentHour in startHour until endHour
    }
}
```

## Verifying Your Implementation

It is critical to ensure your custom strategy behaves correctly. FF4K provides a [Contract Test Suite](../usage/testing.md) that you can use to automatically verify your implementation against the expected behavior.

```kotlin
class TimeBasedStrategyTest : FlippingStrategyContractTest() {
    
    // Create a strategy that should PASS given the current conditions (or context)
    override fun createStrategyForPassingCase(): FlippingStrategy {
        // Assuming we mock the clock or configure the strategy to span the current time
        return TimeBasedStrategy(startHour = 0, endHour = 24) 
    }

    // Create a strategy that should FAIL
    override fun createStrategyForFailingCase(): FlippingStrategy {
        // 23 until 1 creates an empty range in Kotlin, so this will always fail
        return TimeBasedStrategy(startHour = 23, endHour = 1)
    }

    // Provide context if your strategy relies on it (optional for time-based)
    override fun contextThatShouldPass() = FlippingExecutionContext()
    override fun contextThatShouldFail() = FlippingExecutionContext()

    // Verify JSON serialization
    override fun expectedJsonForSampleParams(): String {
        return """{"type":"timeBasedStrategy","startHour":0,"endHour":24}"""
    }
}
```

## Registering for Serialization

If you need JSON serialization support for your custom strategy, register it in a custom
serializers module and combine it with the built-in module. See [Serialization](../usage/basics.md#serialization)
for more details on `ff4kSerializersModule`.

```kotlin
val customSerializersModule = SerializersModule {
    polymorphic(FlippingStrategy::class) {
        subclass(TimeBasedStrategy::class, TimeBasedStrategy.serializer())
    }
}

val json = Json {
    serializersModule = ff4kSerializersModule + customSerializersModule
}
```
