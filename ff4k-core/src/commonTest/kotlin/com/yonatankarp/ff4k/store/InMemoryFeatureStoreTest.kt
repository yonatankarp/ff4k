package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.config.FF4kConfiguration
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InMemoryFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore() = InMemoryFeatureStore()

    @Test
    fun `test store initialized with feature map contains features`() = runTest {
        // Given
        val initialFeatures = mapOf(
            "feature-1" to Feature("feature-1", isEnabled = true),
            "feature-2" to Feature("feature-2", isEnabled = false),
            "feature-3" to Feature("feature-3", isEnabled = true),
        )

        // When
        val store = InMemoryFeatureStore(initialFeatures)

        // Then
        assertEquals(3, store.count())
        assertTrue("feature-1" in store)
        assertTrue("feature-2" in store)
        assertTrue("feature-3" in store)
        assertEquals(true, store["feature-1"]?.isEnabled)
        assertEquals(false, store["feature-2"]?.isEnabled)
        assertEquals(true, store["feature-3"]?.isEnabled)
    }

    @Test
    fun `test store initialized with FF4kConfiguration contains features`() = runTest {
        // Given
        val config = FF4kConfiguration(
            features = mapOf(
                "config-feature-1" to Feature("config-feature-1", isEnabled = true),
                "config-feature-2" to Feature("config-feature-2", isEnabled = false),
            ),
        )

        // When
        val store = InMemoryFeatureStore(config)

        // Then
        assertEquals(2, store.count())
        assertTrue("config-feature-1" in store)
        assertTrue("config-feature-2" in store)
        assertEquals(true, store["config-feature-1"]?.isEnabled)
        assertEquals(false, store["config-feature-2"]?.isEnabled)
    }

    @Test
    fun `test store initialized with empty configuration is empty`() = runTest {
        // Given
        val config = FF4kConfiguration()

        // When
        val store = InMemoryFeatureStore(config)

        // Then
        assertTrue(store.isEmpty())
        assertEquals(0, store.count())
    }

    @Test
    fun `test concurrent writes are not creating race condition`() = runTest {
        // Given
        val store = InMemoryFeatureStore()

        // When
        val jobs = (1..100).map { i ->
            launch {
                store += Feature("feature-$i", isEnabled = true)
            }
        }
        jobs.joinAll()

        // Then
        assertEquals(100, store.count())
    }

    @Test
    fun `test concurrent reads and writes do not cause deadlock or corruption`() = runTest {
        // Given
        val store = InMemoryFeatureStore()
        store += Feature("test", isEnabled = false)

        // When
        val readJobs = (1..50).map {
            launch {
                repeat(100) {
                    store["test"]
                }
            }
        }

        val writeJobs = (1..50).map {
            launch {
                repeat(100) {
                    store.toggle("test")
                }
            }
        }

        (readJobs + writeJobs).joinAll()

        // Then
        assertTrue("test" in store)
        assertNotNull(store["test"])
    }
}
