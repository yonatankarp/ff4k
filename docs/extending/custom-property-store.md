# Implementing a Property Store

To create a custom property store, implement the `PropertyStore` interface.

```kotlin
class MyCustomPropertyStore : PropertyStore {
   // Implement interface...
}
```

## Verifying Your Implementation

It is critical to ensure your custom store behaves correctly. FF4K provides a [Contract Test Suite](../usage/testing.md) that you can use to automatically verify your implementation against the expected behavior.

```kotlin
class MyCustomPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore {
        // Return a fresh instance of your store for each test
        return MyCustomPropertyStore()
    }
}
```

This will run a suite of tests covering CRUD operations, concurrency, and error handling.

## Registering Your Store

Once implemented, pass your custom store to the `ff4k` configuration function.

```kotlin
suspend fun main() {
    val ff4k = ff4k(
        propertyStore = MyCustomPropertyStore()
        // ...
    ) {
        // ...
    }
}
```
