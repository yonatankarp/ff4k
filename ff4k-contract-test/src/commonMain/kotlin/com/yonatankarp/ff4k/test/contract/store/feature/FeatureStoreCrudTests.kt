@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreTestSupport.Companion.FEATURE_NAME
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal interface FeatureStoreCrudTests : FeatureStoreTestSupport {

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
    fun `should throw exception when transform changes feature uid`() = runTest {
        // Given
        val store = createStore()
        store += createFeature()

        // When / Then
        assertFailsWith<IllegalStateException> {
            store.updateFeature(FEATURE_NAME) { feature ->
                feature.copy(uid = "different-id")
            }
        }

        // Verify
        assertNotNull(store[FEATURE_NAME])
        assertNull(store["different-id"])
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
}
