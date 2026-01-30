package com.yonatankarp.ff4k.config

import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.BASIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.DEFAULT_VALUES_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.POLYMORPHIC_CONFIG_JSON
import com.yonatankarp.ff4k.config.ConfigurationTestFixtures.allTestProperties
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

internal class JsonFF4kConfigurationParserTest :
    FunSpec({

        val parser = JsonFF4kConfigurationParser()
        val jsonParser = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = ff4kSerializersModule
        }

        suspend fun <T> withTempFile(
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

        test("export configuration to JSON") {
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
            jsonString shouldContain "dark-mode"
            jsonString shouldContain "retryLimit"
            jsonString shouldContain "desc"
            jsonString shouldContain "Maximum retry limit"
        }

        test("round-trip export and import preserves configuration") {
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
            reimportedConfig.settings shouldBe originalConfig.settings
            reimportedConfig.features.size shouldBe originalConfig.features.size
            originalConfig.features.forEach { (key, feature) ->
                reimportedConfig.features[key] shouldBe feature
            }
            reimportedConfig.properties.size shouldBe originalConfig.properties.size
            originalConfig.properties.forEach { (key, property) ->
                reimportedConfig.properties[key] shouldBe property
            }
        }

        test("parseConfigurationFile loads configuration from file path") {
            // Given
            val jsonContent = BASIC_CONFIG_JSON

            withTempFile(jsonContent) { filePath ->
                // When
                val config = parser.parseConfigurationFile(filePath)

                // Then
                config.settings.autoCreate.shouldBeTrue()
                config.features.shouldHaveSize(1)
                config.features.values.first().apply {
                    uid shouldBe "dark-mode"
                    isEnabled.shouldBeTrue()
                }
                config.properties.shouldHaveSize(1)
                config.properties["max-retries"] shouldBe PropertyInt("max-retries", 3)
            }
        }

        test("parseConfigurationFile with all property types") {
            // Given
            val jsonContent = POLYMORPHIC_CONFIG_JSON

            withTempFile(jsonContent) { filePath ->
                // When
                val config = parser.parseConfigurationFile(filePath)

                // Then
                config.properties.size shouldBe allTestProperties.size
                config.properties.forEach { (key, value) ->
                    value shouldBe allTestProperties[key]
                }
            }
        }

        test("parseConfigurationFile throws exception for non-existent file") {
            // Given
            val nonExistentPath = "/this/path/does/not/exist/ff4k_config.json"

            // When & Then
            val exception = shouldThrow<IllegalArgumentException> {
                parser.parseConfigurationFile(nonExistentPath)
            }
            exception.message.orEmpty() shouldContain nonExistentPath
        }

        test("parseConfigurationFile throws exception for malformed JSON") {
            // Given
            val malformedJson = "{ this is not valid json }"

            withTempFile(malformedJson) { filePath ->
                // When & Then
                shouldThrow<Exception> {
                    parser.parseConfigurationFile(filePath)
                }
            }
        }

        test("parseConfigurationFile throws exception for invalid configuration structure") {
            // Given
            // language=json
            val invalidConfig = """
            {
              "settings": { "autoCreate": "not-a-boolean" }
            }
            """.trimIndent()

            withTempFile(invalidConfig) { filePath ->
                // When & Then
                shouldThrow<Exception> {
                    parser.parseConfigurationFile(filePath)
                }
            }
        }

        test("parseConfigurationFile handles empty configuration") {
            // Given
            val emptyConfig = DEFAULT_VALUES_CONFIG_JSON

            withTempFile(emptyConfig) { filePath ->
                // When
                val config = parser.parseConfigurationFile(filePath)

                // Then
                config.settings shouldBe FF4kSettings()
                config.features.shouldBeEmpty()
                config.properties.shouldBeEmpty()
            }
        }
    })
