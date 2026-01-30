@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.exception.GroupNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.ANOTHER_GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.GROUP_NAME
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal interface FeatureStoreGroupTests : FeatureStoreTestSupport {

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
        store.removeFromGroup(FEATURE_NAME, GROUP_NAME)

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
            store.removeFromGroup(FEATURE_NAME, GROUP_NAME)
        }
    }

    @Test
    fun `should throw exception when removing group the feature is not in`() = runTest {
        // Given
        val store = createStore()
        store += feature(uid = FEATURE_NAME) {
            group = GROUP_NAME
        }

        // When / Then
        assertFailsWith<GroupNotFoundException> {
            store.removeFromGroup(FEATURE_NAME, ANOTHER_GROUP_NAME)
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
        store.removeFromGroup(FEATURE_NAME, GROUP_NAME)

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
}
