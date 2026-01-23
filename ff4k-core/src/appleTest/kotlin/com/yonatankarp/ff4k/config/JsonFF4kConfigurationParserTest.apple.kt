package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.BASIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.DEFAULT_VALUES_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.POLYMORPHIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.allTestProperties
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Apple/iOS-specific tests for [JsonFF4kConfigurationParser] that require loading resources.
 * These tests write JSON content to temp files since native platforms load resources from
 * the filesystem rather than the classpath.
 */
class JsonFF4kConfigurationParserTestApple {

    private val parser = JsonFF4kConfigurationParser()

    private suspend fun <T> withTempResource(
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
    fun `parse JSON file content`() = runTest {
        withTempResource(BASIC_CONFIG_JSON) { tempFile ->
            // When
            val config = parser.parseConfigurationResource(tempFile)

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
    }

    @Test
    fun `parse JSON file content with polymorphic properties`() = runTest {
        withTempResource(POLYMORPHIC_CONFIG_JSON) { tempFile ->
            // When
            val config = parser.parseConfigurationResource(tempFile)

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
    }

    @Test
    fun `parse JSON with no settings`() = runTest {
        withTempResource(DEFAULT_VALUES_CONFIG_JSON) { tempFile ->
            // When
            val config = parser.parseConfigurationResource(tempFile)

            // Then
            assertNotNull(config.settings)
            assertFalse(config.settings.autoCreate)
            assertNotNull(config.features)
            assertNotNull(config.properties)
        }
    }
}
