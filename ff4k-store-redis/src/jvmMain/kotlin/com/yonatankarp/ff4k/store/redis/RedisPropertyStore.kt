package com.yonatankarp.ff4k.store.redis

import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.serialization.PropertySerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json

/** Stores each property as a JSON document in the Redis hash [key] (field = property name). */
class RedisPropertyStore(
    connection: StatefulRedisConnection<String, String>,
    private val key: String = "ff4k:properties",
    private val json: Json = ff4kJson,
) : PropertyStore {
    private val redis = connection.async()

    override suspend fun get(name: String): Property<Any>? = redis.hget(key, name).await()?.let { json.decodeFromString(PropertySerializer, it) }

    override suspend fun getAll(): List<Property<Any>> = redis.hvals(key).await().map { json.decodeFromString(PropertySerializer, it) }

    override suspend fun put(property: Property<Any>) {
        redis.hset(key, property.name, json.encodeToString(PropertySerializer, property)).await()
    }

    override suspend fun delete(name: String) {
        redis.hdel(key, name).await()
    }
}
