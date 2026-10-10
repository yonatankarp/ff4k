package com.yonatankarp.ff4k.store.mongodb

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.serialization.ff4kJson
import kotlinx.serialization.json.Json
import org.bson.Document

/**
 * Stores each feature as a JSON document in the [collection] of [database]. Pass a [json] built on
 * `ff4kSerializersModule` to persist custom strategies and property types.
 */
class MongoFeatureStore(
    database: MongoDatabase,
    collection: String = "ff4k_features",
    private val json: Json = ff4kJson,
) : FeatureStore {
    private val documents = MongoDocuments(database.getCollection<Document>(collection))

    override suspend fun get(id: String): Feature? = documents.get(id)?.let { json.decodeFromString(it.data) }

    override suspend fun getAll(): List<Feature> = documents.getAll().map { json.decodeFromString(it) }

    override suspend fun put(feature: Feature) = documents.upsert(feature.id, json.encodeToString(feature))

    override suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature {
        // optimistic locking: retry until our write lands on the version we read
        while (true) {
            val row = documents.get(id) ?: throw FeatureNotFoundException(id)
            val updated = transform(json.decodeFromString(row.data))
            require(updated.id == id) { "Cannot change feature id during update: expected '$id', got '${updated.id}'" }
            if (documents.replaceIfUnchanged(id, row, json.encodeToString(updated))) return updated
        }
    }

    override suspend fun delete(id: String) = documents.delete(id)
}
