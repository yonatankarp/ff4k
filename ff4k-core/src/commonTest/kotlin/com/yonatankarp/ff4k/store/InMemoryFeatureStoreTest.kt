package com.yonatankarp.ff4k.store

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
