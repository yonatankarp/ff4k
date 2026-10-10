# MongoDB Store

`ff4k-store-mongodb` provides `MongoFeatureStore` and `MongoPropertyStore` on the official [MongoDB Kotlin coroutine driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/). Each feature and property is one document `{ _id, data, version }` whose `data` field holds the same JSON the other stores persist. Collections default to `ff4k_features` and `ff4k_properties` and are created by MongoDB on first write.

## Installation

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-mongodb:<version>")
}
```

The module brings in `org.mongodb:mongodb-driver-kotlin-coroutine` as an API dependency.

## Usage

```kotlin
val client = MongoClient.create("mongodb://localhost:27017")
val database = client.getDatabase("app")

val ff4k = FF4k(
    features = MongoFeatureStore(database).cached(),
    properties = MongoPropertyStore(database).cached(),
)
```

Pass `collection = "..."` to either store to use another collection name.

Wrap the stores with [`cached()`](../usage/basics.md#caching) so flag checks do not hit the database on every call.

## Atomic updates

`MongoFeatureStore.update` uses optimistic locking: it reads the document and its `version`, applies the transform, and writes back only if `version` is unchanged, retrying otherwise. Concurrent updates to the same feature are never lost, and no transaction or replica set is required.

## Custom strategies and property types

Pass the same `Json` built on `ff4kSerializersModule` to both `MongoFeatureStore` and `MongoPropertyStore`, exactly as for the [SQLite store](sqlite.md#custom-strategies-and-property-types):

```kotlin
val json = Json {
    serializersModule = ff4kSerializersModule + SerializersModule {
        polymorphic(FlippingStrategy::class) { subclass(MyStrategy::class) }
    }
}
val featureStore = MongoFeatureStore(database, json)
val propertyStore = MongoPropertyStore(database, json)
```
