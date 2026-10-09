# Contract Testing

The `ff4k-contract-test` module provides a suite of reusable tests to verify that your custom `FeatureStore` and `PropertyStore` implementations adhere to the FF4K specification. This ensures consistency and reliability across different storage backends.

## Setup

First, add the contract test dependency to your project.

**Gradle (Kotlin DSL)**

If you are using the [FF4K BOM](../index.md#installation), you don't need to specify the version:

```kotlin
dependencies {
    testImplementation("com.yonatankarp:ff4k-contract-test")
}
```

Otherwise, specify the version explicitly:

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

`FeatureStoreContractTest` runs tests covering:
- `get`, `getAll`, `put`, `update` and `delete`
- the `enable`, `disable` and group extension functions
- error handling (`FeatureNotFoundException`, id changes during `update`)
- concurrent `update` calls not losing writes

## Testing a Property Store

Similar to features, you can test your custom `PropertyStore` by extending `PropertyStoreContractTest`.

```kotlin
class MyCustomPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore {
        return MyCustomPropertyStore()
    }
}
```

