package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.BASIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.DEFAULT_VALUES_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.POLYMORPHIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.allTestProperties
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonFF4kConfigurationParserTest {

    private val parser = JsonFF4kConfigurationParser()

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
        // Given
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

        // When
        val jsonString = parser.export(originalConfig)
        val reimportedConfig = jsonParser.decodeFromString<FF4kConfiguration>(jsonString)

        // Then
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

    private suspend fun <T> withTempFile(
        content: String,
        block: suspend (String) -> T,
    ): T {
        val tempFile = createTempFilePath()
        return try {
            writeFileContent(tempFile, content)
            block(tempFile)
        } finally {
            runCatching { deleteTempFile(tempFile) }
        }
    }

    @Test
    fun `parseConfigurationFile loads configuration from file path`() = runTest {
        // Given
        val jsonContent = BASIC_CONFIG_JSON

        withTempFile(jsonContent) { filePath ->
            // When
            val config = parser.parseConfigurationFile(filePath)

            // Then
            assertTrue(config.settings.autoCreate)
            assertEquals(1, config.features.size)
            assertEquals("dark-mode", config.features.values.first().uid)
            assertTrue(config.features.values.first().isEnabled)
            assertEquals(1, config.properties.size)
            assertEquals(PropertyInt("max-retries", 3), config.properties["max-retries"])
        }
    }

    @Test
    fun `parseConfigurationFile with all property types`() = runTest {
        // Given
        val jsonContent = POLYMORPHIC_CONFIG_JSON

        withTempFile(jsonContent) { filePath ->
            // When
            val config = parser.parseConfigurationFile(filePath)

            // Then
            assertEquals(allTestProperties.size, config.properties.size)
            config.properties.forEach { (key, value) ->
                assertEquals(allTestProperties[key], value, "Property '$key' mismatch")
            }
        }
    }

    @Test
    fun `parseConfigurationFile throws exception for non-existent file`() = runTest {
        // Given
        val nonExistentPath = "/this/path/does/not/exist/ff4k_config.json"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            parser.parseConfigurationFile(nonExistentPath)
        }
        assertContains(exception.message.orEmpty(), nonExistentPath)
    }

    @Test
    fun `parseConfigurationFile throws exception for malformed JSON`() = runTest {
        // Given
        val malformedJson = "{ this is not valid json }"

        withTempFile(malformedJson) { filePath ->
            // When & Then
            assertFailsWith<Exception> {
                parser.parseConfigurationFile(filePath)
            }
        }
    }

    @Test
    fun `parseConfigurationFile throws exception for invalid configuration structure`() = runTest {
        // Given
        // language=json
        val invalidConfig = """
            {
              "settings": { "autoCreate": "not-a-boolean" }
            }
        """.trimIndent()

        withTempFile(invalidConfig) { filePath ->
            // When & Then
            assertFailsWith<Exception> {
                parser.parseConfigurationFile(filePath)
            }
        }
    }

    @Test
    fun `parseConfigurationFile handles empty configuration`() = runTest {
        // Given
        val emptyConfig = DEFAULT_VALUES_CONFIG_JSON

        withTempFile(emptyConfig) { filePath ->
            // When
            val config = parser.parseConfigurationFile(filePath)

            // Then
            assertEquals(FF4kSettings(), config.settings)
            assertTrue(config.features.isEmpty())
            assertTrue(config.properties.isEmpty())
        }
    }

    companion object {
        private val jsonParser = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = ff4kSerializersModule
        }
    }
}
