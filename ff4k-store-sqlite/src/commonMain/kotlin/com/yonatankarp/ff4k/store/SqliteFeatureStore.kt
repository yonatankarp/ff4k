package com.yonatankarp.ff4k.store

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.serialization.ff4kJson
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import kotlinx.serialization.json.Json

/**
 * Stores each feature as a JSON document keyed by id. Create the schema with
 * `SqliteDatabase.Schema.create(driver)` before first use. Pass a [json] built on
 * `ff4kSerializersModule` to persist custom strategies.
 */
class SqliteFeatureStore(
    driver: SqlDriver,
    private val json: Json = ff4kJson,
) : FeatureStore {
    private val queries = SqliteDatabase(driver).featureQueries

    override suspend fun get(id: String): Feature? = queries.selectById(id).awaitAsOneOrNull()?.let { json.decodeFromString(it.data_) }

    override suspend fun getAll(): List<Feature> = queries.selectAll().awaitAsList().map { json.decodeFromString(it.data_) }

    override suspend fun put(feature: Feature) {
        queries.upsert(feature.id, json.encodeToString(feature))
    }

    override suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature {
        // optimistic locking: retry until our write lands on the version we read
        while (true) {
            val row = queries.selectById(id).awaitAsOneOrNull() ?: throw FeatureNotFoundException(id)
            val updated = transform(json.decodeFromString(row.data_))
            require(updated.id == id) { "Cannot change feature id during update: expected '$id', got '${updated.id}'" }
            var written = 0L
            queries.transaction {
                queries.updateIfVersion(data = json.encodeToString(updated), expectedVersion = row.version, id = id)
                written = queries.changes().awaitAsOne()
            }
            if (written > 0) return updated
        }
    }

    override suspend fun delete(id: String) {
        queries.deleteById(id)
    }
}
