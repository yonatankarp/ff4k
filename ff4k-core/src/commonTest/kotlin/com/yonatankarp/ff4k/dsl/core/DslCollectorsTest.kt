package com.yonatankarp.ff4k.dsl.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for SetCollector and ListCollector DSL base classes.
 *
 * @author Yonatan Karp-Rudin
 */
class DslCollectorsTest {

    @Test
    fun `SetCollector builds empty set when no values added`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.build()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SetCollector adds value using unary plus operator`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.apply {
            +"value1"
        }.build()

        // Then
        assertEquals(setOf("value1"), result)
    }

    @Test
    fun `SetCollector adds multiple values using unary plus operator`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.apply {
            +"value1"
            +"value2"
            +"value3"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `SetCollector adds value using add method`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.apply {
            add("value1")
        }.build()

        // Then
        assertEquals(setOf("value1"), result)
    }

    @Test
    fun `SetCollector removes duplicates automatically`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.apply {
            +"value1"
            +"value2"
            +"value1"
            +"value3"
            +"value2"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `SetCollector works with Int values`() {
        // Given
        val collector = TestSetCollector<Int>()

        // When
        val result = collector.apply {
            add(1)
            add(2)
            add(3)
        }.build()

        // Then
        assertEquals(setOf(1, 2, 3), result)
    }

    @Test
    fun `SetCollector works with Boolean values`() {
        // Given
        val collector = TestSetCollector<Boolean>()

        // When
        val result = collector.apply {
            +true
            +false
        }.build()

        // Then
        assertEquals(setOf(true, false), result)
    }

    @Test
    fun `SetCollector combines unary plus and add methods`() {
        // Given
        val collector = TestSetCollector<String>()

        // When
        val result = collector.apply {
            +"value1"
            add("value2")
            +"value3"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `ListCollector builds empty list when no items added`() {
        // Given
        val collector = TestListCollector<String>()

        // When
        val result = collector.build()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `ListCollector adds item using unary plus operator`() {
        // Given
        val collector = TestListCollector<String>()

        // When
        val result = collector.apply {
            +"item1"
        }.build()

        // Then
        assertEquals(listOf("item1"), result)
    }

    @Test
    fun `ListCollector adds multiple items using unary plus operator`() {
        // Given
        val collector = TestListCollector<String>()

        // When
        val result = collector.apply {
            +"item1"
            +"item2"
            +"item3"
        }.build()

        // Then
        assertEquals(listOf("item1", "item2", "item3"), result)
    }

    @Test
    fun `ListCollector preserves insertion order`() {
        // Given
        val collector = TestListCollector<String>()

        // When
        val result = collector.apply {
            +"third"
            +"first"
            +"second"
        }.build()

        // Then
        assertEquals(listOf("third", "first", "second"), result)
    }

    @Test
    fun `ListCollector allows duplicates`() {
        // Given
        val collector = TestListCollector<String>()

        // When
        val result = collector.apply {
            +"item1"
            +"item2"
            +"item1"
            +"item3"
            +"item1"
        }.build()

        // Then
        assertEquals(listOf("item1", "item2", "item1", "item3", "item1"), result)
    }

    @Test
    fun `ListCollector works with Int values`() {
        // Given
        val collector = TestListCollector<Int>()

        // When
        val result = collector.apply {
            add(1)
            add(2)
            add(3)
        }.build()

        // Then
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `ListCollector works with custom objects`() {
        // Given
        data class Item(val id: Int, val name: String)
        val item1 = Item(1, "first")
        val item2 = Item(2, "second")
        val collector = TestListCollector<Item>()

        // When
        val result = collector.apply {
            +item1
            +item2
        }.build()

        // Then
        assertEquals(listOf(item1, item2), result)
    }

    /**
     * Concrete implementation of SetCollector for testing.
     */
    private class TestSetCollector<T> : SetCollector<T>()

    /**
     * Concrete implementation of ListCollector for testing.
     */
    private class TestListCollector<T> : ListCollector<T>()
}
