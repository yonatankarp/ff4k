package com.yonatankarp.ff4k.store

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.serialization.PropertySerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import kotlinx.serialization.json.Json

/** Stores each property as a JSON document keyed by name. Shares the schema of [SqliteFeatureStore]. */
class SqlitePropertyStore(
    driver: SqlDriver,
    private val json: Json = ff4kJson,
) : PropertyStore {
    private val queries = SqliteDatabase(driver).propertyQueries

    override suspend fun get(name: String): Property<Any>? = queries.selectByName(name).awaitAsOneOrNull()?.let { json.decodeFromString(PropertySerializer, it.data_) }

    override suspend fun getAll(): List<Property<Any>> = queries.selectAll().awaitAsList().map { json.decodeFromString(PropertySerializer, it.data_) }

    override suspend fun put(property: Property<Any>) {
        queries.upsert(property.name, json.encodeToString(PropertySerializer, property))
    }

    override suspend fun delete(name: String) {
        queries.deleteByName(name)
    }
}
