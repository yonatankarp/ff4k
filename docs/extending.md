# Extending FF4K

FF4K is designed to be extensible. You can implement custom storage backends for features and properties to persist your flags in databases, remote services (like Redis, Consul), or local files.

## Implementing a Feature Store

To create a custom feature store, implement the `FeatureStore` interface. For convenience, it is highly recommended to extend `AbstractFeatureStore`, which handles most of the common logic and boilerplate.

### Using AbstractFeatureStore

`AbstractFeatureStore` provides default implementations for group management, permission handling helper methods, and validation. You primarily need to implement the core CRUD operations.

```kotlin
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.store.AbstractFeatureStore

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

## Implementing a Property Store

Similarly, to create a custom property store, implement the `PropertyStore` interface.

```kotlin
import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.property.Property

class MyCustomPropertyStore : PropertyStore {
   // Implement interface...
}
```

## Verifying Your Implementation

It is critical to ensure your custom store behaves correctly. FF4K provides a [Contract Test Suite](testing.md) that you can use to automatically verify your implementation against the expected behavior.

## Registering Your Store

Once implemented, pass your custom store to the `ff4k` configuration function.

```kotlin
suspend fun main() {
    val ff4k = ff4k(
        featureStore = MyCustomFeatureStore(),
        propertyStore = MyCustomPropertyStore()
    ) {
        // ...
    }
}
```
