package com.yonatankarp.ff4k.store.mongodb

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.serialization.PropertySerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import kotlinx.serialization.json.Json
import org.bson.Document

/** Stores each property as a JSON document in the [collection] of [database]. */
class MongoPropertyStore(
    database: MongoDatabase,
    private val json: Json = ff4kJson,
    collection: String = "ff4k_properties",
) : PropertyStore {
    private val documents = MongoDocuments(database.getCollection<Document>(collection))

    override suspend fun get(name: String): Property<Any>? = documents.get(name)?.let { json.decodeFromString(PropertySerializer, it.data) }

    override suspend fun getAll(): List<Property<Any>> = documents.getAll().map { json.decodeFromString(PropertySerializer, it) }

    override suspend fun put(property: Property<Any>) = documents.upsert(property.name, json.encodeToString(PropertySerializer, property))

    override suspend fun delete(name: String) = documents.delete(name)
}
