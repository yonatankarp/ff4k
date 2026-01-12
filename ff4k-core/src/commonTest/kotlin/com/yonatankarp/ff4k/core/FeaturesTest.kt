package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for Feature extension functions defined in Features.kt.
 *
 * @author Yonatan Karp-Rudin
 */
class FeaturesTest {

    // ============================================================================
    // Extension Properties Tests
    // ============================================================================

    @Test
    fun `isDisabled should return true when feature is disabled`() {
        // Given
        val feature = Feature(uid = FEATURE_UID, isEnabled = false)

        // When
        val result = feature.isDisabled

        // Then
        assertTrue(result)
    }

    @Test
    fun `isDisabled should return false when feature is enabled`() {
        // Given
        val feature = Feature(uid = FEATURE_UID, isEnabled = true)

        // When
        val result = feature.isDisabled

        // Then
        assertFalse(result)
    }

    @Test
    fun `propertyNames should return empty set when no properties`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val names = feature.propertyNames

        // Then
        assertTrue(names.isEmpty())
    }

    @Test
    fun `propertyNames should return all property names when properties exist`() {
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
        assertEquals(2, names.size)
        assertTrue(TIMEOUT_PROPERTY in names)
        assertTrue(REGION_PROPERTY in names)
    }

    @Test
    fun `hasPermissions should return false when no permissions`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val result = feature.hasPermissions

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasPermissions should return true when permissions exist`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_USER_PERMISSIONS,
        )

        // When
        val result = feature.hasPermissions

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasFlippingStrategy should return false when no strategy`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val result = feature.hasFlippingStrategy

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasFlippingStrategy should return true when strategy exists`() {
        // Given
        class TestStrategy : FlippingStrategy {
            override val initParams = emptyMap<String, String>()
        }
        val feature = Feature(
            uid = FEATURE_UID,
            flippingStrategy = TestStrategy(),
        )

        // When
        val result = feature.hasFlippingStrategy

        // Then
        assertTrue(result)
    }

    // ============================================================================
    // Property Access Extensions Tests
    // ============================================================================

    @Test
    fun `getPropertyOrThrow should return property when it exists`() {
        // Given
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(PROPERTY_NAME to property),
        )

        // When
        val retrieved = feature.getPropertyOrThrow<Int>(PROPERTY_NAME)

        // Then
        assertEquals(property, retrieved)
        assertEquals(PROPERTY_VALUE, retrieved.value)
    }

    @Test
    fun `getPropertyOrThrow should throw PropertyNotFoundException when property does not exist`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When / Then
        assertFailsWith<PropertyNotFoundException> {
            feature.getPropertyOrThrow<Int>("nonexistent")
        }
    }

    @Test
    fun `getPropertyValueOrDefault should return property value when it exists`() {
        // Given
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(PROPERTY_NAME to property),
        )

        // When
        val value = feature.getPropertyValueOrDefault(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

        // Then
        assertEquals(PROPERTY_VALUE, value)
    }

    @Test
    fun `getPropertyValueOrDefault should return default value when property does not exist`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)
        // When
        val value = feature.getPropertyValueOrDefault(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

        // Then
        assertEquals(DEFAULT_FALLBACK_VALUE, value)
    }

    @Test
    fun `getPropertyValueOrDefault should return default value when property is null`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val value = feature.getPropertyValueOrDefault(NONEXISTENT_PROPERTY, DEFAULT_FALLBACK_VALUE)

        // Then
        assertEquals(DEFAULT_FALLBACK_VALUE, value)
    }

    @Test
    fun `hasPropertyWithValue should return true when property exists with matching value`() {
        // Given
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(PROPERTY_NAME to property),
        )

        // When
        val result = feature.hasPropertyWithValue(PROPERTY_NAME, PROPERTY_VALUE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasPropertyWithValue should return false when property exists with different value`() {
        // Given
        val property = PropertyInt(name = PROPERTY_NAME, value = PROPERTY_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(PROPERTY_NAME to property),
        )

        // When
        val result = feature.hasPropertyWithValue(PROPERTY_NAME, DEFAULT_FALLBACK_VALUE)

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasPropertyWithValue should return false when property does not exist`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val result = feature.hasPropertyWithValue(PROPERTY_NAME, PROPERTY_VALUE)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getPropertiesOfType should return all properties of specified type`() {
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
        assertEquals(2, intProps.size)
        assertEquals(timeoutProp, intProps[TIMEOUT_PROPERTY])
        assertEquals(cacheProp, intProps[CACHE_PROPERTY])
    }

    @Test
    fun `getPropertiesOfType should return empty map when no properties of type exist`() {
        // Given
        val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(REGION_PROPERTY to regionProp),
        )

        // When
        val intProps = feature.getPropertiesOfType<Int>()

        // Then
        assertTrue(intProps.isEmpty())
    }

    @Test
    fun `getPropertiesOfType should return empty map when no properties exist`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val intProps = feature.getPropertiesOfType<Int>()

        // Then
        assertTrue(intProps.isEmpty())
    }

    // ============================================================================
    // Property Manipulation Extensions Tests
    // ============================================================================

    @Test
    fun `addProperties vararg should return new feature with properties added`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)
        val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
        val regionProp = PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE)

        // When
        val updated = feature.addProperties(timeoutProp, regionProp)

        // Then
        assertEquals(2, updated.customProperties.size)
        assertEquals(timeoutProp, updated.customProperties[TIMEOUT_PROPERTY])
        assertEquals(regionProp, updated.customProperties[REGION_PROPERTY])
        assertTrue(feature.customProperties.isEmpty()) // Original unchanged
    }

    @Test
    fun `addProperties vararg should replace existing properties with same name`() {
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
        assertEquals(1, updated.customProperties.size)
        assertEquals(newTimeoutProp, updated.customProperties[TIMEOUT_PROPERTY])
        assertEquals(DEFAULT_FALLBACK_VALUE, (updated.customProperties[TIMEOUT_PROPERTY] as PropertyInt).value)
    }

    @Test
    fun `addProperties Collection should return new feature with properties added`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)
        val properties = listOf(
            PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE),
            PropertyString(name = REGION_PROPERTY, value = TEST_STRING_VALUE),
        )

        // When
        val updated = feature.addProperties(properties)

        // Then
        assertEquals(2, updated.customProperties.size)
        assertEquals(properties[0], updated.customProperties[TIMEOUT_PROPERTY])
        assertEquals(properties[1], updated.customProperties[REGION_PROPERTY])
        assertTrue(feature.customProperties.isEmpty()) // Original unchanged
    }

    @Test
    fun `addProperties Collection should preserve existing properties`() {
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
        assertEquals(3, updated.customProperties.size)
        assertEquals(existingProp, updated.customProperties[EXISTING_PROPERTY])
        assertEquals(newProperties[0], updated.customProperties[TIMEOUT_PROPERTY])
        assertEquals(newProperties[1], updated.customProperties[REGION_PROPERTY])
    }

    @Test
    fun `removeProperties vararg should return new feature with properties removed`() {
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
        assertEquals(1, updated.customProperties.size)
        assertEquals(regionProp, updated.customProperties[REGION_PROPERTY])
        assertFalse(updated.customProperties.containsKey(TIMEOUT_PROPERTY))
        assertFalse(updated.customProperties.containsKey(CACHE_PROPERTY))
        assertEquals(3, feature.customProperties.size) // Original unchanged
    }

    @Test
    fun `removeProperties vararg should ignore non-existent properties`() {
        // Given
        val timeoutProp = PropertyInt(name = TIMEOUT_PROPERTY, value = TIMEOUT_VALUE)
        val feature = Feature(
            uid = FEATURE_UID,
            customProperties = mapOf(TIMEOUT_PROPERTY to timeoutProp),
        )

        // When
        val updated = feature.removeProperties(TIMEOUT_PROPERTY, NONEXISTENT_PROPERTY, CACHE_PROPERTY)

        // Then
        assertTrue(updated.customProperties.isEmpty())
    }

    @Test
    fun `removeProperties Collection should return new feature with properties removed`() {
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
        assertEquals(1, updated.customProperties.size)
        assertEquals(regionProp, updated.customProperties[REGION_PROPERTY])
        assertFalse(updated.customProperties.containsKey(TIMEOUT_PROPERTY))
        assertEquals(2, feature.customProperties.size) // Original unchanged
    }

    @Test
    fun `clearProperties should return new feature with no custom properties`() {
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
        assertTrue(updated.customProperties.isEmpty())
        assertEquals(2, feature.customProperties.size) // Original unchanged
    }

    @Test
    fun `clearProperties should work on feature with no properties`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val updated = feature.clearProperties()

        // Then
        assertTrue(updated.customProperties.isEmpty())
    }

    // ============================================================================
    // Permission Extensions Tests
    // ============================================================================

    @Test
    fun `grantPermissions vararg should return new feature with permissions added`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

        // Then
        assertEquals(2, updated.permissions.size)
        assertTrue(ADMIN_PERMISSION in updated.permissions)
        assertTrue(USER_PERMISSION in updated.permissions)
        assertTrue(feature.permissions.isEmpty()) // Original unchanged
    }

    @Test
    fun `grantPermissions vararg should preserve existing permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = setOf(EXISTING_PERMISSION),
        )

        // When
        val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

        // Then
        assertEquals(3, updated.permissions.size)
        assertTrue(EXISTING_PERMISSION in updated.permissions)
        assertTrue(ADMIN_PERMISSION in updated.permissions)
        assertTrue(USER_PERMISSION in updated.permissions)
    }

    @Test
    fun `grantPermissions vararg should not duplicate existing permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_ONLY_PERMISSION,
        )

        // When
        val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

        // Then
        assertEquals(2, updated.permissions.size)
        assertTrue(ADMIN_PERMISSION in updated.permissions)
        assertTrue(USER_PERMISSION in updated.permissions)
    }

    @Test
    fun `grantPermissions Collection should return new feature with permissions added`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)
        val permissions = listOf(ADMIN_PERMISSION, USER_PERMISSION)

        // When
        val updated = feature.grantPermissions(permissions)

        // Then
        assertEquals(2, updated.permissions.size)
        assertTrue(ADMIN_PERMISSION in updated.permissions)
        assertTrue(USER_PERMISSION in updated.permissions)
        assertTrue(feature.permissions.isEmpty()) // Original unchanged
    }

    @Test
    fun `revokePermissions vararg should return new feature with permissions removed`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ALL_PERMISSIONS,
        )

        // When
        val updated = feature.revokePermissions(ADMIN_PERMISSION, GUEST_PERMISSION)

        // Then
        assertEquals(1, updated.permissions.size)
        assertTrue(USER_PERMISSION in updated.permissions)
        assertFalse(ADMIN_PERMISSION in updated.permissions)
        assertFalse(GUEST_PERMISSION in updated.permissions)
        assertEquals(3, feature.permissions.size) // Original unchanged
    }

    @Test
    fun `revokePermissions vararg should ignore non-existent permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_ONLY_PERMISSION,
        )

        // When
        val updated = feature.revokePermissions(ADMIN_PERMISSION, NONEXISTENT_PERMISSION)

        // Then
        assertTrue(updated.permissions.isEmpty())
    }

    @Test
    fun `revokePermissions Collection should return new feature with permissions removed`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_USER_PERMISSIONS,
        )

        // When
        val updated = feature.revokePermissions(setOf(ADMIN_PERMISSION))

        // Then
        assertEquals(1, updated.permissions.size)
        assertTrue(USER_PERMISSION in updated.permissions)
        assertFalse(ADMIN_PERMISSION in updated.permissions)
        assertEquals(2, feature.permissions.size) // Original unchanged
    }

    @Test
    fun `hasAnyPermission should return true when feature has at least one permission`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_USER_PERMISSIONS,
        )

        // When
        val hasAdmin = feature.hasAnyPermission(ADMIN_PERMISSION, GUEST_PERMISSION)
        val hasUser = feature.hasAnyPermission(USER_PERMISSION)
        val hasNonexistentOrUser = feature.hasAnyPermission(NONEXISTENT_PERMISSION, USER_PERMISSION)

        // Then
        assertTrue(hasAdmin)
        assertTrue(hasUser)
        assertTrue(hasNonexistentOrUser)
    }

    @Test
    fun `hasAnyPermission should return false when feature has none of the permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_ONLY_PERMISSION,
        )

        // When
        val result = feature.hasAnyPermission(USER_PERMISSION, GUEST_PERMISSION)

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasAnyPermission should return false when feature has no permissions`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val result = feature.hasAnyPermission(ADMIN_PERMISSION, USER_PERMISSION)

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasAllPermissions should return true when feature has all specified permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ALL_PERMISSIONS,
        )

        // When
        val hasAdminAndUser = feature.hasAllPermissions(ADMIN_PERMISSION, USER_PERMISSION)
        val hasGuest = feature.hasAllPermissions(GUEST_PERMISSION)
        val hasAll = feature.hasAllPermissions(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION)

        // Then
        assertTrue(hasAdminAndUser)
        assertTrue(hasGuest)
        assertTrue(hasAll)
    }

    @Test
    fun `hasAllPermissions should return false when feature is missing any permission`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ADMIN_USER_PERMISSIONS,
        )

        // When
        val hasAllIncludingGuest = feature.hasAllPermissions(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION)
        val hasGuest = feature.hasAllPermissions(GUEST_PERMISSION)

        // Then
        assertFalse(hasAllIncludingGuest)
        assertFalse(hasGuest)
    }

    @Test
    fun `hasAllPermissions should return false when feature has no permissions`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val result = feature.hasAllPermissions(ADMIN_PERMISSION)

        // Then
        assertFalse(result)
    }

    @Test
    fun `clearPermissions should return new feature with no permissions`() {
        // Given
        val feature = Feature(
            uid = FEATURE_UID,
            permissions = ALL_PERMISSIONS,
        )

        // When
        val updated = feature.clearPermissions()

        // Then
        assertTrue(updated.permissions.isEmpty())
        assertEquals(3, feature.permissions.size) // Original unchanged
    }

    @Test
    fun `clearPermissions should work on feature with no permissions`() {
        // Given
        val feature = Feature(uid = FEATURE_UID)

        // When
        val updated = feature.clearPermissions()

        // Then
        assertTrue(updated.permissions.isEmpty())
    }

    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val PROPERTY_NAME = "maxRetries"
        private const val PROPERTY_VALUE = 3

        // Property name constants
        private const val TIMEOUT_PROPERTY = "timeout"
        private const val REGION_PROPERTY = "region"
        private const val CACHE_PROPERTY = "cache"
        private const val EXISTING_PROPERTY = "existing"
        private const val NONEXISTENT_PROPERTY = "nonexistent"

        // Property value constants
        private const val TIMEOUT_VALUE = 5000
        private const val CACHE_SIZE = 100
        private const val DEFAULT_FALLBACK_VALUE = 999
        private const val TEST_STRING_VALUE = "us-east-1"

        // Permission constants
        private const val ADMIN_PERMISSION = "ADMIN"
        private const val USER_PERMISSION = "USER"
        private const val GUEST_PERMISSION = "GUEST"
        private const val EXISTING_PERMISSION = "EXISTING"
        private const val NONEXISTENT_PERMISSION = "NONEXISTENT"

        // Common permission sets
        private val ADMIN_USER_PERMISSIONS = setOf(ADMIN_PERMISSION, USER_PERMISSION)
        private val ALL_PERMISSIONS = setOf(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION)
        private val ADMIN_ONLY_PERMISSION = setOf(ADMIN_PERMISSION)
    }
}
