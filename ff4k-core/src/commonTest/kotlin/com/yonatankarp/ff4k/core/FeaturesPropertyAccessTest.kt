package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

internal class FeaturesPropertyAccessTest :
    FunSpec({

        test("getPropertyOrThrow should return property when it exists") {
            // Given
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to property),
            )

            // When
            val retrieved = feature.getPropertyOrThrow<Int>(PROPERTY_NAME)

            // Then
            retrieved shouldBe property
            retrieved.value shouldBe PROPERTY_VALUE
        }

        test("getPropertyOrThrow should throw PropertyNotFoundException when property does not exist") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When / Then
            shouldThrow<PropertyNotFoundException> {
                feature.getPropertyOrThrow<Int>("nonexistent")
            }
        }

        test("getPropertyValueOrDefault should return property value when it exists") {
            // Given
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to property),
            )

            // When
            val value = feature.getPropertyValueOrDefault(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

            // Then
            value shouldBe PROPERTY_VALUE
        }

        test("getPropertyValueOrDefault should return default value when property does not exist") {
            // Given
            val feature = Feature(uid = FEATURE_UID)
            // When
            val value = feature.getPropertyValueOrDefault(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

            // Then
            value shouldBe DEFAULT_FALLBACK_VALUE
        }

        test("getPropertyValueOrDefault should return default value when property is null") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val value = feature.getPropertyValueOrDefault(NONEXISTENT_PROPERTY, DEFAULT_FALLBACK_VALUE)

            // Then
            value shouldBe DEFAULT_FALLBACK_VALUE
        }

        test("hasPropertyWithValue should return true when property exists with matching value") {
            // Given
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to property),
            )

            // When
            val result = feature.hasPropertyWithValue(PROPERTY_NAME, PROPERTY_VALUE)

            // Then
            result.shouldBeTrue()
        }

        test("hasPropertyWithValue should return false when property exists with different value") {
            // Given
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to property),
            )

            // When
            val result = feature.hasPropertyWithValue(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

            // Then
            result.shouldBeFalse()
        }

        test("hasPropertyWithValue should return false when property does not exist") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val result = feature.hasPropertyWithValue(PROPERTY_NAME, PROPERTY_VALUE)

            // Then
            result.shouldBeFalse()
        }

        test("getPropertiesOfType should return all properties of specified type") {
            // Given
            val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val cacheProp = PropertyInt(name = CACHE_PROPERTY, value = CACHE_SIZE)
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(
                    TIMEOUT_PROPERTY to timeoutProp,
                    CACHE_PROPERTY to cacheProp,
                    REGION_PROPERTY to regionProp,
                ),
            )

            // When
            val intProps = feature.getPropertiesOfType<Int>()

            // Then
            intProps.size shouldBe 2
            intProps[TIMEOUT_PROPERTY] shouldBe timeoutProp
            intProps[CACHE_PROPERTY] shouldBe cacheProp
        }

        test("getPropertiesOfType should return empty map when no properties of type exist") {
            // Given
            val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(REGION_PROPERTY to regionProp),
            )

            // When
            val intProps = feature.getPropertiesOfType<Int>()

            // Then
            intProps.shouldBeEmpty()
        }

        test("getPropertiesOfType should return empty map when no properties exist") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val intProps = feature.getPropertiesOfType<Int>()

            // Then
            intProps.shouldBeEmpty()
        }
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val PROPERTY_NAME = "maxRetries"
        private const val PROPERTY_VALUE = 3
        private const val TIMEOUT_PROPERTY = "timeout"
        private const val CACHE_PROPERTY = "cache"
        private const val REGION_PROPERTY = "region"
        private const val NONEXISTENT_PROPERTY = "nonexistent"
        private const val TIMEOUT_VALUE = 5000
        private const val CACHE_SIZE = 100
        private const val DEFAULT_FALLBACK_VALUE = 999
        private const val TEST_STRING_VALUE = "us-east-1"
    }
}
