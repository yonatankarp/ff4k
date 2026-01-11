@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Abstract contract test for [FeatureStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all FeatureStore implementations
 * must pass. It tests all CRUD operations, group management, permission handling, and edge cases.
 *
 * To use this contract test, extend this class and implement the [createStore] method:
 *
 * ```kotlin
 * class InMemoryFeatureStoreTest : FeatureStoreContractTest() {
 *     override suspend fun createStore(): FeatureStore = InMemoryFeatureStore()
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class FeatureStoreContractTest {

    /**
     * Create a fresh, empty FeatureStore instance for each test.
     *
     * Each test should get a clean store to ensure test isolation.
     * Implementations should return a new instance on each call.
     */
    protected abstract suspend fun createStore(): FeatureStore

    private fun createFeature(
        uid: String = FEATURE_NAME,
        isEnabled: Boolean = false,
    ) = Feature(uid = uid, isEnabled = isEnabled)

    @Test
    fun `should create a new feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)

        // When
        store += feature

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertEquals(FEATURE_NAME, retrieved.uid)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should throw exception when creating duplicate feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When / Then
        assertFailsWith<FeatureAlreadyExistsException> {
            store += feature
        }
    }

    @Test
    fun `should read feature by id`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)
        store += feature

        // When
        val retrieved = store[FEATURE_NAME]

        // Then
        assertNotNull(retrieved)
        assertEquals(FEATURE_NAME, retrieved.uid)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should return null when reading non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When
        val retrieved = store["non-existent"]

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `should read all features`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = true)
        store += createFeature(uid = "feature2", isEnabled = false)
        store += createFeature(uid = "feature3", isEnabled = true)

        // When
        val allFeatures = store.getAll()

        // Then
        assertEquals(3, allFeatures.size)
        assertTrue("feature1" in allFeatures)
        assertTrue("feature2" in allFeatures)
        assertTrue("feature3" in allFeatures)
    }

    @Test
    fun `should return empty map when no features exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val allFeatures = store.getAll()

        // Then
        assertTrue(allFeatures.isEmpty())
    }

    @Test
    fun `should update existing feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        val updated = feature.copy(isEnabled = true)
        store.update(updated)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should throw exception when updating non-existent feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.update(feature)
        }
    }

    @Test
    fun `should delete feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store -= FEATURE_NAME

        // Then
        assertNull(store[FEATURE_NAME])
    }

    @Test
    fun `should throw exception when deleting non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store -= FEATURE_NAME
        }
    }

    @Test
    fun `should check if feature exists using contains operator`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // Then
        assertTrue(FEATURE_NAME in store)
        assertFalse("non-existent" in store)
    }

    @Test
    fun `should enable a disabled feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store.enable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should disable an enabled feature`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)
        store += feature

        // When
        store.disable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertFalse(retrieved.isEnabled)
    }

    @Test
    fun `should throw exception when enabling non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.enable(FEATURE_NAME)
        }
    }

    @Test
    fun `should throw exception when disabling non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.disable(FEATURE_NAME)
        }
    }

    @Test
    fun `should allow enabling an already enabled feature without error`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        store.enable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should allow disabling an already disabled feature without error`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = false)

        // When
        store.disable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertFalse(retrieved.isEnabled)
    }

    @Test
    fun `should enable all features in a group`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = false)
        store += createFeature(uid = "feature2", isEnabled = false)
        store += createFeature(uid = "feature3", isEnabled = false)
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)

        // When
        store.enableGroup(GROUP_NAME)

        // Then
        assertTrue(store["feature1"]!!.isEnabled)
        assertTrue(store["feature2"]!!.isEnabled)
        assertFalse(store["feature3"]!!.isEnabled)
    }

    @Test
    fun `should disable all features in a group`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = true)
        store += createFeature(uid = "feature2", isEnabled = true)
        store += createFeature(uid = "feature3", isEnabled = true)
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)

        // When
        store.disableGroup(GROUP_NAME)

        // Then
        assertNotNull(store["feature1"])
        assertFalse(store["feature1"]!!.isEnabled)
        assertFalse(store["feature2"]!!.isEnabled)
        assertTrue(store["feature3"]!!.isEnabled)
    }

    @Test
    fun `should add feature to group`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        assertEquals(1, groupFeatures.size)
        assertTrue(FEATURE_NAME in groupFeatures)
    }

    @Test
    fun `should throw exception when adding non-existent feature to group`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.addToGroup(FEATURE_NAME, GROUP_NAME)
        }
    }

    @Test
    fun `should remove feature from group`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = FEATURE_NAME)
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.removeFromGroup(FEATURE_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        assertTrue(groupFeatures.isEmpty())
    }

    @Test
    fun `should throw exception when removing non-existent feature from group`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.removeFromGroup(FEATURE_NAME)
        }
    }

    @Test
    fun `should get all features in a group`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)
        store.addToGroup("feature3", ANOTHER_GROUP_NAME)

        // When
        val group1Features = store.getGroup(GROUP_NAME)

        // Then
        assertEquals(2, group1Features.size)
        assertTrue("feature1" in group1Features)
        assertTrue("feature2" in group1Features)
        assertFalse("feature3" in group1Features)
    }

    @Test
    fun `should return empty map for non-existent group`() = runTest {
        // Given
        val store = createStore()

        // When
        val groupFeatures = store.getGroup(GROUP_NAME)

        // Then
        assertTrue(groupFeatures.isEmpty())
    }

    @Test
    fun `should check if group exists`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        assertTrue(store.containsGroup(GROUP_NAME))
        assertFalse(store.containsGroup(ANOTHER_GROUP_NAME))
    }

    @Test
    fun `should get all group names`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)
        store.addToGroup("feature3", ANOTHER_GROUP_NAME)

        // When
        val groups = store.getAllGroups()

        // Then
        assertEquals(2, groups.size)
        assertTrue(GROUP_NAME in groups)
        assertTrue(ANOTHER_GROUP_NAME in groups)
    }

    @Test
    fun `should return empty set when no groups exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val groups = store.getAllGroups()

        // Then
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should allow adding feature to same group multiple times without error`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        assertEquals(1, groupFeatures.size)
        assertTrue(FEATURE_NAME in groupFeatures)
    }

    @Test
    fun `should move feature from one group to another`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.addToGroup(FEATURE_NAME, ANOTHER_GROUP_NAME)

        // Then
        val newGroupFeatures = store.getGroup(ANOTHER_GROUP_NAME)
        assertEquals(1, newGroupFeatures.size)
        assertTrue(FEATURE_NAME in newGroupFeatures)

        val oldGroupFeatures = store.getGroup(GROUP_NAME)
        assertTrue(oldGroupFeatures.isEmpty())
    }

    @Test
    fun `should clean up empty group after removing last feature`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.removeFromGroup(FEATURE_NAME)

        // Then
        assertFalse(store.containsGroup(GROUP_NAME))
        val groups = store.getAllGroups()
        assertFalse(GROUP_NAME in groups)
    }

    @Test
    fun `should clean up group when deleting last feature in group`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store -= FEATURE_NAME

        // Then
        assertFalse(store.containsGroup(GROUP_NAME))
        val groups = store.getAllGroups()
        assertFalse(GROUP_NAME in groups)
    }

    @Test
    fun `should clear groups along with features`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", ANOTHER_GROUP_NAME)

        // When
        store.clear()

        // Then
        val groups = store.getAllGroups()
        assertTrue(groups.isEmpty())
        assertFalse(store.containsGroup(GROUP_NAME))
        assertFalse(store.containsGroup(ANOTHER_GROUP_NAME))
    }

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

    @Test
    fun `should clear all features`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")

        // When
        store.clear()

        // Then
        val allFeatures = store.getAll()
        assertTrue(allFeatures.isEmpty())
    }

    @Test
    fun `should check if store is empty`() = runTest {
        // Given
        val store = createStore()

        // Then
        assertTrue(store.isEmpty())

        // When
        store += createFeature()

        // Then
        assertFalse(store.isEmpty())
    }

    @Test
    fun `should return count 0 when store is empty`() = runTest {
        // Given
        val store = createStore()

        // When
        val result = store.count()

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `should count features in store`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")

        // When
        val result = store.count()

        // Then
        assertEquals(3, result)
    }

    @Test
    fun `should update feature using transform function`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.updateFeature(FEATURE_NAME) { feature ->
            feature.copy(isEnabled = true)
        }

        // Then
        val updated = store[FEATURE_NAME]
        assertNotNull(updated)
        assertTrue(updated.isEnabled)
    }

    @Test
    fun `should throw exception when updating non-existent feature with transform`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.updateFeature(FEATURE_NAME) { it }
        }
    }

    @Test
    fun `should create or update feature - create path`() = runTest {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)

        // When
        store.createOrUpdate(feature)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should create or update feature - update path`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val updated = createFeature(isEnabled = true)
        store.createOrUpdate(updated)

        // Then
        val retrieved = store[FEATURE_NAME]
        assertNotNull(retrieved)
        assertTrue(retrieved.isEnabled)
    }

    @Test
    fun `should toggle feature from disabled to enabled`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.toggle(FEATURE_NAME)

        // Then
        val toggled = store[FEATURE_NAME]
        assertNotNull(toggled)
        assertTrue(toggled.isEnabled)
    }

    @Test
    fun `should toggle feature from enabled to disabled`() = runTest {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        store.toggle(FEATURE_NAME)

        // Then
        val toggled = store[FEATURE_NAME]
        assertNotNull(toggled)
        assertFalse(toggled.isEnabled)
    }

    @Test
    fun `should throw exception when toggling non-existent feature`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.toggle(FEATURE_NAME)
        }
    }

    @Test
    fun `should get feature or throw exception`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val feature = store.getOrThrow(FEATURE_NAME)

        // Then
        assertNotNull(feature)
        assertEquals(FEATURE_NAME, feature.uid)
    }

    @Test
    fun `should throw exception when getting non-existent feature with getOrThrow`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<FeatureNotFoundException> {
            store.getOrThrow(FEATURE_NAME)
        }
    }

    companion object {
        private const val FEATURE_NAME = "feature"
        private const val GROUP_NAME = "group"
        private const val ANOTHER_GROUP_NAME = "anotherGroup"
        private const val ROLE = "admin"
    }
}
