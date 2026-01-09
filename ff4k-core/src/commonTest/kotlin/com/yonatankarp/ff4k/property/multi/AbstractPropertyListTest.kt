package com.yonatankarp.ff4k.property.multi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AbstractPropertyListTest {

    @Test
    fun `addAll at index inserts elements at the right position`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "d"),
        )
        val elementsToInsert = listOf("b", "c")

        // When
        val changed = property.addAll(1, elementsToInsert)

        // Then
        assertTrue(changed)
        assertEquals(listOf("a", "b", "c", "d"), property)
    }

    @Test
    fun `get returns element at index`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c"),
        )

        // When
        val element = property[1]

        // Then
        assertEquals("b", element)
    }

    @Test
    fun `set updates element at index and returns the element`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c"),
        )

        // When
        val returned = property.set(1, "x")

        // Then
        assertEquals("x", returned)
        assertEquals(listOf("a", "x", "c"), property)
    }

    @Test
    fun `add at index inserts element`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "c"),
        )

        // When
        property.add(1, "b")

        // Then
        assertEquals(listOf("a", "b", "c"), property)
    }

    @Test
    fun `removeAt removes and returns element`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c"),
        )

        // When
        val removed = property.removeAt(1)

        // Then
        assertEquals("b", removed)
        assertEquals(listOf("a", "c"), property)
    }

    @Test
    fun `indexOf returns first index of element`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "a"),
        )

        // When
        val index = property.indexOf("a")

        // Then
        assertEquals(0, index)
    }

    @Test
    fun `lastIndexOf returns last index of element`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "a"),
        )

        // When
        val index = property.lastIndexOf("a")

        // Then
        assertEquals(2, index)
    }

    @Test
    fun `listIterator iterates over current elements in order`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c"),
        )

        // When
        val iterated = buildList {
            val iterator = property.listIterator()
            while (iterator.hasNext()) add(iterator.next())
        }

        // Then
        assertEquals(listOf("a", "b", "c"), iterated)
    }

    @Test
    fun `subList returns correct slice`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c", "d"),
        )

        // When
        val slice = property.subList(1, 3)

        // Then
        assertEquals(listOf("b", "c"), slice)
    }

    @Test
    fun `listIterator with index starts iteration at the given position`() {
        // Given
        val property = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b", "c", "d"),
        )
        val startIndex = 2

        // When
        val iterator = property.listIterator(startIndex)
        val iterated = buildList {
            while (iterator.hasNext()) add(iterator.next())
        }

        // Then
        assertEquals(listOf("c", "d"), iterated)
        assertTrue(property.listIterator(0).hasNext())
    }

    @Test
    fun `list properties are equal and have same hashCode when all fields match`() {
        // Given
        val propertyName = "list"
        val value = mutableListOf("a", "b")
        val description = "desc"
        val fixedValues = mutableSetOf(mutableListOf("a", "b"))
        val readOnly = true

        val first = StringListProperty(propertyName, value.toMutableList(), description, fixedValues, readOnly)
        val second = StringListProperty(propertyName, value.toMutableList(), description, fixedValues, readOnly)

        // When
        val equals = first == second
        val hashEquals = first.hashCode() == second.hashCode()

        // Then
        assertEquals(true, equals)
        assertEquals(true, hashEquals)
    }

    @Test
    fun `list properties are not equal when any field differs`() {
        // Given
        val base = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b"),
            description = "desc",
            fixedValues = mutableSetOf(mutableListOf("a", "b")),
            readOnly = true,
        )

        val differentName = StringListProperty(
            name = "other",
            value = mutableListOf("a", "b"),
            description = "desc",
            fixedValues = mutableSetOf(mutableListOf("a", "b")),
            readOnly = true,
        )

        val differentValue = StringListProperty(
            name = "list",
            value = mutableListOf("a", "c"),
            description = "desc",
            fixedValues = mutableSetOf(mutableListOf("a", "c")),
            readOnly = true,
        )

        val differentDescription = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b"),
            description = "different",
            fixedValues = mutableSetOf(mutableListOf("a", "b")),
            readOnly = true,
        )

        val differentFixedValues = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b"),
            description = "desc",
            fixedValues = mutableSetOf(mutableListOf("a", "b"), mutableListOf("a", "c")),
            readOnly = true,
        )

        val differentReadOnly = StringListProperty(
            name = "list",
            value = mutableListOf("a", "b"),
            description = "desc",
            fixedValues = mutableSetOf(mutableListOf("a", "b")),
            readOnly = false,
        )

        // When / Then
        assertNotEquals(base, differentName)
        assertNotEquals(base, differentValue)
        assertNotEquals(base, differentDescription)
        assertNotEquals(base, differentFixedValues)
        assertNotEquals(base, differentReadOnly)
    }

    private class StringListProperty(
        name: String,
        value: MutableList<String> = mutableListOf(),
        description: String? = null,
        fixedValues: MutableSet<MutableList<String>> = mutableSetOf(),
        readOnly: Boolean = false,
    ) : AbstractPropertyList<String>(name, value, description, fixedValues, readOnly)
}
