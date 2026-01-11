package com.yonatankarp.ff4k.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FlippingExecutionContext.
 *
 * @author Yonatan Karp-Rudin
 */
class FlippingExecutionContextTest {

    @Test
    fun `should store and retrieve values with correct types`() {
        // Given
        val context = FlippingExecutionContext()
        context["userId"] = 123
        context["userName"] = "Alice"
        context["isActive"] = true

        // When
        val userId = context.get<Int>("userId")
        val userName = context.get<String>("userName")
        val isActive = context.get<Boolean>("isActive")

        // Then
        assertEquals(123, userId)
        assertEquals("Alice", userName)
        assertEquals(true, isActive)
    }

    @Test
    fun `should return null for non-existent keys`() {
        // Given
        val context = FlippingExecutionContext()

        // When
        val result = context.get<String>("missingKey")

        // Then
        assertNull(result)
    }

    @Test
    fun `should throw when type mismatch occurs`() {
        // Given
        val context = FlippingExecutionContext()
        context["userId"] = 123

        // When / Then
        assertFailsWith<IllegalStateException> {
            context.get<String>("userId")
        }
    }

    @Test
    fun `should throw when required key is missing`() {
        // Given
        val context = FlippingExecutionContext()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            context.get<String>("missingKey", required = true)
        }
    }

    @Test
    fun `should not throw when required key exists`() {
        // Given
        val context = FlippingExecutionContext()
        context["key"] = "value"

        // When
        val result = context.get<String>("key", required = true)

        // Then
        assertEquals("value", result)
    }

    @Test
    fun `contains operator should return true for existing keys`() {
        // Given
        val context = FlippingExecutionContext()
        context["userId"] = 123

        // When
        val contains = "userId" in context

        // Then
        assertTrue(contains)
    }

    @Test
    fun `contains operator should return false for non-existent keys`() {
        // Given
        val context = FlippingExecutionContext()

        // When
        val contains = "userId" in context

        // Then
        assertFalse(contains)
    }

    @Test
    fun `isEmpty should return true for new context`() {
        // Given
        val context = FlippingExecutionContext()

        // When
        val isEmpty = context.isEmpty

        // Then
        assertTrue(isEmpty)
    }

    @Test
    fun `isEmpty should return false after adding values`() {
        // Given
        val context = FlippingExecutionContext()
        context["key"] = "value"

        // When
        val isEmpty = context.isEmpty

        // Then
        assertFalse(isEmpty)
    }

    @Test
    fun `should handle null values correctly`() {
        // Given
        val context = FlippingExecutionContext()
        context["nullableValue"] = null

        // When
        val value = context.get<String?>("nullableValue")
        val contains = "nullableValue" in context

        // Then
        assertNull(value)
        assertTrue(contains)
    }

    @Test
    fun `should work with data class as value in context`() {
        // Given
        data class User(val id: Int, val name: String)
        val context = FlippingExecutionContext()
        val user = User(1, "Alice")
        context["user"] = user

        // When
        val result = context.get<User>("user")

        // Then
        assertEquals(user, result)
    }

    @Test
    fun `should support multiple types in same context`() {
        // Given
        val context = FlippingExecutionContext()
        context["string"] = "text"
        context["int"] = 42
        context["double"] = 3.14
        context["boolean"] = true
        context["list"] = listOf(1, 2, 3)

        // When
        val string = context.get<String>("string")
        val int = context.get<Int>("int")
        val double = context.get<Double>("double")
        val boolean = context.get<Boolean>("boolean")
        val list = context.get<List<Int>>("list")

        // Then
        assertEquals("text", string)
        assertEquals(42, int)
        assertEquals(3.14, double)
        assertEquals(true, boolean)
        assertEquals(listOf(1, 2, 3), list)
    }
}
