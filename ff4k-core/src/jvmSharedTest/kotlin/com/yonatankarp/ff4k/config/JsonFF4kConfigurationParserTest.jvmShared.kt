package com.yonatankarp.ff4k.config

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
 * JVM-specific tests for [JsonFF4kConfigurationParser] that require loading resources
 * from the classpath. These tests are separated from commonTest because iOS simulator
 * tests cannot access bundled resources in the same way.
 */
class JsonFF4kConfigurationParserTestJvmShared {

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
}
