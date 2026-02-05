package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe

internal class FeaturesPropertyManipulationTest :
    FunSpec({

        test("addProperties vararg should return new feature with properties added") {
            // Given
            val feature = Feature(uid = FEATURE_UID)
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)

            // When
            val updated = feature.addProperties(timeoutProp, regionProp)

            // Then
            updated.customProperties.size shouldBe 2
            updated.customProperties[TIMEOUT_PROPERTY] shouldBe timeoutProp
            updated.customProperties[REGION_PROPERTY] shouldBe regionProp
            feature.customProperties.shouldBeEmpty() // Original unchanged
        }

        test("addProperties vararg should replace existing properties with same name") {
            // Given
            val oldTimeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(TIMEOUT_PROPERTY to oldTimeoutProp),
            )
            val newTimeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = DEFAULT_FALLBACK_VALUE)

            // When
            val updated = feature.addProperties(newTimeoutProp)

            // Then
            updated.customProperties.size shouldBe 1
            updated.customProperties[TIMEOUT_PROPERTY] shouldBe newTimeoutProp
            (updated.customProperties[TIMEOUT_PROPERTY] as PropertyInt).value shouldBe DEFAULT_FALLBACK_VALUE
        }

        test("addProperties Collection should return new feature with properties added") {
            // Given
            val feature = Feature(uid = FEATURE_UID)
            val properties = listOf(
                PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE),
                PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE),
            )

            // When
            val updated = feature.addProperties(properties)

            // Then
            updated.customProperties.size shouldBe 2
            updated.customProperties[TIMEOUT_PROPERTY] shouldBe properties[0]
            updated.customProperties[REGION_PROPERTY] shouldBe properties[1]
            feature.customProperties.shouldBeEmpty() // Original unchanged
        }

        test("addProperties Collection should preserve existing properties") {
            // Given
            val existingProp = PropertyInt(name = EXISTING_PROPERTY, value = CACHE_SIZE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(EXISTING_PROPERTY to existingProp),
            )
            val newProperties = listOf(
                PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE),
                PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE),
            )

            // When
            val updated = feature.addProperties(newProperties)

            // Then
            updated.customProperties.size shouldBe 3
            updated.customProperties[EXISTING_PROPERTY] shouldBe existingProp
            updated.customProperties[TIMEOUT_PROPERTY] shouldBe newProperties[0]
            updated.customProperties[REGION_PROPERTY] shouldBe newProperties[1]
        }

        test("removeProperties vararg should return new feature with properties removed") {
            // Given
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val cacheProp = PropertyInt(name = CACHE_PROPERTY, value = CACHE_SIZE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(
                    TIMEOUT_PROPERTY to timeoutProp,
                    REGION_PROPERTY to regionProp,
                    CACHE_PROPERTY to cacheProp,
                ),
            )

            // When
            val updated = feature.removeProperties(TIMEOUT_PROPERTY, CACHE_PROPERTY)

            // Then
            updated.customProperties.size shouldBe 1
            updated.customProperties[REGION_PROPERTY] shouldBe regionProp
            updated.customProperties shouldNotContainKey TIMEOUT_PROPERTY
            updated.customProperties shouldNotContainKey CACHE_PROPERTY
            feature.customProperties.size shouldBe 3 // Original unchanged
        }

        test("removeProperties vararg should ignore non-existent properties") {
            // Given
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(TIMEOUT_PROPERTY to timeoutProp),
            )

            // When
            val updated = feature.removeProperties(TIMEOUT_PROPERTY, NONEXISTENT_PROPERTY, CACHE_PROPERTY)

            // Then
            updated.customProperties.shouldBeEmpty()
        }

        test("removeProperties Collection should return new feature with properties removed") {
            // Given
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(
                    TIMEOUT_PROPERTY to timeoutProp,
                    REGION_PROPERTY to regionProp,
                ),
            )

            // When
            val updated = feature.removeProperties(setOf(TIMEOUT_PROPERTY))

            // Then
            updated.customProperties.size shouldBe 1
            updated.customProperties[REGION_PROPERTY] shouldBe regionProp
            updated.customProperties shouldNotContainKey TIMEOUT_PROPERTY
            feature.customProperties.size shouldBe 2 // Original unchanged
        }

        test("clearProperties should return new feature with no custom properties") {
            // Given
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(
                    TIMEOUT_PROPERTY to timeoutProp,
                    REGION_PROPERTY to regionProp,
                ),
            )

            // When
            val updated = feature.clearProperties()

            // Then
            updated.customProperties.shouldBeEmpty()
            feature.customProperties.size shouldBe 2 // Original unchanged
        }

        test("clearProperties should work on feature with no properties") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.clearProperties()

            // Then
            updated.customProperties.shouldBeEmpty()
        }
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val TIMEOUT_PROPERTY = "timeout"
        private const val REGION_PROPERTY = "region"
        private const val CACHE_PROPERTY = "cache"
        private const val EXISTING_PROPERTY = "existing"
        private const val NONEXISTENT_PROPERTY = "nonexistent"
        private const val TIMEOUT_VALUE = 5000
        private const val CACHE_SIZE = 100
        private const val DEFAULT_FALLBACK_VALUE = 999
        private const val TEST_STRING_VALUE = "us-east-1"
    }
}
