package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.BASIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.DEFAULT_VALUES_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.POLYMORPHIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.allTestProperties
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Apple/iOS-specific tests for [JsonFF4kConfigurationParser] that require loading resources.
 * These tests write JSON content to temp files since native platforms load resources from
 * the filesystem rather than the classpath.
 */
class JsonFF4kConfigurationParserTestApple :
    FunSpec({

        val parser = JsonFF4kConfigurationParser()

        suspend fun <T> withTempResource(
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

        test("parse JSON file content") {
            withTempResource(BASIC_CONFIG_JSON) { tempFile ->
                // When
                val config = parser.parseConfigurationResource(tempFile)

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
        }

        test("parse JSON file content with polymorphic properties") {
            withTempResource(POLYMORPHIC_CONFIG_JSON) { tempFile ->
                // When
                val config = parser.parseConfigurationResource(tempFile)

                // Then
                config.properties.size shouldBe allTestProperties.size
                config.properties.forEach { (key, value) ->
                    value shouldBe allTestProperties[key]
                }
            }
        }

        test("parse JSON with no settings") {
            withTempResource(DEFAULT_VALUES_CONFIG_JSON) { tempFile ->
                // When
                val config = parser.parseConfigurationResource(tempFile)

                // Then
                config.settings.shouldNotBeNull()
                config.settings.autoCreate.shouldBeFalse()
                config.features.shouldNotBeNull()
                config.properties.shouldNotBeNull()
            }
        }
    })
