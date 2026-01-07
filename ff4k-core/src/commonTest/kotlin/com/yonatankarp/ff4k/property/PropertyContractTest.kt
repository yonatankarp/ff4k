package com.yonatankarp.ff4k.property

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.test.*

abstract class PropertyContractTest<V, P : Property<V>> {

    protected open val json = Json {
        serializersModule = humanReadableSerializerModule
    }

    protected abstract val serializer: KSerializer<P>

    protected abstract fun create(
        name: String,
        value: V,
        description: String? = null,
        fixedValues: Set<V> = emptySet(),
        readOnly: Boolean = false
    ): P

    protected abstract fun sampleName(): String
    protected abstract fun sampleValue(): V
    protected abstract fun otherValueNotInFixedValues(): V
    protected abstract fun fixedValuesIncludingSample(sample: V): Set<V>

    protected open fun assertJsonHasName(jsonString: String, expectedName: String) {
        assertTrue(""""name":"$expectedName"""" in jsonString, "JSON missing name: $jsonString")
    }

    protected open fun assertJsonHasValue(jsonString: String, expectedValue: V) {
        assertTrue(""""value":$expectedValue""" in jsonString, "JSON missing value: $jsonString")
    }

    @Test
    fun `stores name and value`() {
        // Given
        val name = sampleName()
        val value = sampleValue()

        // When
        val property = create(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val name = sampleName()
        val value = sampleValue()
        val property = create(name = name, value = value)

        // When
        val jsonString = json.encodeToString(serializer, property)

        // Then
        assertJsonHasName(jsonString, name)
        assertJsonHasValue(jsonString, value)
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = create(
            name = sampleName(),
            value = sampleValue(),
            description = "Some description"
        )

        // When
        val jsonString = json.encodeToString(serializer, original)
        val deserialized = json.decodeFromString(serializer, jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = sampleName()
        val value = sampleValue()

        // When
        val property = create(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = sampleName()
        val value = sampleValue()
        val fixed = fixedValuesIncludingSample(value)

        // When
        val property = create(name = name, value = value, fixedValues = fixed)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = sampleName()
        val value = sampleValue()

        // When
        val property = create(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = sampleName()
        val value = sampleValue()
        val fixed = fixedValuesIncludingSample(value)

        // When
        val property = create(name = name, value = value, fixedValues = fixed)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = sampleName()
        val valueNotInFixed = otherValueNotInFixedValues()
        val fixed = fixedValuesIncludingSample(sampleValue())

        // When
        val property = create(name = name, value = valueNotInFixed, fixedValues = fixed)

        // Then
        assertFalse(property.isValid)
    }
}
