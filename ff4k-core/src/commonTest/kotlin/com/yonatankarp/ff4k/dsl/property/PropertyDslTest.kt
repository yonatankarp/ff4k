package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Tests for top-level property() and properties() DSL functions.
 */
internal class PropertyDslTest :
    FunSpec({

        test("property creates single property with value") {
            // When
            val result = property(PROPERTY_MAX_RETRIES) {
                value = VALUE_MAX_RETRIES
            }

            // Then
            result.name shouldBe PROPERTY_MAX_RETRIES
            result.value shouldBe VALUE_MAX_RETRIES
        }

        test("property creates property with all options") {
            // When
            val result = property(PROPERTY_LOG_LEVEL) {
                value = VALUE_LOG_LEVEL
                description = DESCRIPTION_LOG_LEVEL
                readOnly = true
                fixedValues {
                    +LOG_LEVEL_DEBUG
                    +LOG_LEVEL_INFO
                    +LOG_LEVEL_WARN
                    +LOG_LEVEL_ERROR
                }
            }

            // Then
            result.name shouldBe PROPERTY_LOG_LEVEL
            result.value shouldBe VALUE_LOG_LEVEL
            result.description shouldBe DESCRIPTION_LOG_LEVEL
            result.readOnly.shouldBeTrue()
            result.fixedValues shouldBe LOG_LEVELS
        }

        test("property throws when value not set") {
            // When / Then
            shouldThrow<IllegalStateException> {
                property<String>(PROPERTY_MAX_RETRIES) { }
            }
        }

        test("properties creates empty list when no properties defined") {
            // When
            val result = properties { }

            // Then
            result.shouldBeEmpty()
        }

        test("properties creates single property using DSL block") {
            // When
            val result = properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
            }

            // Then
            result.size shouldBe 1
            result[0].name shouldBe PROPERTY_MAX_RETRIES
            result[0].value shouldBe VALUE_MAX_RETRIES
        }

        test("properties creates multiple properties using DSL blocks") {
            // When
            val result = properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                    description = DESCRIPTION_MAX_RETRIES
                }
                property(PROPERTY_TIMEOUT_MS) {
                    value = VALUE_TIMEOUT_MS
                }
                property(PROPERTY_API_URL) {
                    value = VALUE_API_URL
                    readOnly = true
                }
            }

            // Then
            result.size shouldBe 3

            result[0].name shouldBe PROPERTY_MAX_RETRIES
            result[0].value shouldBe VALUE_MAX_RETRIES
            result[0].description shouldBe DESCRIPTION_MAX_RETRIES

            result[1].name shouldBe PROPERTY_TIMEOUT_MS
            result[1].value shouldBe VALUE_TIMEOUT_MS

            result[2].name shouldBe PROPERTY_API_URL
            result[2].value shouldBe VALUE_API_URL
            result[2].readOnly.shouldBeTrue()
        }

        test("properties accepts pre-built properties") {
            // Given
            val preBuiltProperty = PropertyString(PROPERTY_ENV, VALUE_ENV)

            // When
            val result = properties {
                property(preBuiltProperty)
            }

            // Then
            result.size shouldBe 1
            result[0] shouldBe preBuiltProperty
        }

        test("properties combines pre-built and DSL-defined properties") {
            // Given
            val preBuiltProperty = PropertyInt(PROPERTY_PORT, VALUE_PORT)

            // When
            val result = properties {
                property(preBuiltProperty)
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
            }

            // Then
            result.size shouldBe 2
            result[0].name shouldBe PROPERTY_PORT
            result[1].name shouldBe PROPERTY_MAX_RETRIES
        }

        test("properties preserves insertion order") {
            // When
            val result = properties {
                property(PROPERTY_THIRD) { value = VALUE_THIRD }
                property(PROPERTY_FIRST) { value = VALUE_FIRST }
                property(PROPERTY_SECOND) { value = VALUE_SECOND }
            }

            // Then
            result[0].name shouldBe PROPERTY_THIRD
            result[1].name shouldBe PROPERTY_FIRST
            result[2].name shouldBe PROPERTY_SECOND
        }
    }) {
    private companion object {
        private const val PROPERTY_MAX_RETRIES = "max-retries"
        private const val PROPERTY_TIMEOUT_MS = "timeout-ms"
        private const val PROPERTY_API_URL = "api.base.url"
        private const val PROPERTY_LOG_LEVEL = "log-level"
        private const val PROPERTY_ENV = "environment"
        private const val PROPERTY_PORT = "server.port"
        private const val PROPERTY_FIRST = "first"
        private const val PROPERTY_SECOND = "second"
        private const val PROPERTY_THIRD = "third"

        private const val VALUE_MAX_RETRIES = 3
        private const val VALUE_TIMEOUT_MS = 5000L
        private const val VALUE_API_URL = "https://api.example.com"
        private const val VALUE_ENV = "production"
        private const val VALUE_PORT = 8080
        private const val VALUE_FIRST = "first-value"
        private const val VALUE_SECOND = "second-value"
        private const val VALUE_THIRD = "third-value"

        private const val DESCRIPTION_MAX_RETRIES = "Maximum retry attempts"
        private const val DESCRIPTION_LOG_LEVEL = "Application log level"

        private const val LOG_LEVEL_DEBUG = "DEBUG"
        private const val LOG_LEVEL_INFO = "INFO"
        private const val LOG_LEVEL_WARN = "WARN"
        private const val LOG_LEVEL_ERROR = "ERROR"
        private const val VALUE_LOG_LEVEL = LOG_LEVEL_INFO
        private val LOG_LEVELS = setOf(LOG_LEVEL_DEBUG, LOG_LEVEL_INFO, LOG_LEVEL_WARN, LOG_LEVEL_ERROR)
    }
}
