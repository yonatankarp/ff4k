package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

/**
 * Tests for Feature.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FeatureTest :
    FunSpec({

        val json = Json {
            serializersModule = ff4kSerializersModule
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        test("should create feature with default values") {
            // When
            val feature = Feature(uid = FEATURE_UID)

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled.shouldBeFalse()
            feature.description.shouldBeNull()
            feature.group.shouldBeNull()
            feature.permissions.shouldBeEmpty()
            feature.flippingStrategy.shouldBeNull()
            feature.customProperties.shouldBeEmpty()
        }

        test("should create feature with all properties") {
            // Given
            val isEnabled = true
            val description = "Test feature"
            val group = "test-group"
            val permissions = setOf("ADMIN", "USER")
            val properties =
                mapOf("key" to PropertyString(name = "key", value = "value"))

            // When
            val feature = Feature(
                uid = FEATURE_UID,
                isEnabled = isEnabled,
                description = description,
                group = group,
                permissions = permissions,
                customProperties = properties,
            )

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled shouldBe isEnabled
            feature.description shouldBe description
            feature.group shouldBe group
            feature.permissions shouldBe permissions
            feature.customProperties shouldBe properties
        }

        context("validation should throw IllegalArgumentException") {
            withData(
                nameFn = { it.description },
                featureValidationData,
            ) { (uid, group, _) ->
                shouldThrow<IllegalArgumentException> {
                    Feature(uid = uid, group = group)
                }
            }
        }

        test("enable should return new feature with isEnabled true") {
            // Given
            val feature = Feature(uid = FEATURE_UID, isEnabled = false)

            // When
            val enabled = feature.enable()

            // Then
            enabled.isEnabled.shouldBeTrue()
            feature.isEnabled.shouldBeFalse() // Original unchanged
        }

        test("disable should return new feature with isEnabled false") {
            // Given
            val feature = Feature(uid = FEATURE_UID, isEnabled = true)

            // When
            val disabled = feature.disable()

            // Then
            disabled.isEnabled.shouldBeFalse()
            feature.isEnabled.shouldBeTrue() // Original unchanged
        }

        test("toggle should return new feature with inverted isEnabled") {
            // Given
            val enabledFeature = Feature(uid = FEATURE_UID, isEnabled = true)
            val disabledFeature = Feature(uid = FEATURE_UID, isEnabled = false)

            // When
            val toggledFromEnabled = enabledFeature.toggle()
            val toggledFromDisabled = disabledFeature.toggle()

            // Then
            toggledFromEnabled.isEnabled.shouldBeFalse()
            toggledFromDisabled.isEnabled.shouldBeTrue()
            enabledFeature.isEnabled.shouldBeTrue() // Original unchanged
            disabledFeature.isEnabled.shouldBeFalse() // Original unchanged
        }

        test("addProperty should return new feature with property added") {
            // Given
            val feature = Feature(uid = FEATURE_UID)
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)

            // When
            val updated = feature.addProperty(property)

            // Then
            updated.customProperties.size shouldBe 1
            updated.customProperties[PROPERTY_NAME] shouldBe property
            feature.customProperties.shouldBeEmpty() // Original unchanged
        }

        test("addProperty should replace existing property with same name") {
            // Given
            val oldValue = 3
            val newValue = 5
            val oldProperty = PropertyInt(name = PROPERTY_NAME, value = oldValue)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to oldProperty),
            )
            val newProperty = PropertyInt(name = PROPERTY_NAME, value = newValue)

            // When
            val updated = feature.addProperty(newProperty)

            // Then
            updated.customProperties.size shouldBe 1
            updated.customProperties[PROPERTY_NAME] shouldBe newProperty
            (updated.customProperties[PROPERTY_NAME] as PropertyInt).value shouldBe newValue
        }

        test("addProperty should preserve existing properties") {
            // Given
            val existingPropertyName = "region"
            val existingPropertyValue = "US"
            val existingProperty = PropertyString(
                name = existingPropertyName,
                value = existingPropertyValue,
            )
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(existingPropertyName to existingProperty),
            )
            val newProperty =
                PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)

            // When
            val updated = feature.addProperty(newProperty)

            // Then
            updated.customProperties.size shouldBe 2
            updated.customProperties[existingPropertyName] shouldBe existingProperty
            updated.customProperties[PROPERTY_NAME] shouldBe newProperty
        }

        test("displayStrategyClassName should return null when no strategy") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val className = feature.displayStrategyClassName

            // Then
            className.shouldBeNull()
        }

        test("displayStrategyClassName should return strategy class name when strategy exists") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy()
            val feature = Feature(uid = FEATURE_UID, flippingStrategy = strategy)

            // When
            val className = feature.displayStrategyClassName

            // Then
            className shouldBe "AlwaysTrueFlippingStrategy"
        }

        test("getProperty should return property when it exists") {
            // Given
            val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
            val feature = Feature(
                uid = FEATURE_UID,
                customProperties = mapOf(PROPERTY_NAME to property),
            )

            // When
            val retrieved = feature.getProperty<Int>(PROPERTY_NAME)

            // Then
            retrieved.shouldNotBeNull().let {
                it shouldBe property
                it.value shouldBe PROPERTY_VALUE
            }
        }

        test("getProperty should return null when it does not exist") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val retrieved = feature.getProperty<Int>(PROPERTY_NAME)

            // Then
            retrieved.shouldBeNull()
        }

        test("should serialize feature with properties correctly") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                isEnabled = true,
                customProperties = mapOf(
                    PROPERTY_NAME to PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE),
                    REGION_PROPERTY_NAME to PropertyString(name = REGION_PROPERTY_NAME, value = REGION_PROPERTY_VALUE),
                ),
            )

            // When
            val jsonString = json.encodeToString(Feature.serializer(), feature)

            // Then
            jsonString shouldContain """"type": "int""""
            jsonString shouldContain """"type": "string""""

            val deserialized = json.decodeFromString<Feature>(jsonString)
            deserialized shouldBe feature
        }

        test("should deserialize feature from json correctly") {
            // Given
            // language=json
            val jsonString = """
            {
                "uid": "$FEATURE_UID",
                "isEnabled": true,
                "customProperties": {
                    "$PROPERTY_NAME": {
                        "type": "int",
                        "name": "$PROPERTY_NAME",
                        "value": $PROPERTY_VALUE,
                        "fixedValues": [],
                        "readOnly": false
                    },
                    "$REGION_PROPERTY_NAME": {
                        "type": "string",
                        "name": "$REGION_PROPERTY_NAME",
                        "value": "$REGION_PROPERTY_VALUE",
                        "fixedValues": [],
                        "readOnly": false
                    }
                }
            }
            """.trimIndent()

            // When
            val feature = json.decodeFromString<Feature>(jsonString)

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled.shouldBeTrue()
            feature.customProperties.size shouldBe 2

            val maxRetries = feature.customProperties[PROPERTY_NAME]
            maxRetries.shouldBeInstanceOf<PropertyInt>()
            maxRetries.value shouldBe PROPERTY_VALUE

            val region = feature.customProperties[REGION_PROPERTY_NAME]
            region.shouldBeInstanceOf<PropertyString>()
            region.value shouldBe REGION_PROPERTY_VALUE
        }
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val PROPERTY_NAME = "maxRetries"
        private const val PROPERTY_VALUE = 3
        private const val REGION_PROPERTY_NAME = "region"
        private const val REGION_PROPERTY_VALUE = "US"

        private val featureValidationData = listOf(
            FeatureValidationData(uid = "", description = "when uid is blank"),
            FeatureValidationData(uid = "   ", description = "when uid is whitespace only"),
            FeatureValidationData(group = "", description = "when group is blank"),
            FeatureValidationData(group = "   ", description = "when group is whitespace only"),
        )
    }
}

private data class FeatureValidationData(
    val uid: String = "valid-uid",
    val group: String? = null,
    val description: String,
)
