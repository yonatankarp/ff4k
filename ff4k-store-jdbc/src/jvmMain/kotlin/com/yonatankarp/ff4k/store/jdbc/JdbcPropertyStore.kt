package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.serialization.PropertySerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import kotlinx.serialization.json.Json
import javax.sql.DataSource

/** Stores each property as a JSON document in [JdbcSchema.PROPERTIES_TABLE]. Shares the schema of [JdbcFeatureStore]. */
class JdbcPropertyStore(
    dataSource: DataSource,
    dialect: JdbcDialect,
    private val json: Json = ff4kJson,
) : PropertyStore {
    private val documents = JdbcDocuments(dataSource, JdbcSchema.PROPERTIES_TABLE, dialect)

    override suspend fun get(name: String): Property<Any>? = documents.get(name)?.let { json.decodeFromString(PropertySerializer, it.data) }

    override suspend fun getAll(): List<Property<Any>> = documents.getAll().map { json.decodeFromString(PropertySerializer, it) }

    override suspend fun put(property: Property<Any>) = documents.upsert(property.name, json.encodeToString(PropertySerializer, property))

    override suspend fun delete(name: String) = documents.delete(name)
}
