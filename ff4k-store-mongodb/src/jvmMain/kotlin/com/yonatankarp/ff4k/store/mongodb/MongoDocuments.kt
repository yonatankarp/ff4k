package com.yonatankarp.ff4k.store.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.inc
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.Document

/** One JSON document per item, stored as `{ _id, data, version }`. */
internal class MongoDocuments(private val collection: MongoCollection<Document>) {
    class Row(val data: String, val version: Long)

    suspend fun get(id: String): Row? = collection.find(eq("_id", id)).firstOrNull()?.let {
        Row(it.getString("data"), (it["version"] as Number).toLong())
    }

    suspend fun getAll(): List<String> = collection.find().map { it.getString("data") }.toList()

    suspend fun upsert(id: String, data: String) {
        collection.updateOne(eq("_id", id), combine(set("data", data), inc("version", 1L)), UpdateOptions().upsert(true))
    }

    /**
     * Writes [data] only if the document is still exactly [expected], version and content; returns whether it
     * did. The content check matters because a deleted and recreated document starts again at version 1.
     */
    suspend fun replaceIfUnchanged(id: String, expected: Row, data: String): Boolean = collection.updateOne(
        and(eq("_id", id), eq("version", expected.version), eq("data", expected.data)),
        combine(set("data", data), set("version", expected.version + 1)),
        UpdateOptions().collation(BINARY),
    ).matchedCount > 0

    private companion object {
        // byte-wise comparison, so a collection's case-insensitive default collation cannot match a changed document
        val BINARY: Collation = Collation.builder().locale("simple").build()
    }

    suspend fun delete(id: String) {
        collection.deleteOne(eq("_id", id))
    }
}
