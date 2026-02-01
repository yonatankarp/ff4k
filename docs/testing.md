# Contract Testing

The `ff4k-contract-test` module provides a suite of reusable tests to verify that your custom `FeatureStore` and `PropertyStore` implementations adhere to the FF4K specification. This ensures consistency and reliability across different storage backends.

## Setup

First, add the contract test dependency to your project.

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    testImplementation("com.yonatankarp:ff4k-contract-test:<version>")
}
```

## Testing a Feature Store

To test your custom `FeatureStore`, create a test class that extends `FeatureStoreContractTest` and implement the `createStore` method.

```kotlin
class MyCustomFeatureStoreTest : FeatureStoreContractTest() {

    // This method is called before each test to provide a fresh store instance
    override suspend fun createStore(): FeatureStore {
        // Return a fresh instance of your store
        // If your store relies on an external DB, ensure it's cleaned up here
        return MyCustomFeatureStore()
    }
}
```

The `FeatureStoreContractTest` will automatically run a comprehensive set of tests covering:
- basic CRUD (Create, Read, Update, Delete)
- toggling features
- group operations (enable/disable group, add/remove from group)
- permissions
- error handling (e.g., throwing correct exceptions for missing features)
- concurrency handling

## Testing Properties

To test custom `Property` implementations, you can extend `PropertyContractTest`.

```kotlin
class MyPropertyTest : PropertyContractTest<String, MyStringProperty>() {
    override val serializer = MyStringProperty.serializer()

    override fun create(
        name: String,
        value: String,
        description: String?,
        fixedValues: Set<String>,
        readOnly: Boolean
    ) = MyStringProperty(name, value, description, fixedValues, readOnly)

    override fun sampleName() = "test-prop"
    override fun sampleValue() = "value"
    override fun otherValueNotInFixedValues() = "other-value"
    override fun fixedValuesIncludingSample(sample: String) = setOf(sample, "other")
}
```

## Testing Flipping Strategies

To test custom `FlippingStrategy` implementations, extend `FlippingStrategyContractTest` and implement the required methods.

```kotlin
class MyCustomStrategyTest : FlippingStrategyContractTest() {

    // Create a strategy instance configured to pass with contextThatShouldPass()
    override fun createStrategyForPassingCase(): FlippingStrategy {
        return MyCustomStrategy(configuredToPass = true)
    }

    // Create a strategy instance configured to fail with contextThatShouldFail()
    override fun createStrategyForFailingCase(): FlippingStrategy {
        return MyCustomStrategy(configuredToPass = false)
    }

    // Context that should result in true when used with createStrategyForPassingCase()
    override fun contextThatShouldPass(): FlippingExecutionContext {
        return FlippingExecutionContext(ContextKeys.USER_ID to "allowed-user")
    }

    // Context that should result in false when used with createStrategyForFailingCase()
    override fun contextThatShouldFail(): FlippingExecutionContext {
        return FlippingExecutionContext(ContextKeys.USER_ID to "denied-user")
    }

    // Expected JSON serialization for the strategy from createStrategyForPassingCase()
    override fun expectedJsonForSampleParams(): String {
        return """{"type":"myCustom","configuredToPass":true}"""
    }
}
```

The contract test provides separate strategy creation methods (`createStrategyForPassingCase` and `createStrategyForFailingCase`) to support strategies that depend on injected dependencies like clocks or random sources. For context-driven strategies where the same instance can both pass and fail based on context, both methods can return the same strategy instance.

The `FlippingStrategyContractTest` will automatically run tests covering:
- Evaluation returns true for passing case
- Evaluation returns false for failing case
- Handling of null feature store
- JSON serialization

## Running Tests

Run your tests as you normally would use Gradle:

```bash
./gradlew test
```
