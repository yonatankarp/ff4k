@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.property

import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.core.createOrUpdateProperty
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreTestSupport.Companion.PROPERTY_NAME
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal interface PropertyStoreConcurrencyTests : PropertyStoreTestSupport {

    @Test
    fun `concurrent property updates via atomic method should be atomic`() = runTest {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 0)

        // When
        val jobs = (1..100).map {
            launch {
                store.updateProperty<Int>(PROPERTY_NAME) { property ->
                    PropertyInt(
                        name = PROPERTY_NAME,
                        value = property.value + 1,
                    )
                }
            }
        }
        jobs.joinAll()

        // Then
        val finalValue = store.get<Int>(PROPERTY_NAME)?.value
        assertNotNull(finalValue)
        assertEquals(100, finalValue)
    }

    @Test
    fun `concurrent createOrUpdateProperty calls should handle race conditions`() = runTest {
        // Given
        val store = createStore()

        // When
        val jobs = (1..100).map { i ->
            launch {
                store.createOrUpdateProperty(
                    PropertyInt(
                        name = PROPERTY_NAME,
                        value = i,
                    ),
                )
            }
        }
        jobs.joinAll()

        // Then
        val property = store.get<Int>(PROPERTY_NAME)
        assertNotNull(property)
        assertTrue(property.value in 1..100)
    }

    @Test
    fun `concurrent property deletions should handle missing properties gracefully`() = runTest {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += PropertyInt(name = "prop-$i", value = i)
        }

        // When
        val jobs = (1..10).map { i ->
            launch {
                try {
                    store -= "prop-$i"
                } catch (_: PropertyNotFoundException) {
                    // Ignore - another coroutine might have deleted it
                }
            }
        }
        jobs.joinAll()

        // Then
        assertEquals(0, store.count())
    }

    @Test
    fun `concurrent set operations should handle updates correctly`() = runTest {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 0)

        // When
        val jobs = (1..50).map { i ->
            launch {
                store.updateProperty(PROPERTY_NAME) { property ->
                    PropertyInt(name = property.name, value = i)
                }
            }
        }
        jobs.joinAll()

        // Then
        val finalValue = store.get<Int>(PROPERTY_NAME)?.value
        assertNotNull(finalValue)
        assertTrue(finalValue in 1..50)
    }
}
