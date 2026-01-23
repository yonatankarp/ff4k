package com.yonatankarp.ff4k.config

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.property.PropertyBigDecimal
import com.yonatankarp.ff4k.property.PropertyBigInteger
import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyByte
import com.yonatankarp.ff4k.property.PropertyDouble
import com.yonatankarp.ff4k.property.PropertyFloat
import com.yonatankarp.ff4k.property.PropertyInstant
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyLocalDate
import com.yonatankarp.ff4k.property.PropertyLocalDateTime
import com.yonatankarp.ff4k.property.PropertyLogLevel
import com.yonatankarp.ff4k.property.PropertyLong
import com.yonatankarp.ff4k.property.PropertyShort
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JsonFF4kConfigurationParserTest {

    private val parser = JsonFF4kConfigurationParser()

    @Test
    fun `parse JSON file content`() = runTest {
        // Given
        val content = "ff4k_configuration.json"

        // When
        val config = parser.parseConfigurationResource(content)

        // Then
        assertTrue(config.settings.autoCreate)
        assertEquals(1, config.features.size)
        assertEquals(
            Feature(
                uid = "dark-mode",
                isEnabled = true,
                description = "Whether or not the user is in dark-mode",
            ),
            config.features.values.first(),
        )
        assertEquals(1, config.properties.size)
        assertEquals(
            PropertyInt(name = "max-retries", value = 3),
            config.properties.values.first(),
        )
    }

    @Test
    fun `parse JSON file content with polymorphic properties`() = runTest {
        // Given
        val content = "ff4k_configuration_polymorphic.json"

        // When
        val config = parser.parseConfigurationResource(content)

        // Then
        assertEquals(
            allTestProperties.size,
            config.properties.size,
            "Expected ${allTestProperties.size} properties but found ${config.properties.size}",
        )
        config.properties.forEach { (key, value) ->
            assertEquals(
                allTestProperties[key],
                value,
                "Property '$key' is not equal to '$value'",
            )
        }
    }

    @Test
    fun `parse JSON with no settings`() = runTest {
        // Given
        val content = "ff4k_configuration_with_default_values.json"

        // When
        val config = parser.parseConfigurationResource(content)

        // Then
        assertNotNull(config.settings)
        assertFalse(config.settings.autoCreate)
        assertNotNull(config.features)
        assertNotNull(config.properties)
    }

    @Test
    fun `export configuration to JSON`() = runTest {
        //  Given
        val config = FF4kConfiguration(
            settings = FF4kSettings(autoCreate = false),
            features = mapOf(
                "dark-mode" to Feature(
                    uid = "dark-mode",
                    isEnabled = true,
                    description = "Whether or not the user is in dark-mode",
                ),
            ),
            properties = mapOf(
                "retryLimit" to PropertyInt(
                    "retryLimit",
                    5,
                    description = "Maximum retry limit",
                ),
            ),
        )

        // When
        val jsonString = parser.export(config)

        // Then
        assertTrue("dark-mode" in jsonString)
        assertTrue("retryLimit" in jsonString)
        assertTrue("desc" in jsonString)
        assertTrue("Maximum retry limit" in jsonString)
    }

    @Test
    fun `round-trip export and import preserves configuration`() = runTest {
        // Given - a configuration with various features and all property types
        val originalConfig = FF4kConfiguration(
            settings = FF4kSettings(autoCreate = true),
            features = mapOf(
                "feature-enabled" to Feature(
                    uid = "feature-enabled",
                    isEnabled = true,
                    description = "An enabled feature",
                ),
                "feature-disabled" to Feature(
                    uid = "feature-disabled",
                    isEnabled = false,
                    description = "A disabled feature",
                ),
                "feature-no-description" to Feature(
                    uid = "feature-no-description",
                    isEnabled = true,
                ),
            ),
            properties = allTestProperties,
        )

        // When - export to JSON and parse back
        val jsonString = parser.export(originalConfig)
        val reimportedConfig = jsonParser.decodeFromString<FF4kConfiguration>(jsonString)

        // Then - the reimported configuration matches the original
        assertEquals(originalConfig.settings, reimportedConfig.settings)
        assertEquals(originalConfig.features.size, reimportedConfig.features.size)
        originalConfig.features.forEach { (key, feature) ->
            assertEquals(feature, reimportedConfig.features[key], "Feature '$key' mismatch")
        }
        assertEquals(originalConfig.properties.size, reimportedConfig.properties.size)
        originalConfig.properties.forEach { (key, property) ->
            assertEquals(property, reimportedConfig.properties[key], "Property '$key' mismatch")
        }
    }

    companion object {
        private val jsonParser = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = ff4kSerializersModule
        }

        private val allTestProperties: Map<String, Property<*>> = mapOf(
            "retryLimit" to PropertyInt(
                "retryLimit",
                5,
                description = "Maximum number of retries",
            ),
            "welcomeMessage" to PropertyString(
                "welcomeMessage",
                "Hello, user!",
                description = "Greeting message for users",
            ),
            "piValue" to PropertyDouble(
                "piValue",
                3.14159,
                description = "Pi constant value",
            ),
            "sessionTimeout" to PropertyFloat(
                "sessionTimeout",
                30.0f,
                description = "Session timeout in minutes",
            ),
            "maxUsers" to PropertyLong(
                "maxUsers",
                1_000L,
                description = "Maximum concurrent users allowed",
            ),
            "isBetaEnabled" to PropertyBoolean(
                "isBetaEnabled",
                true,
                description = "Flag to enable beta features",
            ),
            "defaultPriority" to PropertyShort(
                "defaultPriority",
                1.toShort(),
                description = "Default task priority",
            ),
            "defaultByteValue" to PropertyByte(
                "defaultByteValue",
                31.toByte(),
                description = "Default byte configuration",
            ),
            "bigOrderNumber" to PropertyBigInteger(
                "bigOrderNumber",
                BigInteger.parseString("1234567890123456789"),
                description = "Large order identifier",
            ),
            "taxRate" to PropertyBigDecimal(
                "taxRate",
                BigDecimal.parseString("1.9E-1"),
                description = "Tax rate as BigDecimal",
            ),
            "launchDate" to PropertyLocalDate(
                "launchDate",
                LocalDate.parse("2026-01-23"),
                description = "Official launch date",
            ),
            "lastLogin" to PropertyLocalDateTime(
                "lastLogin",
                LocalDateTime.parse("2026-01-23T21:00"),
                description = "User last login timestamp",
            ),
            "eventTimestamp" to PropertyInstant(
                "eventTimestamp",
                Instant.parse("2026-01-23T21:00:00Z"),
                description = "Event occurrence timestamp",
            ),
            "logLevel" to PropertyLogLevel(
                "logLevel",
                PropertyLogLevel.LogLevel.INFO,
                description = "Logging level for the system",
            ),
        )
    }
}
