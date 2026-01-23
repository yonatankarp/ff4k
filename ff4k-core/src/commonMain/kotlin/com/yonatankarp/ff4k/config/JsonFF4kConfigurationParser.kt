package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JSON-based implementation of [FF4kConfigurationParser] for [FF4kConfiguration].
 *
 * This parser uses kotlinx.serialization to handle JSON serialization and deserialization
 * of FF4k configurations, with full support for polymorphic property types.
 *
 * ## Features
 *
 * - **Pretty-printed output**: Exported JSON is formatted for readability.
 * - **Lenient parsing**: Unknown keys in JSON are ignored, enabling forward compatibility.
 * - **Polymorphic properties**: All property types (int, string, boolean, etc.) are correctly
 *   serialized and deserialized using type discriminators.
 *
 * ## Usage
 *
 * ```kotlin
 * val parser = JsonFF4kConfigurationParser()
 *
 * // Load from resources (e.g., bundled config)
 * val config = parser.parseConfigurationResource("ff4k_config.json")
 *
 * // Load from filesystem
 * val fileConfig = parser.parseConfigurationFile("/path/to/config.json")
 *
 * // Export configuration to JSON string
 * val jsonString = parser.export(config)
 * ```
 *
 * @param json Custom [Json] instance for serialization. Defaults to a pre-configured
 *   instance with pretty printing, lenient parsing, and FF4k serializers module.
 *
 * @see FF4kConfiguration
 * @see FF4kConfigurationParser
 */
class JsonFF4kConfigurationParser(
    private val json: Json = DEFAULT_PARSER,
) : FF4kConfigurationParser<FF4kConfiguration> {

    override suspend fun parseConfigurationResource(file: String): FF4kConfiguration {
        val content = loadResourceContent(file)
        return json.decodeFromString(content)
    }

    override suspend fun parseConfigurationFile(file: String): FF4kConfiguration {
        val content = readFileContent(file)
        return json.decodeFromString(content)
    }

    override suspend fun export(configuration: FF4kConfiguration): String = json.encodeToString(configuration)

    companion object {
        private val DEFAULT_PARSER = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = ff4kSerializersModule
        }
    }
}
