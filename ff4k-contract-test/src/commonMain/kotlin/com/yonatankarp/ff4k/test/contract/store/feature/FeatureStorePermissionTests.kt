@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.ROLE
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal interface FeatureStorePermissionTests : FeatureStoreTestSupport {

    @Test
    fun `should grant role to feature`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertTrue(ROLE in feature.permissions)
    }

    @Test
    fun `should throw exception when granting role to non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.grantRoleToFeature(FEATURE_NAME, ROLE)
        }
    }

    @Test
    fun `should revoke role from feature`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // When
        store.revokeRoleFromFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertFalse(ROLE in feature.permissions)
    }

    @Test
    fun `should throw exception when revoking role from non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.revokeRoleFromFeature(FEATURE_NAME, ROLE)
        }
    }

    @Test
    fun `should allow granting same role multiple times without error`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // When
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
    }

    @Test
    fun `should allow revoking non-existent role without error`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.revokeRoleFromFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
    }
}
