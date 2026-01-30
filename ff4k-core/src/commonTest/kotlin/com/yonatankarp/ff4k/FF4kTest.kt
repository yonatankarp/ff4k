package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests for the FF4k main class.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FF4kTest :
    FunSpec({

        test("check should return true for enabled feature") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true))

            // When
            val result = ff4k.check(FEATURE_DARK_MODE)

            // Then
            result.shouldBeTrue()
        }

        test("check should return false for disabled feature") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = false))

            // When
            val result = ff4k.check(FEATURE_DARK_MODE)

            // Then
            result.shouldBeFalse()
        }

        test("check should throw when feature does not exist and autoCreate is false") {
            // Given
            val ff4k = FF4k(autoCreate = false)

            // When/Then
            shouldThrow<FeatureNotFoundException> {
                ff4k.check(FEATURE_NON_EXISTENT)
            }
        }

        test("check should return false when feature does not exist and autoCreate is true") {
            // Given
            val ff4k = FF4k(autoCreate = true)

            // When
            val result = ff4k.check(FEATURE_NEW_CHECKOUT)

            // Then
            result.shouldBeFalse()
            ff4k.hasFeature(FEATURE_NEW_CHECKOUT).shouldBeTrue()
        }

        test("features should return all features") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = true)
            ff4k.addFeature(FEATURE_BETA, isEnabled = false)

            // When
            val features = ff4k.features()

            // Then
            features.size shouldBe 2
            features shouldContainKey FEATURE_DARK_MODE
            features shouldContainKey FEATURE_BETA
        }

        test("feature should return feature by id") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, description = DESCRIPTION_DARK_MODE))

            // When
            val feature = ff4k.feature(FEATURE_DARK_MODE)

            // Then
            feature.uid shouldBe FEATURE_DARK_MODE
            feature.description shouldBe DESCRIPTION_DARK_MODE
            feature.isEnabled.shouldBeTrue()
        }

        test("hasFeature should return true for existing feature") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(FEATURE_DARK_MODE)

            // When/Then
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeTrue()
        }

        test("hasFeature should return false for non-existing feature") {
            // Given
            val ff4k = FF4k()

            // When/Then
            ff4k.hasFeature(FEATURE_NON_EXISTENT).shouldBeFalse()
        }

        test("deleteFeature should remove feature") {
            // Given
            val ff4k = FF4k()
            val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)
            ff4k.addFeature(feature)

            // When
            ff4k.deleteFeature(feature)

            // Then
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeFalse()
        }

        test("enable should enable a disabled feature") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = false)

            // When
            ff4k.enable(FEATURE_DARK_MODE)

            // Then
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
        }

        test("disable should disable an enabled feature") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = true)

            // When
            ff4k.disable(FEATURE_DARK_MODE)

            // Then
            ff4k.check(FEATURE_DARK_MODE).shouldBeFalse()
        }

        test("enable should auto-create feature when autoCreate is true") {
            // Given
            val ff4k = FF4k(autoCreate = true)

            // When
            ff4k.enable(FEATURE_NEW_CHECKOUT)

            // Then
            ff4k.hasFeature(FEATURE_NEW_CHECKOUT).shouldBeTrue()
            ff4k.check(FEATURE_NEW_CHECKOUT).shouldBeTrue()
        }

        test("disable should auto-create feature when autoCreate is true") {
            // Given
            val ff4k = FF4k(autoCreate = true)

            // When
            ff4k.disable(FEATURE_NEW_CHECKOUT)

            // Then
            ff4k.hasFeature(FEATURE_NEW_CHECKOUT).shouldBeTrue()
            ff4k.check(FEATURE_NEW_CHECKOUT).shouldBeFalse()
        }

        test("properties should return all properties") {
            // Given
            val ff4k = FF4k()
            ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))
            ff4k.addProperty(PropertyString(PROPERTY_ENV, VALUE_ENV_PRODUCTION))

            // When
            val properties = ff4k.properties()

            // Then
            properties.size shouldBe 2
            properties shouldContainKey PROPERTY_API_URL
            properties shouldContainKey PROPERTY_ENV
        }

        test("property should return property by name") {
            // Given
            val ff4k = FF4k()
            ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))

            // When
            val property = ff4k.property<String>(PROPERTY_API_URL)

            // Then
            property.shouldNotBeNull()
            property.value shouldBe VALUE_API_URL
        }

        test("property should return null for non-existing property") {
            // Given
            val ff4k = FF4k()

            // When
            val property = ff4k.property<String>(PROPERTY_NON_EXISTENT)

            // Then
            property.shouldBeNull()
        }

        test("hasProperty should return true for existing property") {
            // Given
            val ff4k = FF4k()
            ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))

            // When/Then
            ff4k.hasProperty(PROPERTY_API_URL).shouldBeTrue()
        }

        test("hasProperty should return false for non-existing property") {
            // Given
            val ff4k = FF4k()

            // When/Then
            ff4k.hasProperty(PROPERTY_NON_EXISTENT).shouldBeFalse()
        }

        test("deleteProperty should remove property") {
            // Given
            val ff4k = FF4k()
            val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)
            ff4k.addProperty(property)

            // When
            ff4k.deleteProperty(property)

            // Then
            ff4k.hasProperty(PROPERTY_API_URL).shouldBeFalse()
        }

        context("propertyAsString should return property value as string") {
            withData(
                nameFn = { "for ${it.property::class.simpleName} with value ${it.expectedValue}" },
                propertyAsStringData,
            ) { (property, expectedValue) ->
                // Given
                val ff4k = FF4k()
                ff4k.addProperty(property)

                // When
                val value = ff4k.propertyAsString<Any>(property.name)

                // Then
                value shouldBe expectedValue
            }
        }

        test("featuresByGroup should return features in group") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = false, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = true, group = GROUP_BILLING))

            // When
            val uiFeatures = ff4k.featuresByGroup(GROUP_UI)

            // Then
            uiFeatures.size shouldBe 2
            uiFeatures shouldContainKey FEATURE_DARK_MODE
            uiFeatures shouldContainKey FEATURE_BETA
        }

        test("containGroup should return true for existing group") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))

            // When/Then
            ff4k.containGroup(GROUP_UI).shouldBeTrue()
        }

        test("containGroup should return false for non-existing group") {
            // Given
            val ff4k = FF4k()

            // When/Then
            ff4k.containGroup(GROUP_NON_EXISTENT).shouldBeFalse()
        }

        test("enableGroup should enable all features in group") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = false, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = false, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = false, group = GROUP_BILLING))

            // When
            ff4k.enableGroup(GROUP_UI)

            // Then
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.check(FEATURE_BETA).shouldBeTrue()
            ff4k.check(FEATURE_PREMIUM).shouldBeFalse() // Different group, unchanged
        }

        test("disableGroup should disable all features in group") {
            // Given
            val ff4k = FF4k()
            ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = true, group = GROUP_UI))
            ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = true, group = GROUP_BILLING))

            // When
            ff4k.disableGroup(GROUP_UI)

            // Then
            ff4k.check(FEATURE_DARK_MODE).shouldBeFalse()
            ff4k.check(FEATURE_BETA).shouldBeFalse()
            ff4k.check(FEATURE_PREMIUM).shouldBeTrue() // Different group, unchanged
        }

        test("fluent API should allow method chaining") {
            // Given/When
            val ff4k = FF4k()
                .addFeature(FEATURE_DARK_MODE, isEnabled = true)
                .addFeature(FEATURE_BETA, isEnabled = false)
                .addProperty(PropertyString(PROPERTY_ENV, VALUE_ENV_PRODUCTION))
                .enable(FEATURE_BETA)

            // Then
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.check(FEATURE_BETA).shouldBeTrue()
            ff4k.hasProperty(PROPERTY_ENV).shouldBeTrue()
        }

        test("fluent API methods should return same instance") {
            // Given
            val ff4k = FF4k()

            // When
            val result = ff4k.addFeature(FEATURE_DARK_MODE)

            // Then
            result shouldBeSameInstanceAs ff4k
        }
    }) {
    companion object {
        // Feature IDs
        private const val FEATURE_DARK_MODE = "feature-dark-mode"
        private const val FEATURE_BETA = "feature-beta-program"
        private const val FEATURE_PREMIUM = "feature-premium-tier"
        private const val FEATURE_NEW_CHECKOUT = "feature-new-checkout"
        private const val FEATURE_NON_EXISTENT = "feature-does-not-exist"

        // Feature descriptions
        private const val DESCRIPTION_DARK_MODE = "Enable dark mode theme"

        // Property names
        private const val PROPERTY_API_URL = "api.base.url"
        private const val PROPERTY_ENV = "environment"
        private const val PROPERTY_MAX_CONNECTIONS = "max.connections"
        private const val PROPERTY_CACHE_ENABLED = "cache.enabled"
        private const val PROPERTY_NON_EXISTENT = "property-does-not-exist"

        // Property values
        private const val VALUE_API_URL = "https://api.example.com"
        private const val VALUE_ENV_PRODUCTION = "production"
        private const val VALUE_MAX_CONNECTIONS = 100
        private const val VALUE_CACHE_ENABLED = true

        // Groups
        private const val GROUP_UI = "ui-features"
        private const val GROUP_BILLING = "billing-features"
        private const val GROUP_NON_EXISTENT = "group-does-not-exist"

        private val propertyAsStringData = listOf(
            PropertyAsStringData(PropertyString(PROPERTY_API_URL, VALUE_API_URL), VALUE_API_URL),
            PropertyAsStringData(
                PropertyInt(PROPERTY_MAX_CONNECTIONS, VALUE_MAX_CONNECTIONS),
                VALUE_MAX_CONNECTIONS.toString(),
            ),
            PropertyAsStringData(
                PropertyBoolean(PROPERTY_CACHE_ENABLED, VALUE_CACHE_ENABLED),
                VALUE_CACHE_ENABLED.toString(),
            ),
        )
    }
}

private data class PropertyAsStringData(
    val property: Property<*>,
    val expectedValue: String,
)
