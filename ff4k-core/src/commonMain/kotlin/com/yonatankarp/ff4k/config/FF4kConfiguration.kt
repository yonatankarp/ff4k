package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.Property
import kotlinx.serialization.Serializable

/**
 * Represents the complete configuration for an FF4k instance.
 *
 * This data class holds all the settings, feature flags, and properties that define
 * the behavior of FF4k. It can be serialized to and deserialized from JSON, enabling
 * configuration management through external files or resources.
 *
 * ## Usage
 *
 * Configuration can be loaded from JSON files using [JsonFF4kConfigurationParser]:
 * ```kotlin
 * val parser = JsonFF4kConfigurationParser()
 * val config = parser.parseConfigurationResource("ff4k_config.json")
 * ```
 *
 * Or created programmatically:
 * ```kotlin
 * val config = FF4kConfiguration(
 *     settings = FF4kSettings(autoCreate = true),
 *     features = mapOf("my-feature" to Feature(uid = "my-feature", isEnabled = true)),
 *     properties = mapOf("timeout" to intProperty("timeout") { value = 30 })
 * )
 * ```
 *
 * @property settings Global settings that control FF4k behavior.
 * @property features Map of feature flags keyed by their unique identifier.
 * @property properties Map of properties keyed by their name.
 *
 * @see FF4kSettings
 * @see JsonFF4kConfigurationParser
 */
@Serializable
data class FF4kConfiguration(
    val settings: FF4kSettings = FF4kSettings(),
    val features: Map<String, Feature> = emptyMap(),
    val properties: Map<String, Property<*>> = emptyMap(),
)

/**
 * Global settings that control FF4k behavior.
 *
 * These settings affect how FF4k handles feature flags and properties at runtime.
 *
 * @property autoCreate When `true`, FF4k will automatically create feature flags
 *   or properties when they are accessed but don't exist in the store. When `false`
 *   (default), accessing a non-existent feature or property will throw an exception.
 *
 * @see FF4kConfiguration
 */
@Serializable
data class FF4kSettings(
    val autoCreate: Boolean = false,
)
