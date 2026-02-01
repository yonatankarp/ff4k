package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests for Feature extension functions defined in Features.kt.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FeaturesTest :
    FunSpec({

        // ============================================================================
        // Extension Properties Tests
        // ============================================================================

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

        // ============================================================================
        // Property Access Extensions Tests
        // ============================================================================

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

        // ============================================================================
        // Property Manipulation Extensions Tests
        // ============================================================================

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

        // ============================================================================
        // Permission Extensions Tests
        // ============================================================================

        test("grantPermissions vararg should return new feature with permissions added") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

            // Then
            updated.permissions.size shouldBe 2
            updated.permissions shouldContain ADMIN_PERMISSION
            updated.permissions shouldContain USER_PERMISSION
            feature.permissions.shouldBeEmpty() // Original unchanged
        }

        test("grantPermissions vararg should preserve existing permissions") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = setOf(EXISTING_PERMISSION),
            )

            // When
            val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

            // Then
            updated.permissions.size shouldBe 3
            updated.permissions shouldContain EXISTING_PERMISSION
            updated.permissions shouldContain ADMIN_PERMISSION
            updated.permissions shouldContain USER_PERMISSION
        }

        test("grantPermissions vararg should not duplicate existing permissions") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ADMIN_ONLY_PERMISSION,
            )

            // When
            val updated = feature.grantPermissions(ADMIN_PERMISSION, USER_PERMISSION)

            // Then
            updated.permissions.size shouldBe 2
            updated.permissions shouldContain ADMIN_PERMISSION
            updated.permissions shouldContain USER_PERMISSION
        }

        test("grantPermissions Collection should return new feature with permissions added") {
            // Given
            val feature = Feature(uid = FEATURE_UID)
            val permissions = listOf(ADMIN_PERMISSION, USER_PERMISSION)

            // When
            val updated = feature.grantPermissions(permissions)

            // Then
            updated.permissions.size shouldBe 2
            updated.permissions shouldContain ADMIN_PERMISSION
            updated.permissions shouldContain USER_PERMISSION
            feature.permissions.shouldBeEmpty() // Original unchanged
        }

        test("revokePermissions vararg should return new feature with permissions removed") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ALL_PERMISSIONS,
            )

            // When
            val updated = feature.revokePermissions(ADMIN_PERMISSION, GUEST_PERMISSION)

            // Then
            updated.permissions.size shouldBe 1
            updated.permissions shouldContain USER_PERMISSION
            updated.permissions shouldNotContain ADMIN_PERMISSION
            updated.permissions shouldNotContain GUEST_PERMISSION
            feature.permissions.size shouldBe 3 // Original unchanged
        }

        test("revokePermissions vararg should ignore non-existent permissions") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ADMIN_ONLY_PERMISSION,
            )

            // When
            val updated = feature.revokePermissions(ADMIN_PERMISSION, NONEXISTENT_PERMISSION)

            // Then
            updated.permissions.shouldBeEmpty()
        }

        test("revokePermissions Collection should return new feature with permissions removed") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ADMIN_USER_PERMISSIONS,
            )

            // When
            val updated = feature.revokePermissions(setOf(ADMIN_PERMISSION))

            // Then
            updated.permissions.size shouldBe 1
            updated.permissions shouldContain USER_PERMISSION
            updated.permissions shouldNotContain ADMIN_PERMISSION
            feature.permissions.size shouldBe 2 // Original unchanged
        }

        context("hasAnyPermission") {
            withData(
                nameFn = { it.description },
                hasAnyPermissionData,
            ) { (_, featurePermissions, permissionsToCheck, expected) ->
                // Given
                val feature = Feature(
                    uid = FEATURE_UID,
                    permissions = featurePermissions,
                )

                // When
                val result = feature.hasAnyPermission(*permissionsToCheck.toTypedArray())

                // Then
                result shouldBe expected
            }
        }

        context("hasAllPermissions") {
            withData(
                nameFn = { it.description },
                hasAllPermissionsData,
            ) { (_, featurePermissions, permissionsToCheck, expected) ->
                // Given
                val feature = Feature(
                    uid = FEATURE_UID,
                    permissions = featurePermissions,
                )

                // When
                val result = feature.hasAllPermissions(*permissionsToCheck.toTypedArray())

                // Then
                result shouldBe expected
            }
        }

        test("clearPermissions should return new feature with no permissions") {
            // Given
            val feature = Feature(
                uid = FEATURE_UID,
                permissions = ALL_PERMISSIONS,
            )

            // When
            val updated = feature.clearPermissions()

            // Then
            updated.permissions.shouldBeEmpty()
            feature.permissions.size shouldBe 3 // Original unchanged
        }

        test("clearPermissions should work on feature with no permissions") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.clearPermissions()

            // Then
            updated.permissions.shouldBeEmpty()
        }

        // ============================================================================
        // Group Management Extensions Tests
        // ============================================================================

        test("addGroup should return new feature with group assigned") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.addGroup(GROUP_NAME)

            // Then
            updated.group shouldBe GROUP_NAME
            feature.group.shouldBeNull() // Original unchanged
        }

        test("addGroup should overwrite existing group") {
            // Given
            val feature = Feature(uid = FEATURE_UID, group = EXISTING_GROUP)

            // When
            val updated = feature.addGroup(GROUP_NAME)

            // Then
            updated.group shouldBe GROUP_NAME
        }

        test("removeGroup should return new feature with no group") {
            // Given
            val feature = Feature(uid = FEATURE_UID, group = GROUP_NAME)

            // When
            val updated = feature.removeGroup()

            // Then
            updated.group.shouldBeNull()
            feature.group shouldBe GROUP_NAME // Original unchanged
        }

        test("removeGroup should work on feature with no group") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.removeGroup()

            // Then
            updated.group.shouldBeNull()
        }
    }) {
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

        // Group constants
        private const val GROUP_NAME = "beta-users"
        private const val EXISTING_GROUP = "alpha-users"

        private val hasAnyPermissionData = listOf(
            PermissionCheckData(
                description = "returns true when feature has at least one permission",
                featurePermissions = ADMIN_USER_PERMISSIONS,
                permissionsToCheck = setOf(ADMIN_PERMISSION, GUEST_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns true when feature has single matching permission",
                featurePermissions = ADMIN_USER_PERMISSIONS,
                permissionsToCheck = setOf(USER_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns true when feature has non-existent or user permission",
                featurePermissions = ADMIN_USER_PERMISSIONS,
                permissionsToCheck = setOf(NONEXISTENT_PERMISSION, USER_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns false when feature has none of the permissions",
                featurePermissions = ADMIN_ONLY_PERMISSION,
                permissionsToCheck = setOf(USER_PERMISSION, GUEST_PERMISSION),
                expected = false,
            ),
            PermissionCheckData(
                description = "returns false when feature has no permissions",
                featurePermissions = emptySet(),
                permissionsToCheck = setOf(ADMIN_PERMISSION, USER_PERMISSION),
                expected = false,
            ),
        )

        private val hasAllPermissionsData = listOf(
            PermissionCheckData(
                description = "returns true when feature has all specified permissions",
                featurePermissions = ALL_PERMISSIONS,
                permissionsToCheck = setOf(ADMIN_PERMISSION, USER_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns true when feature has single matching permission",
                featurePermissions = ALL_PERMISSIONS,
                permissionsToCheck = setOf(GUEST_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns true when feature has all permissions (full set)",
                featurePermissions = ALL_PERMISSIONS,
                permissionsToCheck = setOf(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION),
                expected = true,
            ),
            PermissionCheckData(
                description = "returns false when feature is missing any permission",
                featurePermissions = ADMIN_USER_PERMISSIONS,
                permissionsToCheck = setOf(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION),
                expected = false,
            ),
            PermissionCheckData(
                description = "returns false when feature has no permissions",
                featurePermissions = emptySet(),
                permissionsToCheck = setOf(ADMIN_PERMISSION),
                expected = false,
            ),
        )
    }

    private data class PermissionCheckData(
        val description: String,
        val featurePermissions: Set<String>,
        val permissionsToCheck: Set<String>,
        val expected: Boolean,
    )
}
