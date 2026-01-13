package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the FF4k main class.
 *
 * @author Yonatan Karp-Rudin
 */
class FF4kTest {

    @Test
    fun `check should return true for enabled feature`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true))

        // When
        val result = ff4k.check(FEATURE_DARK_MODE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `check should return false for disabled feature`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = false))

        // When
        val result = ff4k.check(FEATURE_DARK_MODE)

        // Then
        assertFalse(result)
    }

    @Test
    fun `check should throw when feature does not exist and autoCreate is false`() = runTest {
        // Given
        val ff4k = FF4k(autoCreate = false)

        // When/Then
        assertFailsWith<FeatureNotFoundException> {
            ff4k.check(FEATURE_NON_EXISTENT)
        }
    }

    @Test
    fun `check should return false when feature does not exist and autoCreate is true`() = runTest {
        // Given
        val ff4k = FF4k(autoCreate = true)

        // When
        val result = ff4k.check(FEATURE_NEW_CHECKOUT)

        // Then
        assertFalse(result)
        assertTrue(ff4k.hasFeature(FEATURE_NEW_CHECKOUT))
    }

    @Test
    fun `features should return all features`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = true)
        ff4k.addFeature(FEATURE_BETA, isEnabled = false)

        // When
        val features = ff4k.features()

        // Then
        assertEquals(2, features.size)
        assertTrue(FEATURE_DARK_MODE in features)
        assertTrue(FEATURE_BETA in features)
    }

    @Test
    fun `feature should return feature by id`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, description = DESCRIPTION_DARK_MODE))

        // When
        val feature = ff4k.feature(FEATURE_DARK_MODE)

        // Then
        assertEquals(FEATURE_DARK_MODE, feature.uid)
        assertEquals(DESCRIPTION_DARK_MODE, feature.description)
        assertTrue(feature.isEnabled)
    }

    @Test
    fun `hasFeature should return true for existing feature`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(FEATURE_DARK_MODE)

        // When/Then
        assertTrue(ff4k.hasFeature(FEATURE_DARK_MODE))
    }

    @Test
    fun `hasFeature should return false for non-existing feature`() = runTest {
        // Given
        val ff4k = FF4k()

        // When/Then
        assertFalse(ff4k.hasFeature(FEATURE_NON_EXISTENT))
    }

    @Test
    fun `deleteFeature should remove feature`() = runTest {
        // Given
        val ff4k = FF4k()
        val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)
        ff4k.addFeature(feature)

        // When
        ff4k.deleteFeature(feature)

        // Then
        assertFalse(ff4k.hasFeature(FEATURE_DARK_MODE))
    }

    @Test
    fun `enable should enable a disabled feature`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = false)

        // When
        ff4k.enable(FEATURE_DARK_MODE)

        // Then
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
    }

    @Test
    fun `disable should disable an enabled feature`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(FEATURE_DARK_MODE, isEnabled = true)

        // When
        ff4k.disable(FEATURE_DARK_MODE)

        // Then
        assertFalse(ff4k.check(FEATURE_DARK_MODE))
    }

    @Test
    fun `enable should auto-create feature when autoCreate is true`() = runTest {
        // Given
        val ff4k = FF4k(autoCreate = true)

        // When
        ff4k.enable(FEATURE_NEW_CHECKOUT)

        // Then
        assertTrue(ff4k.hasFeature(FEATURE_NEW_CHECKOUT))
        assertTrue(ff4k.check(FEATURE_NEW_CHECKOUT))
    }

    @Test
    fun `disable should auto-create feature when autoCreate is true`() = runTest {
        // Given
        val ff4k = FF4k(autoCreate = true)

        // When
        ff4k.disable(FEATURE_NEW_CHECKOUT)

        // Then
        assertTrue(ff4k.hasFeature(FEATURE_NEW_CHECKOUT))
        assertFalse(ff4k.check(FEATURE_NEW_CHECKOUT))
    }

    @Test
    fun `properties should return all properties`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))
        ff4k.addProperty(PropertyString(PROPERTY_ENV, VALUE_ENV_PRODUCTION))

        // When
        val properties = ff4k.properties()

        // Then
        assertEquals(2, properties.size)
        assertTrue(PROPERTY_API_URL in properties)
        assertTrue(PROPERTY_ENV in properties)
    }

    @Test
    fun `property should return property by name`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))

        // When
        val property = ff4k.property<String>(PROPERTY_API_URL)

        // Then
        assertNotNull(property)
        assertEquals(VALUE_API_URL, property.value)
    }

    @Test
    fun `property should return null for non-existing property`() = runTest {
        // Given
        val ff4k = FF4k()

        // When
        val property = ff4k.property<String>(PROPERTY_NON_EXISTENT)

        // Then
        assertNull(property)
    }

    @Test
    fun `hasProperty should return true for existing property`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))

        // When/Then
        assertTrue(ff4k.hasProperty(PROPERTY_API_URL))
    }

    @Test
    fun `hasProperty should return false for non-existing property`() = runTest {
        // Given
        val ff4k = FF4k()

        // When/Then
        assertFalse(ff4k.hasProperty(PROPERTY_NON_EXISTENT))
    }

    @Test
    fun `deleteProperty should remove property`() = runTest {
        // Given
        val ff4k = FF4k()
        val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)
        ff4k.addProperty(property)

        // When
        ff4k.deleteProperty(property)

        // Then
        assertFalse(ff4k.hasProperty(PROPERTY_API_URL))
    }

    @Test
    fun `propertyAsString should return property value as string`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyString(PROPERTY_API_URL, VALUE_API_URL))

        // When
        val value = ff4k.propertyAsString<String>(PROPERTY_API_URL)

        // Then
        assertEquals(VALUE_API_URL, value)
    }

    @Test
    fun `propertyAsString should return int property value as string`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyInt(PROPERTY_MAX_CONNECTIONS, VALUE_MAX_CONNECTIONS))

        // When
        val value = ff4k.propertyAsString<Int>(PROPERTY_MAX_CONNECTIONS)

        // Then
        assertEquals(VALUE_MAX_CONNECTIONS.toString(), value)
    }

    @Test
    fun `propertyAsString should return boolean property value as string`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addProperty(PropertyBoolean(PROPERTY_CACHE_ENABLED, VALUE_CACHE_ENABLED))

        // When
        val value = ff4k.propertyAsString<Boolean>(PROPERTY_CACHE_ENABLED)

        // Then
        assertEquals(VALUE_CACHE_ENABLED.toString(), value)
    }

    @Test
    fun `featuresByGroup should return features in group`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = false, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = true, group = GROUP_BILLING))

        // When
        val uiFeatures = ff4k.featuresByGroup(GROUP_UI)

        // Then
        assertEquals(2, uiFeatures.size)
        assertTrue(FEATURE_DARK_MODE in uiFeatures)
        assertTrue(FEATURE_BETA in uiFeatures)
    }

    @Test
    fun `containGroup should return true for existing group`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))

        // When/Then
        assertTrue(ff4k.containGroup(GROUP_UI))
    }

    @Test
    fun `containGroup should return false for non-existing group`() = runTest {
        // Given
        val ff4k = FF4k()

        // When/Then
        assertFalse(ff4k.containGroup(GROUP_NON_EXISTENT))
    }

    @Test
    fun `enableGroup should enable all features in group`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = false, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = false, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = false, group = GROUP_BILLING))

        // When
        ff4k.enableGroup(GROUP_UI)

        // Then
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
        assertTrue(ff4k.check(FEATURE_BETA))
        assertFalse(ff4k.check(FEATURE_PREMIUM)) // Different group, unchanged
    }

    @Test
    fun `disableGroup should disable all features in group`() = runTest {
        // Given
        val ff4k = FF4k()
        ff4k.addFeature(Feature(FEATURE_DARK_MODE, isEnabled = true, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_BETA, isEnabled = true, group = GROUP_UI))
        ff4k.addFeature(Feature(FEATURE_PREMIUM, isEnabled = true, group = GROUP_BILLING))

        // When
        ff4k.disableGroup(GROUP_UI)

        // Then
        assertFalse(ff4k.check(FEATURE_DARK_MODE))
        assertFalse(ff4k.check(FEATURE_BETA))
        assertTrue(ff4k.check(FEATURE_PREMIUM)) // Different group, unchanged
    }

    @Test
    fun `fluent API should allow method chaining`() = runTest {
        // Given/When
        val ff4k = FF4k()
            .addFeature(FEATURE_DARK_MODE, isEnabled = true)
            .addFeature(FEATURE_BETA, isEnabled = false)
            .addProperty(PropertyString(PROPERTY_ENV, VALUE_ENV_PRODUCTION))
            .enable(FEATURE_BETA)

        // Then
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
        assertTrue(ff4k.check(FEATURE_BETA))
        assertTrue(ff4k.hasProperty(PROPERTY_ENV))
    }

    @Test
    fun `fluent API methods should return same instance`() = runTest {
        // Given
        val ff4k = FF4k()

        // When
        val result = ff4k.addFeature(FEATURE_DARK_MODE)

        // Then
        assertSame(ff4k, result)
    }

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
    }
}
