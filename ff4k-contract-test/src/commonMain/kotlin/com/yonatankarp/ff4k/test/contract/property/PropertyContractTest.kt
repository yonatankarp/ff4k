@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.property

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import com.yonatankarp.ff4k.property.Property
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        readOnly: Boolean = false,
    ): P

    protected abstract fun sampleName(): String
    protected abstract fun sampleValue(): V
    protected abstract fun otherValueNotInFixedValues(): V
    protected abstract fun fixedValuesIncludingSample(sample: V): Set<V>

    protected open fun assertJsonHasName(
        jsonString: String,
        expectedName: String,
    ) {
        assertTrue(
            """"name":"$expectedName"""" in jsonString,
            "JSON missing name: $jsonString",
        )
    }

    protected open fun assertJsonHasValue(
        jsonString: String,
        expectedValue: V,
    ) {
        assertTrue(
            """"value":$expectedValue""" in jsonString,
            "JSON missing value: $jsonString",
        )
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
            description = "Some description",
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
    fun `should create property without errors when no fixed values provided`() {
        // Given
        val name = sampleName()
        val value = sampleValue()

        // When / Then
        val property = create(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `should create property without errors when value within fixed values`() {
        // Given
        val name = sampleName()
        val value = sampleValue()
        val fixedValues = fixedValuesIncludingSample(value)

        // When / Then
        val property = create(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `should create property without errors when fixed values is empty`() {
        // Given
        val name = sampleName()
        val value = sampleValue()
        val fixedValues = emptySet<V>()

        // When / Then
        val property = create(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `should throw exception when value is not within fixed values`() {
        // Given
        val name = sampleName()
        val value = otherValueNotInFixedValues()
        val fixed = fixedValuesIncludingSample(sampleValue())

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            create(name = name, value = value, fixedValues = fixed)
        }
    }
}
