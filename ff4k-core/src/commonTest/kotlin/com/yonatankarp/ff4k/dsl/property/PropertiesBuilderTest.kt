package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Tests for PropertiesBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
internal class PropertiesBuilderTest :
    FunSpec({

        test("builds empty list when no properties added") {
            // Given
            val builder = PropertiesBuilder()

            // When
            val result = builder.build()

            // Then
            result.shouldBeEmpty()
        }

        test("adds pre-built property using property method") {
            // Given
            val builder = PropertiesBuilder()
            val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

            // When
            val result = builder.apply {
                property(property)
            }.build()

            // Then
            result.size shouldBe 1
            result[0] shouldBe property
        }

        test("adds multiple pre-built properties") {
            // Given
            val builder = PropertiesBuilder()
            val property1 = PropertyString(PROPERTY_API_URL, VALUE_API_URL)
            val property2 = PropertyInt(PROPERTY_MAX_RETRIES, VALUE_MAX_RETRIES)

            // When
            val result = builder.apply {
                property(property1)
                property(property2)
            }.build()

            // Then
            result.size shouldBe 2
            result[0] shouldBe property1
            result[1] shouldBe property2
        }

        test("creates property inline using DSL block") {
            // Given
            val builder = PropertiesBuilder()

            // When
            val result = builder.apply {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                    description = DESCRIPTION_MAX_RETRIES
                }
            }.build()

            // Then
            result.size shouldBe 1
            result[0].name shouldBe PROPERTY_MAX_RETRIES
            result[0].value shouldBe VALUE_MAX_RETRIES
            result[0].description shouldBe DESCRIPTION_MAX_RETRIES
        }

        test("creates multiple properties inline using DSL blocks") {
            // Given
            val builder = PropertiesBuilder()

            // When
            val result = builder.apply {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
                property(PROPERTY_TIMEOUT_MS) {
                    value = VALUE_TIMEOUT_MS
                }
                property(PROPERTY_API_URL) {
                    value = VALUE_API_URL
                }
            }.build()

            // Then
            result.size shouldBe 3
            result[0].name shouldBe PROPERTY_MAX_RETRIES
            result[1].name shouldBe PROPERTY_TIMEOUT_MS
            result[2].name shouldBe PROPERTY_API_URL
        }

        test("combines pre-built and DSL-defined properties") {
            // Given
            val builder = PropertiesBuilder()
            val preBuiltProperty = PropertyString(PROPERTY_ENV, VALUE_ENV)

            // When
            val result = builder.apply {
                property(preBuiltProperty)
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
            }.build()

            // Then
            result.size shouldBe 2
            result[0].name shouldBe PROPERTY_ENV
            result[1].name shouldBe PROPERTY_MAX_RETRIES
        }

        test("preserves insertion order") {
            // Given
            val builder = PropertiesBuilder()

            // When
            val result = builder.apply {
                property(PROPERTY_THIRD) { value = VALUE_THIRD }
                property(PROPERTY_FIRST) { value = VALUE_FIRST }
                property(PROPERTY_SECOND) { value = VALUE_SECOND }
            }.build()

            // Then
            result[0].name shouldBe PROPERTY_THIRD
            result[1].name shouldBe PROPERTY_FIRST
            result[2].name shouldBe PROPERTY_SECOND
        }

        test("allows duplicate properties") {
            // Given
            val builder = PropertiesBuilder()
            val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

            // When
            val result = builder.apply {
                property(property)
                property(property)
            }.build()

            // Then
            result.size shouldBe 2
        }

        test("creates property with all options") {
            // Given
            val builder = PropertiesBuilder()

            // When
            val result = builder.apply {
                property(PROPERTY_LOG_LEVEL) {
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
            }.build()

            // Then
            result.size shouldBe 1
            val prop = result[0]
            prop.name shouldBe PROPERTY_LOG_LEVEL
            prop.value shouldBe VALUE_LOG_LEVEL
            prop.description shouldBe DESCRIPTION_LOG_LEVEL
            prop.readOnly.shouldBeTrue()
            prop.fixedValues shouldBe LOG_LEVELS
        }
    }) {
    private companion object {
        private const val PROPERTY_API_URL = "api.base.url"
        private const val PROPERTY_MAX_RETRIES = "max-retries"
        private const val PROPERTY_TIMEOUT_MS = "timeout-ms"
        private const val PROPERTY_ENV = "environment"
        private const val PROPERTY_LOG_LEVEL = "log-level"
        private const val PROPERTY_FIRST = "first"
        private const val PROPERTY_SECOND = "second"
        private const val PROPERTY_THIRD = "third"

        private const val VALUE_API_URL = "https://api.example.com"
        private const val VALUE_MAX_RETRIES = 3
        private const val VALUE_TIMEOUT_MS = 5000L
        private const val VALUE_ENV = "production"
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
