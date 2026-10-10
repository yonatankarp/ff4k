package com.yonatankarp.ff4k.store.redis

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.serialization.ff4kJson
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json

/**
 * Stores each feature as a JSON document in the Redis hash [key] (field = feature id). Give each
 * application its own [key] to share one Redis server. Pass a [json] built on `ff4kSerializersModule`
 * to persist custom strategies and property types.
 */
class RedisFeatureStore(
    connection: StatefulRedisConnection<String, String>,
    private val key: String = "ff4k:features",
    private val json: Json = ff4kJson,
) : FeatureStore {
    private val redis = connection.async()

    override suspend fun get(id: String): Feature? = redis.hget(key, id).await()?.let { json.decodeFromString(it) }

    override suspend fun getAll(): List<Feature> = redis.hvals(key).await().map { json.decodeFromString(it) }

    override suspend fun put(feature: Feature) {
        redis.hset(key, feature.id, json.encodeToString(feature)).await()
    }

    override suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature {
        // optimistic locking: the script writes only if the stored document is still the one we read
        while (true) {
            val current = redis.hget(key, id).await() ?: throw FeatureNotFoundException(id)
            val updated = transform(json.decodeFromString(current))
            require(updated.id == id) { "Cannot change feature id during update: expected '$id', got '${updated.id}'" }
            val written = redis.eval<Long>(COMPARE_AND_SET, ScriptOutputType.INTEGER, arrayOf(key), id, current, json.encodeToString(updated)).await()
            if (written == 1L) return updated
        }
    }

    override suspend fun delete(id: String) {
        redis.hdel(key, id).await()
    }

    private companion object {
        // a missing field reads as false, so a concurrent delete fails the swap and the retry throws FeatureNotFoundException
        const val COMPARE_AND_SET = """
            if redis.call('HGET', KEYS[1], ARGV[1]) == ARGV[2] then
                redis.call('HSET', KEYS[1], ARGV[1], ARGV[3])
                return 1
            end
            return 0
        """
    }
}
