package com.yonatankarp.ff4k.property.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for FixedValuesBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
class FixedValuesBuilderTest {

    @Test
    fun `builds empty set when no values added`() {
        // When
        val result = FixedValuesBuilder<String>().build()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `builds set with single value`() {
        // When
        val result = FixedValuesBuilder<String>().apply {
            +"value1"
        }.build()

        // Then
        assertEquals(setOf("value1"), result)
    }

    @Test
    fun `builds set with multiple values`() {
        // When
        val result = FixedValuesBuilder<String>().apply {
            +"value1"
            +"value2"
            +"value3"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `removes duplicates automatically`() {
        // When
        val result = FixedValuesBuilder<String>().apply {
            +"value1"
            +"value2"
            +"value1"
            +"value2"
            +"value3"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `works with Int values using add method`() {
        // When
        val result = FixedValuesBuilder<Int>().apply {
            add(1)
            add(2)
            add(3)
        }.build()

        // Then
        assertEquals(setOf(1, 2, 3), result)
    }

    @Test
    fun `works with Boolean values`() {
        // When
        val result = FixedValuesBuilder<Boolean>().apply {
            +true
            +false
        }.build()

        // Then
        assertEquals(setOf(true, false), result)
    }

    @Test
    fun `builds immutable set`() {
        // When
        val result = FixedValuesBuilder<String>().apply {
            +"value1"
            +"value2"
        }.build()

        // Then
        assertEquals(setOf("value1", "value2"), result)
        assertEquals(2, result.size)
    }

    @Test
    fun `can be reused in DSL context`() {
        // When
        val property = stringProperty("test") {
            value = "a"
            fixedValues {
                +"a"
                +"b"
                +"c"
            }
        }

        // Then
        assertEquals(setOf("a", "b", "c"), property.fixedValues)
    }
}
