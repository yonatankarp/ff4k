# Custom Feature Store

`FeatureStore` has five suspend functions:

```kotlin
interface FeatureStore {
    suspend fun get(id: String): Feature?
    suspend fun getAll(): List<Feature>
    suspend fun put(feature: Feature)                                  // insert or replace
    suspend fun update(id: String, transform: (Feature) -> Feature): Feature
    suspend fun delete(id: String)                                     // no-op when missing
}
```

Rules every implementation must follow (the [contract tests](testing.md) check them):

- `get` returns null for unknown ids.
- `update` is atomic against concurrent updates, throws `FeatureNotFoundException` for unknown ids, and rejects a transform that changes the id with `IllegalArgumentException`.
- `enable`, `disable`, `group`, `enableGroup` and `disableGroup` are extension functions built on `update` and `getAll`; you do not implement them.

Serialize a `Feature` with `ff4kJson` (or a `Json` whose module extends `ff4kSerializersModule`) when your backend stores text:

```kotlin
class RedisFeatureStore(private val redis: RedisCommands) : FeatureStore {
    override suspend fun get(id: String): Feature? =
        redis.get("ff4k:feature:$id")?.let { ff4kJson.decodeFromString(it) }

    override suspend fun getAll(): List<Feature> =
        redis.keys("ff4k:feature:*").mapNotNull { redis.get(it) }.map { ff4kJson.decodeFromString(it) }

    override suspend fun put(feature: Feature) {
        redis.set("ff4k:feature:${feature.id}", ff4kJson.encodeToString(feature))
    }

    override suspend fun update(id: String, transform: (Feature) -> Feature): Feature {
        // use WATCH/MULTI or a version field so concurrent updates do not lose writes
        val updated = transform(get(id) ?: throw FeatureNotFoundException(id))
        require(updated.id == id)
        put(updated)
        return updated
    }

    override suspend fun delete(id: String) {
        redis.del("ff4k:feature:$id")
    }
}
```
