package com.yonatankarp.ff4k.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

internal class FeaturesPermissionTest :
    FunSpec({

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
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val ADMIN_PERMISSION = "ADMIN"
        private const val USER_PERMISSION = "USER"
        private const val GUEST_PERMISSION = "GUEST"
        private const val EXISTING_PERMISSION = "EXISTING"
        private const val NONEXISTENT_PERMISSION = "NONEXISTENT"

        private val ADMIN_USER_PERMISSIONS = setOf(ADMIN_PERMISSION, USER_PERMISSION)
        private val ALL_PERMISSIONS = setOf(ADMIN_PERMISSION, USER_PERMISSION, GUEST_PERMISSION)
        private val ADMIN_ONLY_PERMISSION = setOf(ADMIN_PERMISSION)

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
