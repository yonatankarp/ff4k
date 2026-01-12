package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Feature.
 *
 * @author Yonatan Karp-Rudin
 */
class FeatureTest {

    private val json = Json {
        serializersModule = ff4kSerializersModule
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `should create feature with default values`() {
        // When
        val feature = Feature(uid = FEATURE_UID)

        // Then
        assertEquals(FEATURE_UID, feature.uid)
        assertFalse(feature.isEnabled)
        assertNull(feature.description)
        assertNull(feature.group)
        assertTrue(feature.permissions.isEmpty())
        assertNull(feature.flippingStrategy)
        assertTrue(feature.customProperties.isEmpty())
    }

    @Test
    fun `should create feature with all properties`() {
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
        assertEquals(FEATURE_UID, feature.uid)
        assertEquals(isEnabled, feature.isEnabled)
        assertEquals(description, feature.description)
        assertEquals(group, feature.group)
        assertEquals(permissions, feature.permissions)
        assertEquals(properties, feature.customProperties)
    }

    @Test
    fun `should throw when uid is blank`() {
        // Given
        val uid = ""

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            Feature(uid = uid)
        }
    }

    @Test
    fun `should throw when uid is whitespace only`() {
        // Given
        val uid = "   "

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            Feature(uid = uid)
        }
    }

    @Test
    fun `should throw when group is blank`() {
        // Given
        val group = ""

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            Feature(uid = FEATURE_UID, group = group)
        }
    }

    @Test
    fun `should throw when group is whitespace only`() {
        // Given
        val group = "   "

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            Feature(uid = FEATURE_UID, group = group)
        }
    }

    @Test
    fun `enable should return new feature with isEnabled true`() {
        // Given
        val feature = Feature(uid = FEATURE_UID, isEnabled = false)

        // When
        val enabled = feature.enable()

        // Then
        assertTrue(enabled.isEnabled)
        assertFalse(feature.isEnabled) // Original unchanged
    }

    @Test
    fun `disable should return new feature with isEnabled false`() {
        // Given
        val feature = Feature(uid = FEATURE_UID, isEnabled = true)

        // When
        val disabled = feature.disable()

        // Then
        assertFalse(disabled.isEnabled)
        assertTrue(feature.isEnabled) // Original unchanged
    }

    @Test
    fun `toggle should return new feature with inverted isEnabled`() {
        // Given
        val enabledFeature = Feature(uid = FEATURE_UID, isEnabled = true)
        val disabledFeature = Feature(uid = FEATURE_UID, isEnabled = false)

        // When
        val toggledFromEnabled = enabledFeature.toggle()
        val toggledFromDisabled = disabledFeature.toggle()

        // Then
        assertFalse(toggledFromEnabled.isEnabled)
        assertTrue(toggledFromDisabled.isEnabled)
        assertTrue(enabledFeature.isEnabled) // Original unchanged
        assertFalse(disabledFeature.isEnabled) // Original unchanged
    }

    @Test
    fun `addProperty should return new feature with property added`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)

        // When
        val updated = feature.addProperty(property)

        // Then
        assertEquals(1, updated.customProperties.size)
        assertEquals(property, updated.customProperties[PROPERTY_NAME])
        assertTrue(feature.customProperties.isEmpty()) // Original unchanged
    }

    @Test
    fun `addProperty should replace existing property with same name`() {
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
        assertEquals(1, updated.customProperties.size)
        assertEquals(newProperty, updated.customProperties[PROPERTY_NAME])
        assertEquals(
            newValue,
            (updated.customProperties[PROPERTY_NAME] as PropertyInt).value,
        )
    }

    @Test
    fun `addProperty should preserve existing properties`() {
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
        assertEquals(2, updated.customProperties.size)
        assertEquals(
            existingProperty,
            updated.customProperties[existingPropertyName],
        )
        assertEquals(newProperty, updated.customProperties[PROPERTY_NAME])
    }

    @Test
    fun `displayStrategyClassName should return null when no strategy`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val className = feature.displayStrategyClassName

        // Then
        assertNull(className)
    }

    @Test
    fun `displayStrategyClassName should return strategy class name when strategy exists`() {
        // Given
        class TestStrategy : FlippingStrategy {
            override val initParams = emptyMap<String, String>()
        }

        val strategy = TestStrategy()
        val feature = Feature(uid = FEATURE_UID, flippingStrategy = strategy)

        // When
        val className = feature.displayStrategyClassName

        // Then
        assertEquals("TestStrategy", className)
    }

    @Test
    fun `getProperty should return property when it exists`() {
        // Given
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(PROPERTY_NAME to property),
        )

        // When
        val retrieved = feature.getProperty<Int>(PROPERTY_NAME)

        // Then
        assertEquals(property, retrieved)
        assertEquals(PROPERTY_VALUE, retrieved?.value)
    }

    @Test
    fun `getProperty should return null when it does not exist`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val retrieved = feature.getProperty<Int>(PROPERTY_NAME)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `should serialize feature with properties correctly`() {
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
        assertTrue(""""type": "int"""" in jsonString)
        assertTrue(""""type": "string"""" in jsonString)

        val deserialized = json.decodeFromString<Feature>(jsonString)
        assertEquals(feature, deserialized)
    }

    @Test
    fun `should deserialize feature from json correctly`() {
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
        assertEquals(FEATURE_UID, feature.uid)
        assertTrue(feature.isEnabled)
        assertEquals(2, feature.customProperties.size)

        val maxRetries = feature.customProperties[PROPERTY_NAME]
        assertTrue(maxRetries is PropertyInt)
        assertEquals(PROPERTY_VALUE, maxRetries.value)

        val region = feature.customProperties[REGION_PROPERTY_NAME]
        assertTrue(region is PropertyString)
        assertEquals(REGION_PROPERTY_VALUE, region.value)
    }

    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val PROPERTY_NAME = "maxRetries"
        private const val PROPERTY_VALUE = 3
        private const val REGION_PROPERTY_NAME = "region"
        private const val REGION_PROPERTY_VALUE = "US"
    }
}
