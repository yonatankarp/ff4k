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
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import com.yonatankarp.ff4k.core.FeatureStore

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

## Testing Properties

To test custom `Property` implementations, you can extend `PropertyContractTest`.

```kotlin
import com.yonatankarp.ff4k.test.contract.property.PropertyContractTest

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

## Running Tests

Run your tests as you normally would using Gradle:

```bash
./gradlew test
```
