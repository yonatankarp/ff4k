package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.config.FF4kConfiguration
import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.property.utils.property
import com.yonatankarp.ff4k.test.contract.store.PropertyStoreContractTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()

    @Test
    fun `test store initialized with property map contains properties`() = runTest {
        // Given
        val initialProperties = mapOf(
            "prop-1" to property("prop-1", "value1"),
            "prop-2" to property("prop-2", 42),
            "prop-3" to property("prop-3", true),
        )

        // When
        val store = InMemoryPropertyStore(initialProperties)

        // Then
        assertEquals(3, store.getAll().size)
        assertTrue(store.contains("prop-1"))
        assertTrue(store.contains("prop-2"))
        assertTrue(store.contains("prop-3"))
        assertEquals("value1", store.get<String>("prop-1")?.value)
        assertEquals(42, store.get<Int>("prop-2")?.value)
        assertEquals(true, store.get<Boolean>("prop-3")?.value)
    }

    @Test
    fun `test store initialized with FF4kConfiguration contains properties`() = runTest {
        // Given
        val config = FF4kConfiguration(
            properties = mapOf(
                "config-prop-1" to property("config-prop-1", "configValue"),
                "config-prop-2" to property("config-prop-2", 100),
            ),
        )

        // When
        val store = InMemoryPropertyStore(config)

        // Then
        assertEquals(2, store.getAll().size)
        assertTrue(store.contains("config-prop-1"))
        assertTrue(store.contains("config-prop-2"))
        assertEquals("configValue", store.get<String>("config-prop-1")?.value)
        assertEquals(100, store.get<Int>("config-prop-2")?.value)
    }

    @Test
    fun `test store initialized with empty configuration is empty`() = runTest {
        // Given
        val config = FF4kConfiguration()

        // When
        val store = InMemoryPropertyStore(config)

        // Then
        assertTrue(store.isEmpty())
        assertEquals(0, store.getAll().size)
    }
}
