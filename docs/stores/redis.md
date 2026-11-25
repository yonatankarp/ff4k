# Redis Store

`ff4k-store-redis` provides `RedisFeatureStore` and `RedisPropertyStore` on top of a [Lettuce](https://redis.github.io/lettuce/) connection. Each store keeps one Redis hash, `ff4k:features` and `ff4k:properties` by default, with one JSON document per feature or property. Listing a store reads its hash; it never scans the keyspace with `KEYS`.

Calls are suspend functions on Lettuce's non-blocking API, so they do not block a thread.

## Installation

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-redis:<version>")   // brings io.lettuce:lettuce-core
}
```

## Usage

```kotlin
val connection = RedisClient.create("redis://localhost:6379").connect()   // one shared, thread-safe connection

val ff4k = FF4k(
    features = RedisFeatureStore(connection).cached(),
    properties = RedisPropertyStore(connection).cached(),
)
```

Wrap the stores with [`cached()`](../usage/basics.md#caching) so flag checks do not hit Redis on every call.

To run several applications against one Redis server, give each its own hash keys:

```kotlin
RedisFeatureStore(connection, key = "billing:ff4k:features")
RedisPropertyStore(connection, key = "billing:ff4k:properties")
```

## Atomic updates

`update` (and with it `enable`, `disable` and the group helpers) reads the feature, applies the transform, and writes the result with a Lua script that only replaces the document if it is still the one that was read. If another writer got there first, the update retries on the new value, so concurrent updates from any number of callers or instances never lose a write. `MULTI`/`WATCH` is not used because it does not isolate callers sharing one connection.

Redis Cluster and cache invalidation through pub/sub are not supported yet.

## Custom strategies and property types

Pass the same `Json` built on `ff4kSerializersModule` to both `RedisFeatureStore` and `RedisPropertyStore`, exactly as for the [SQLite store](sqlite.md#custom-strategies-and-property-types):

```kotlin
RedisFeatureStore(connection, json = myJson)
RedisPropertyStore(connection, json = myJson)
```
