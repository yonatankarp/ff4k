package com.yonatankarp.ff4k.property.multi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbstractPropertyMapTest {

    @Test
    fun `defaults are description null fixedValues empty and readOnly false`() {
        // Given
        val property = StringMapProperty(name = "map")

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
    fun `put inserts entry and returns previous value`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v1"))

        // When
        val previous = property.put("A", "v2")
        val current = property["A"]

        // Then
        assertEquals("v1", previous)
        assertEquals("v2", current)
        assertEquals(1, property.size)
    }

    @Test
    fun `putAll inserts all entries`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))
        val additional = mapOf("B" to "w", "C" to "x")

        // When
        property.putAll(additional)

        // Then
        assertEquals(3, property.size)
        assertEquals(setOf("A", "B", "C"), property.keys)
        assertEquals("w", property["B"])
        assertEquals("x", property["C"])
    }

    @Test
    fun `entries exposes backing map entries`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        val entries = property.entries.associate { it.key to it.value }

        // Then
        assertEquals(mapOf("A" to "v", "B" to "w"), entries)
    }

    @Test
    fun `values exposes backing map values`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        val values = property.values.toSet()

        // Then
        assertEquals(setOf("v", "w"), values)
    }

    @Test
    fun `size reflects backing map size`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        val size = property.size

        // Then
        assertEquals(2, size)
    }

    @Test
    fun `isEmpty reflects backing map`() {
        // Given
        val emptyProperty = StringMapProperty(name = "map", value = mutableMapOf())
        val nonEmptyProperty = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

        // When
        val empty = emptyProperty.isEmpty()
        val nonEmpty = nonEmptyProperty.isEmpty()

        // Then
        assertTrue(empty)
        assertFalse(nonEmpty)
    }

    @Test
    fun `containsKey returns true when key exists`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

        // When
        val containsA = property.containsKey("A")
        val containsB = property.containsKey("B")

        // Then
        assertTrue(containsA)
        assertFalse(containsB)
    }

    @Test
    fun `containsValue returns true when value exists`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        val containsV = property.containsValue("v")
        val containsMissing = property.containsValue("missing")

        // Then
        assertTrue(containsV)
        assertFalse(containsMissing)
    }

    @Test
    fun `get returns value for existing key and null for missing key`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

        // When
        val valueForA = property["A"]
        val valueForMissing = property["missing"]

        // Then
        assertEquals("v", valueForA)
        assertNull(valueForMissing)
    }

    @Test
    fun `remove removes entry and returns removed value`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        val removed = property.remove("A")

        // Then
        assertEquals("v", removed)
        assertFalse(property.containsKey("A"))
        assertEquals(1, property.size)
    }

    @Test
    fun `minusAssign removes entry by key`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        property -= "B"

        // Then
        assertFalse(property.containsKey("B"))
        assertEquals(1, property.size)
        assertEquals(setOf("A"), property.keys)
    }

    @Test
    fun `clear removes all entries`() {
        // Given
        val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

        // When
        property.clear()

        // Then
        assertTrue(property.isEmpty())
        assertEquals(0, property.size)
        assertEquals(emptySet(), property.keys)
    }

    private class StringMapProperty(
        name: String,
        value: MutableMap<String, String> = mutableMapOf(),
        description: String? = null,
        fixedValues: Set<MutableMap<String, String>> = emptySet(),
        readOnly: Boolean = false,
    ) : AbstractPropertyMap<String, MutableMap<String, String>>(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )
}
