package com.yonatankarp.ff4k.property.multi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbstractPropertySetTest {

    @Test
    fun `defaults are description null fixedValues empty and readOnly false`() {
        // Given
        val property = StringSetProperty(name = "set")

        // When
        val description = property.description
        val fixedValues = property.fixedValues
        val readOnly = property.readOnly

        // Then
        assertNull(description)
        assertEquals(emptySet(), fixedValues)
        assertFalse(readOnly)
    }


    @Test
    fun `add inserts element and increases size`() {
        // Given
        val property = StringSetProperty(name = "set")

        // When
        val changed = property.add("a")

        // Then
        assertTrue(changed)
        assertEquals(1, property.size)
        assertTrue(property.contains("a"))
    }

    @Test
    fun `add does not add duplicates`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a"))

        // When
        val firstAddChanged = property.add("a")
        val sizeAfterAdd = property.size

        // Then
        assertFalse(firstAddChanged)
        assertEquals(1, sizeAfterAdd)
    }

    @Test
    fun `plusAssign adds element`() {
        // Given
        val property = StringSetProperty(name = "set")

        // When
        property += "a"

        // Then
        assertTrue(property.contains("a"))
        assertEquals(1, property.size)
    }

    @Test
    fun `remove removes existing element`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

        // When
        val changed = property.remove("a")

        // Then
        assertTrue(changed)
        assertFalse(property.contains("a"))
        assertEquals(setOf("b"), property.value)
    }

    @Test
    fun `minusAssign removes element`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

        // When
        property -= "b"

        // Then
        assertFalse(property.contains("b"))
        assertEquals(setOf("a"), property.value)
    }

    @Test
    fun `addAll adds all new elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a"))
        val elements = listOf("b", "c")

        // When
        val changed = property.addAll(elements)

        // Then
        assertTrue(changed)
        assertEquals(setOf("a", "b", "c"), property.value)
    }

    @Test
    fun `addAll vararg adds all new elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a"))

        // When
        property.addAll("b", "c")

        // Then
        assertEquals(setOf("a", "b", "c"), property.value)
    }

    @Test
    fun `containsAll returns true when all elements present`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
        val required = listOf("a", "c")

        // When
        val result = property.containsAll(required)

        // Then
        assertTrue(result)
    }

    @Test
    fun `removeAll removes provided elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
        val elementsToRemove = listOf("b", "c")

        // When
        val changed = property.removeAll(elementsToRemove)

        // Then
        assertTrue(changed)
        assertEquals(setOf("a"), property.value)
    }

    @Test
    fun `retainAll keeps only provided elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
        val elementsToKeep = listOf("a", "c")

        // When
        val changed = property.retainAll(elementsToKeep)

        // Then
        assertTrue(changed)
        assertEquals(setOf("a", "c"), property.value)
    }

    @Test
    fun `iterator iterates over all elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))

        // When
        val iterated = mutableSetOf<String>()
        val iterator = property.iterator()
        while (iterator.hasNext()) {
            iterated.add(iterator.next())
        }

        // Then
        assertEquals(setOf("a", "b", "c"), iterated)
    }

    @Test
    fun `clear removes all elements`() {
        // Given
        val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

        // When
        property.clear()

        // Then
        assertTrue(property.isEmpty())
        assertEquals(0, property.size)
        assertEquals(emptySet(), property.value)
    }

    private class StringSetProperty(
        name: String,
        value: MutableSet<String> = mutableSetOf(),
        description: String? = null,
        fixedValues: MutableSet<String> = mutableSetOf(),
        readOnly: Boolean = false,
    ) : AbstractPropertySet<String, MutableSet<String>>(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )
}
