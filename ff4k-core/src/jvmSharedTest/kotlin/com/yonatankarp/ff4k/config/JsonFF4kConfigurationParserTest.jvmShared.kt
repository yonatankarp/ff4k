package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.allTestProperties
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * JVM-specific tests for [JsonFF4kConfigurationParser] that require loading resources
 * from the classpath. These tests are separated from commonTest because iOS simulator
 * tests cannot access bundled resources in the same way.
 */
class JsonFF4kConfigurationParserTestJvmShared :
    FunSpec({

        val parser = JsonFF4kConfigurationParser()

        test("parse JSON file content") {
            // Given
            val content = "ff4k_configuration.json"

            // When
            val config = parser.parseConfigurationResource(content)

            // Then
            config.settings.autoCreate.shouldBeTrue()
            config.features.size shouldBe 1
            config.features.values.first() shouldBe Feature(
                uid = "dark-mode",
                isEnabled = true,
                description = "Whether or not the user is in dark-mode",
            )
            config.properties.size shouldBe 1
            config.properties.values.first() shouldBe PropertyInt(name = "max-retries", value = 3)
        }

        test("parse JSON file content with polymorphic properties") {
            // Given
            val content = "ff4k_configuration_polymorphic.json"

            // When
            val config = parser.parseConfigurationResource(content)

            // Then
            config.properties.size shouldBe allTestProperties.size
            config.properties.forEach { (key, value) ->
                value shouldBe allTestProperties[key]
            }
        }

        test("parse JSON with no settings") {
            // Given
            val content = "ff4k_configuration_with_default_values.json"

            // When
            val config = parser.parseConfigurationResource(content)

            // Then
            config.settings.shouldNotBeNull()
            config.settings.autoCreate.shouldBeFalse()
            config.features.shouldNotBeNull()
            config.properties.shouldNotBeNull()
        }
    })
