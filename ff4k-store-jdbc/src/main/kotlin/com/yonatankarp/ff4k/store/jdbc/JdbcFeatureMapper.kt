package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import java.sql.ResultSet

/**
 * Represents a feature row from the database.
 */
internal data class FeatureRow(
    val uid: String,
    val enabled: Long,
    val groupName: String?,
    val description: String?,
    val permissions: String,
    val flippingStrategy: String?,
    val customProperties: String,
    val version: Long,
)

/**
 * Maps between domain [Feature] objects and JDBC database rows.
 *
 * Handles JSON serialization of complex fields:
 * - `permissions` as JSON array of strings
 * - `flippingStrategy` as polymorphic JSON with type discriminator
 * - `customProperties` as JSON map with polymorphic property values
 *
 * @param customSerializersModule Optional additional serializers for custom [FlippingStrategy]
 *   or [Property] implementations. These are merged with the built-in FF4k serializers.
 */
internal class JdbcFeatureMapper(
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
     * Reads a [FeatureRow] from the current position of a [ResultSet].
     *
     * @param rs The result set positioned at a valid row.
     * @return The feature row data.
     */
    fun readRow(rs: ResultSet): FeatureRow = FeatureRow(
        uid = rs.getString("uid"),
        enabled = rs.getLong("enabled"),
        groupName = rs.getString("group_name"),
        description = rs.getString("description"),
        permissions = rs.getString("permissions"),
        flippingStrategy = rs.getString("flipping_strategy"),
        customProperties = rs.getString("custom_properties"),
        version = rs.getLong("version"),
    )

    /**
     * Converts a [FeatureRow] to a domain [Feature].
     *
     * @param row The database row to convert.
     * @return The domain [Feature] object.
     */
    fun toDomain(row: FeatureRow): Feature = Feature(
        uid = row.uid,
        isEnabled = row.enabled != 0L,
        group = row.groupName,
        description = row.description,
        permissions = decodePermissions(row.permissions),
        flippingStrategy = row.flippingStrategy?.let { decodeStrategy(it) },
        customProperties = decodeProperties(row.customProperties),
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
    fun encodePermissions(permissions: Set<String>): String =
        json.encodeToString(permissionsSerializer, permissions)

    /**
     * Encodes a [FlippingStrategy] to JSON string with polymorphic type information.
     *
     * @param strategy The strategy to encode, or null.
     * @return JSON string with type discriminator, or null if strategy is null.
     */
    fun encodeStrategy(strategy: FlippingStrategy?): String? =
        strategy?.let { json.encodeToString(PolymorphicSerializer(FlippingStrategy::class), it) }

    /**
     * Encodes a [Feature]'s custom properties map to JSON string.
     *
     * @param properties The map of properties to encode.
     * @return JSON object string representation.
     */
    fun encodeProperties(properties: Map<String, Property<*>>): String =
        json.encodeToString(propertiesSerializer, properties)

    private fun decodePermissions(jsonString: String): Set<String> =
        json.decodeFromString(permissionsSerializer, jsonString)

    private fun decodeStrategy(jsonString: String): FlippingStrategy =
        json.decodeFromString(PolymorphicSerializer(FlippingStrategy::class), jsonString)

    private fun decodeProperties(jsonString: String): Map<String, Property<*>> =
        json.decodeFromString(propertiesSerializer, jsonString)
}
