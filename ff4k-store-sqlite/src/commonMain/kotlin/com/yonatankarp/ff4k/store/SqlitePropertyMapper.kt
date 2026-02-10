package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import com.yonatankarp.ff4k.store.sqldelight.sqlite.Properties
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

/**
 * Maps between domain [Property] objects and SQLDelight database rows.
 *
 * Properties are stored with queryable metadata columns (type, description, read_only)
 * plus a `data` column containing the full JSON-serialized property for easy
 * deserialization that supports custom property types.
 *
 * @param customSerializersModule Optional additional serializers for custom [Property]
 *   implementations. These are merged with the built-in FF4k serializers.
 */
internal class SqlitePropertyMapper(
    customSerializersModule: SerializersModule = SerializersModule {},
) {
    private val json = Json {
        serializersModule = ff4kSerializersModule + customSerializersModule
        ignoreUnknownKeys = true
    }

    private val propertySerializer = PolymorphicSerializer(Property::class)

    /**
     * Converts a SQLDelight [Properties] row to a domain [Property].
     *
     * @param row The database row to convert.
     * @return The domain [Property] object.
     */
    fun toDomain(row: Properties): Property<*> = json.decodeFromString(propertySerializer, row.data_)

    /**
     * Serializes a [Property] to its full JSON representation.
     *
     * @param property The property to serialize.
     * @return JSON string for storage in the data column.
     */
    fun encodeData(property: Property<*>): String = json.encodeToString(propertySerializer, property)

    /**
     * Encodes a [Property] to its type discriminator string.
     *
     * @param property The property to encode.
     * @return The type discriminator (e.g., "string", "int", "boolean").
     */
    fun encodeType(property: Property<*>): String {
        val jsonElement = json.encodeToJsonElement(propertySerializer, property)
        return jsonElement.jsonObject["type"]?.jsonPrimitive?.content
            ?: error("Property type discriminator not found")
    }

    /**
     * Encodes a boolean to a Long for database storage.
     *
     * @param readOnly The boolean value to encode.
     * @return 1L if true, 0L if false.
     */
    fun encodeReadOnly(readOnly: Boolean): Long = if (readOnly) 1L else 0L
}
