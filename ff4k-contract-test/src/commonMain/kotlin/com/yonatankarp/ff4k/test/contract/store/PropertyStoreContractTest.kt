@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.core.createOrUpdateProperty
import com.yonatankarp.ff4k.core.getPropertyOrThrow
import com.yonatankarp.ff4k.core.getPropertyValue
import com.yonatankarp.ff4k.core.getPropertyValueOrDefault
import com.yonatankarp.ff4k.dsl.intProperty
import com.yonatankarp.ff4k.dsl.stringProperty
import com.yonatankarp.ff4k.exception.PropertyAlreadyExistsException
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Abstract contract test for [PropertyStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all PropertyStore implementations
 * must pass. It tests all CRUD operations, operators, extension functions, and edge cases.
 *
 * To use this contract test, extend this class and implement the [createStore] method:
 *
 * ```kotlin
 * class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
 *     override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class PropertyStoreContractTest {

    /**
     * Create a fresh, empty PropertyStore instance for each test.
     *
     * Each test should get a clean store to ensure test isolation.
     * Implementations should return a new instance on each call.
     */
    protected abstract suspend fun createStore(): PropertyStore

    @Test
    fun `should create a new property`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }

        // When
        store += property

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        assertNotNull(retrieved)
        assertEquals(PROPERTY_NAME, retrieved.name)
        assertEquals(DEFAULT_VALUE, retrieved.value)
    }

    @Test
    fun `should throw exception when creating duplicate property`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }
        store += property

        // When / Then
        assertFailsWith<PropertyAlreadyExistsException> {
            store += property
        }
    }

    @Test
    fun `should create multiple properties with different names`() = runTest {
        // Given
        val store = createStore()

        // When
        store += intProperty("prop1") { value = 1 }
        store += intProperty("prop2") { value = 2 }
        store += intProperty("prop3") { value = 3 }

        // Then
        assertEquals(3, store.getAll().size)
        assertEquals(1, store.get<Int>("prop1")?.value)
        assertEquals(2, store.get<Int>("prop2")?.value)
        assertEquals(3, store.get<Int>("prop3")?.value)
    }

    @Test
    fun `should read property by name`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = 42 }
        store += property

        // When
        val retrieved = store.get<Int>(PROPERTY_NAME)

        // Then
        assertNotNull(retrieved)
        assertEquals(PROPERTY_NAME, retrieved.name)
        assertEquals(42, retrieved.value)
    }

    @Test
    fun `should return null when reading non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When
        val retrieved = store.get<Int>("non-existent")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `should read all properties`() = runTest {
        // Given
        val store = createStore()
        store += intProperty("prop1") { value = 1 }
        store += intProperty("prop2") { value = 2 }
        store += stringProperty("prop3") { value = "test" }

        // When
        val allProperties = store.getAll()

        // Then
        assertEquals(3, allProperties.size)
        assertTrue("prop1" in allProperties)
        assertTrue("prop2" in allProperties)
        assertTrue("prop3" in allProperties)
    }

    @Test
    fun `should return empty map when no properties exist`() = runTest {
        // Given
        val store = createStore()

        // When
        val allProperties = store.getAll()

        // Then
        assertTrue(allProperties.isEmpty())
    }

    @Test
    fun `should get property or default when property exists`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 42 }
        val defaultProperty = intProperty(PROPERTY_NAME) { value = 99 }

        // When
        val result = store.getOrDefault(PROPERTY_NAME, defaultProperty)

        // Then
        assertEquals(42, result.value)
    }

    @Test
    fun `should get default when property does not exist`() = runTest {
        // Given
        val store = createStore()
        val defaultProperty = intProperty(PROPERTY_NAME) { value = 99 }

        // When
        val result = store.getOrDefault(PROPERTY_NAME, defaultProperty)

        // Then
        assertEquals(99, result.value)
    }

    @Test
    fun `should update existing property`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = 10 }
        store += property

        // When
        val updated = intProperty(PROPERTY_NAME) { value = 20 }
        store.updateProperty(updated)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        assertNotNull(retrieved)
        assertEquals(20, retrieved.value)
    }

    @Test
    fun `should update property using transform function`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 10 }

        // When
        store.updateProperty<Int>(PROPERTY_NAME) { property ->
            intProperty(property.name) {
                value = property.value * 2
                description = property.description
            }
        }

        // Then
        val updated = store.get<Int>(PROPERTY_NAME)
        assertNotNull(updated)
        assertEquals(20, updated.value)
    }

    @Test
    fun `should throw exception when updating non-existent property with transform`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<PropertyNotFoundException> {
            store.updateProperty<Int>(PROPERTY_NAME) { it }
        }
    }

    @Test
    fun `should delete property`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }
        store += property

        // When
        store -= PROPERTY_NAME

        // Then
        assertNull(store.get<Int>(PROPERTY_NAME))
    }

    @Test
    fun `should throw exception when deleting non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<PropertyNotFoundException> {
            store -= PROPERTY_NAME
        }
    }

    @Test
    fun `should check if property exists using contains operator`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }
        store += property

        // Then
        assertTrue(PROPERTY_NAME in store)
        assertFalse("non-existent" in store)
    }

    @Test
    fun `should list property names`() = runTest {
        // Given
        val store = createStore()
        store += intProperty("prop1") { value = 1 }
        store += intProperty("prop2") { value = 2 }
        store += intProperty("prop3") { value = 3 }

        // When
        val names = store.listPropertyIds()

        // Then
        assertEquals(3, names.size)
        assertTrue("prop1" in names)
        assertTrue("prop2" in names)
        assertTrue("prop3" in names)
    }

    @Test
    fun `should return empty set when no properties exist for listPropertyNames`() = runTest {
        // Given
        val store = createStore()

        // When
        val names = store.listPropertyIds()

        // Then
        assertTrue(names.isEmpty())
    }

    @Test
    fun `should clear all properties`() = runTest {
        // Given
        val store = createStore()
        store += intProperty("prop1") { value = 1 }
        store += intProperty("prop2") { value = 2 }
        store += intProperty("prop3") { value = 3 }

        // When
        store.clear()

        // Then
        val allProperties = store.getAll()
        assertTrue(allProperties.isEmpty())
    }

    @Test
    fun `should check if store is empty using extension function`() = runTest {
        // Given
        val store = createStore()

        // Then
        assertTrue(store.isEmpty())

        // When
        store += intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }

        // Then
        assertFalse(store.isEmpty())
    }

    @Test
    fun `isEmpty property should return true for empty store`() = runTest {
        // Given
        val store = createStore()

        // Then
        assertTrue(store.isEmpty())
    }

    @Test
    fun `isEmpty property should return false for non-empty store`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }

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
    fun `should count properties in store`() = runTest {
        // Given
        val store = createStore()
        store += intProperty("prop1") { value = 1 }
        store += intProperty("prop2") { value = 2 }
        store += intProperty("prop3") { value = 3 }

        // When
        val result = store.count()

        // Then
        assertEquals(3, result)
    }

    @Test
    fun `should create or update property - create path`() = runTest {
        // Given
        val store = createStore()
        val property = intProperty(PROPERTY_NAME) { value = 42 }

        // When
        store.createOrUpdateProperty(property)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        assertNotNull(retrieved)
        assertEquals(42, retrieved.value)
    }

    @Test
    fun `should create or update property - update path`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 10 }

        // When
        val updated = intProperty(PROPERTY_NAME) { value = 42 }
        store.createOrUpdateProperty(updated)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        assertNotNull(retrieved)
        assertEquals(42, retrieved.value)
    }

    @Test
    fun `should get property or throw exception`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = DEFAULT_VALUE }

        // When
        val property = store.getPropertyOrThrow<Int>(PROPERTY_NAME)

        // Then
        assertNotNull(property)
        assertEquals(PROPERTY_NAME, property.name)
    }

    @Test
    fun `should throw exception when getting non-existent property with getPropertyOrThrow`() = runTest {
        // Given
        val store = createStore()

        // When / Then
        assertFailsWith<PropertyNotFoundException> {
            store.getPropertyOrThrow<Int>(PROPERTY_NAME)
        }
    }

    @Test
    fun `should get property value directly`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 42 }

        // When
        val value = store.getPropertyValue<Int>(PROPERTY_NAME)

        // Then
        assertEquals(42, value)
    }

    @Test
    fun `should return null when getting value of non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When
        val value = store.getPropertyValue<Int>(PROPERTY_NAME)

        // Then
        assertNull(value)
    }

    @Test
    fun `should get property value or default`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 42 }

        // When
        val value = store.getPropertyValueOrDefault(PROPERTY_NAME, 99)

        // Then
        assertEquals(42, value)
    }

    @Test
    fun `should return default when getting value of non-existent property`() = runTest {
        // Given
        val store = createStore()

        // When
        val value = store.getPropertyValueOrDefault(PROPERTY_NAME, 99)

        // Then
        assertEquals(99, value)
    }

    @Test
    fun `concurrent property updates via atomic method should be atomic`() = runTest {
        // Given
        val store = createStore()
        store += intProperty(PROPERTY_NAME) { value = 0 }

        // When
        val jobs = (1..100).map {
            launch {
                store.updateProperty<Int>(PROPERTY_NAME) { property ->
                    intProperty(property.name) {
                        value = property.value + 1
                    }
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
                store.createOrUpdateProperty(intProperty(PROPERTY_NAME) { value = i })
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
            store += intProperty("prop-$i") { value = i }
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
        store += intProperty(PROPERTY_NAME) { value = 0 }

        // When
        val jobs = (1..50).map { i ->
            launch {
                store.updateProperty(PROPERTY_NAME) { property ->
                    intProperty(property.name) {
                        value = i
                    }
                }
            }
        }
        jobs.joinAll()

        // Then
        val finalValue = store.get<Int>(PROPERTY_NAME)?.value
        assertNotNull(finalValue)
        assertTrue(finalValue in 1..50)
    }

    companion object {
        private const val PROPERTY_NAME = "testProperty"
        private const val DEFAULT_VALUE = 42
    }
}
