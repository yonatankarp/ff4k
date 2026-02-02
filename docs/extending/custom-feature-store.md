# Implementing a Feature Store

To create a custom feature store, implement the `FeatureStore` interface. For convenience, it is highly recommended to extend `AbstractFeatureStore`, which handles most of the common logic and boilerplate.

## Using AbstractFeatureStore

`AbstractFeatureStore` provides default implementations for group management, permission handling helper methods, and validation. You primarily need to implement the core CRUD operations.

```kotlin
class MyCustomFeatureStore : AbstractFeatureStore() {

    // Helper map to simulate a DB for this example
    private val db = mutableMapOf<String, Feature>()

    override suspend fun get(featureId: String): Feature? {
        return db[featureId]
    }

    override suspend fun getAll(): Map<String, Feature> {
        return db.toMap()
    }

    // Using the 'plusAssign' operator for creation (store += feature)
    override suspend fun plusAssign(feature: Feature) {
        if (feature.uid in db) {
            throw FeatureAlreadyExistsException(feature.uid)
        }
        db[feature.uid] = feature
    }

    // Using the 'minusAssign' operator for deletion (store -= featureId)
    override suspend fun minusAssign(featureId: String) {
         if (featureId !in db) {
            throw FeatureNotFoundException(featureId)
        }
        db.remove(featureId)
    }

    override suspend fun update(feature: Feature) {
         if (feature.uid !in db) {
            throw FeatureNotFoundException(feature.uid)
        }
        db[feature.uid] = feature
    }

    override suspend fun clear() {
        db.clear()
    }
    
    // ... implement other abstract methods ...
}
```

## Verifying Your Implementation

It is critical to ensure your custom store behaves correctly. FF4K provides a [Contract Test Suite](../usage/testing.md) that you can use to automatically verify your implementation against the expected behavior.

```kotlin
class MyCustomFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore {
        // Return a fresh instance of your store for each test
        return MyCustomFeatureStore()
    }
}
```

This will run a suite of tests covering CRUD operations, toggling features, group operations, permissions, and concurrency.

## Registering Your Store

Once implemented, pass your custom store to the `ff4k` configuration function.

```kotlin
suspend fun main() {
    val ff4k = ff4k(
        featureStore = MyCustomFeatureStore(),
        // ...
    ) {
        // ...
    }
}
```
