package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

internal class FeaturesStateTest :
    FunSpec({

        test("isDisabled should return true when feature is disabled") {
            // Given
            val feature = Feature(uid = FEATURE_UID, isEnabled = false)

            // When
            val result = feature.isDisabled

            // Then
            result.shouldBeTrue()
        }

        test("isDisabled should return false when feature is enabled") {
            // Given
            val feature = Feature(uid = FEATURE_UID, isEnabled = true)

            // When
            val result = feature.isDisabled

            // Then
            result.shouldBeFalse()
        }

        test("propertyNames should return empty set when no properties") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val names = feature.propertyNames

            // Then
            names.shouldBeEmpty()
        }

        test("propertyNames should return all property names when properties exist") {
            // Given
            val property1 = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
            val property2 = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(
                    TIMEOUT_PROPERTY to property1,
                    REGION_PROPERTY to property2,
                ),
            )

            // When
            val names = feature.propertyNames

            // Then
            names.size shouldBe 2
            names shouldContain TIMEOUT_PROPERTY
            names shouldContain REGION_PROPERTY
        }

        test("hasPermissions should return false when no permissions") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val result = feature.hasPermissions

            // Then
            result.shouldBeFalse()
        }

        test("hasPermissions should return true when permissions exist") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ADMIN_USER_PERMISSIONS,
            )

            // When
            val result = feature.hasPermissions

            // Then
            result.shouldBeTrue()
        }

        test("hasFlippingStrategy should return false when no strategy") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val result = feature.hasFlippingStrategy

            // Then
            result.shouldBeFalse()
        }

        test("hasFlippingStrategy should return true when strategy exists") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                flippingStrategy = AlwaysTrueFlippingStrategy,
            )

            // When
            val result = feature.hasFlippingStrategy

            // Then
            result.shouldBeTrue()
        }
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val TIMEOUT_PROPERTY = "timeout"
        private const val REGION_PROPERTY = "region"
        private const val TIMEOUT_VALUE = 5000
        private const val TEST_STRING_VALUE = "us-east-1"
        private const val ADMIN_PERMISSION = "ADMIN"
        private const val USER_PERMISSION = "USER"
        private val ADMIN_USER_PERMISSIONS = setOf(ADMIN_PERMISSION, USER_PERMISSION)
    }
}
