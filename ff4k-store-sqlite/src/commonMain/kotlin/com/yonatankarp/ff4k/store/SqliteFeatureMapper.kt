package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import com.yonatankarp.ff4k.store.sqldelight.sqlite.Features
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

/**
 * Maps between domain [Feature] objects and SQLDelight database rows.
 *
 * Handles JSON serialization of complex fields:
 * - `permissions` as JSON array of strings
 * - `flippingStrategy` as polymorphic JSON with type discriminator
 * - `customProperties` as JSON map with polymorphic property values
 *
 * @param customSerializersModule Optional additional serializers for custom [FlippingStrategy]
 *   or [Property] implementations. These are merged with the built-in FF4k serializers.
 */
internal class SqliteFeatureMapper(
    customSerializersModule: SerializersModule = SerializersModule {},
) {
    private val json = Json {
        serializersModule = ff4kSerializersModule + customSerializersModule
        ignoreUnknownKeys = true
    }

    private val permissionsSerializer = SetSerializer(String.serializer())
    private val propertiesSerializer = MapSerializer(
        String.serializer(),
        PolymorphicSerializer(Property::class),
    )

    /**
     * Converts a SQLDelight [Features] row to a domain [Feature].
     *
     * @param row The database row to convert.
     * @return The domain [Feature] object.
     */
    fun toDomain(row: Features): Feature = Feature(
        uid = row.uid,
        isEnabled = row.enabled != 0L,
        group = row.group_name,
        description = row.description,
        permissions = decodePermissions(row.permissions),
        flippingStrategy = row.flipping_strategy?.let { decodeStrategy(it) },
        customProperties = decodeProperties(row.custom_properties),
    )

    /**
     * Encodes a boolean to a Long for database storage.
     *
     * @param enabled The boolean value to encode.
     * @return 1L if true, 0L if false.
     */
    fun encodeEnabled(enabled: Boolean): Long = if (enabled) 1L else 0L

    /**
     * Encodes a [Feature]'s permissions set to JSON string.
     *
     * @param permissions The set of permission strings to encode.
     * @return JSON array string representation.
     */
    fun encodePermissions(permissions: Set<String>): String = json.encodeToString(permissionsSerializer, permissions)

    /**
     * Encodes a [FlippingStrategy] to JSON string with polymorphic type information.
     *
     * @param strategy The strategy to encode, or null.
     * @return JSON string with type discriminator, or null if strategy is null.
     */
    fun encodeStrategy(strategy: FlippingStrategy?): String? = strategy?.let { json.encodeToString(PolymorphicSerializer(FlippingStrategy::class), it) }

    /**
     * Encodes a [Feature]'s custom properties map to JSON string.
     *
     * @param properties The map of properties to encode.
     * @return JSON object string representation.
     */
    fun encodeProperties(properties: Map<String, Property<*>>): String = json.encodeToString(propertiesSerializer, properties)

    private fun decodePermissions(jsonString: String): Set<String> = json.decodeFromString(permissionsSerializer, jsonString)

    private fun decodeStrategy(jsonString: String): FlippingStrategy = json.decodeFromString(PolymorphicSerializer(FlippingStrategy::class), jsonString)

    private fun decodeProperties(jsonString: String): Map<String, Property<*>> = json.decodeFromString(propertiesSerializer, jsonString)
}
