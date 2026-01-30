@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.ANOTHER_GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.GROUP_NAME
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal interface FeatureStoreConcurrencyTests : FeatureStoreTestSupport {

    @Test
    fun `concurrent enable and grant permissions should not lose changes`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val enableJobs = (1..50).map {
            launch {
                store.enable(FEATURE_NAME)
            }
        }

        val grantJobs = (1..50).map { i ->
            launch {
                store.grantRoleToFeature(FEATURE_NAME, "role-$i")
            }
        }

        (enableJobs + grantJobs).joinAll()

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertTrue(feature.isEnabled)
        assertEquals(50, feature.permissions.size)
    }

    @Test
    fun `concurrent disable and grant permissions should not lose changes`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        val disableJobs = (1..50).map {
            launch {
                store.disable(FEATURE_NAME)
            }
        }

        val grantJobs = (1..50).map { i ->
            launch {
                store.grantRoleToFeature(FEATURE_NAME, "role-$i")
            }
        }

        (disableJobs + grantJobs).joinAll()

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertFalse(feature.isEnabled)
        assertEquals(50, feature.permissions.size)
    }

    @Test
    fun `concurrent grant permissions and enable should not lose changes`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val grantJobs = (1..50).map { i ->
            launch {
                store.grantRoleToFeature(FEATURE_NAME, "role-$i")
            }
        }

        val enableJobs = (1..50).map {
            launch {
                store.enable(FEATURE_NAME)
            }
        }

        (grantJobs + enableJobs).joinAll()

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertTrue(feature.isEnabled)
        assertEquals(50, feature.permissions.size)
    }

    @Test
    fun `concurrent addToGroup and enable should not lose changes`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val addGroupJobs = (1..50).map {
            launch {
                store.addToGroup(FEATURE_NAME, GROUP_NAME)
            }
        }

        val enableJobs = (1..50).map {
            launch {
                store.enable(FEATURE_NAME)
            }
        }

        (addGroupJobs + enableJobs).joinAll()

        // Then
        val feature = store[FEATURE_NAME]
        assertNotNull(feature)
        assertTrue(feature.isEnabled)
        assertEquals(GROUP_NAME, feature.group)
    }

    @Test
    fun `enableGroup should handle concurrent feature deletions gracefully`() = runTest {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = false, group = GROUP_NAME)
        }

        // When
        val enableJob = launch {
            store.enableGroup(GROUP_NAME)
        }

        val deleteJobs = (1..5).map { i ->
            launch {
                store -= "feature-$i"
            }
        }

        (listOf(enableJob) + deleteJobs).joinAll()

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            assertNotNull(feature, "feature-$i should still exist")
            assertTrue(feature.isEnabled, "feature-$i should be enabled")
        }
        assertEquals(5, store.count())
    }

    @Test
    fun `enableGroup should not enable features that left the group concurrently`() = runTest {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = false, group = GROUP_NAME)
        }

        // When
        val enableJob = launch {
            repeat(5) {
                store.enableGroup(GROUP_NAME)
            }
        }

        val moveJobs = (1..5).map { i ->
            launch {
                store.addToGroup("feature-$i", ANOTHER_GROUP_NAME)
            }
        }

        (listOf(enableJob) + moveJobs).joinAll()

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            assertNotNull(feature)
            assertEquals(GROUP_NAME, feature.group)
            assertTrue(feature.isEnabled)
        }
    }

    @Test
    fun `disableGroup should handle concurrent feature deletions gracefully`() = runTest {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = true, group = GROUP_NAME)
        }

        // When
        val disableJob = launch {
            store.disableGroup(GROUP_NAME)
        }

        val deleteJobs = (1..5).map { i ->
            launch {
                store -= "feature-$i"
            }
        }

        (listOf(disableJob) + deleteJobs).joinAll()

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            assertNotNull(feature, "feature-$i should still exist")
            assertFalse(feature.isEnabled, "feature-$i should be disabled")
        }
        assertEquals(5, store.count())
    }
}
