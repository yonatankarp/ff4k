package com.yonatankarp.ff4k.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

/**
 * Tests for FlippingExecutionContext.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FlippingExecutionContextTest :
    FunSpec({

        test("should store and retrieve values with correct types") {
            // Given
            val values = mapOf(
                ContextKeys.USER_ID to 123,
                ContextKeys.USER_NAME to "Alice",
                "isActive" to true,
            )
            val context = FlippingExecutionContext(values)

            // When
            val userId = context.get<Int>(ContextKeys.USER_ID)
            val userName = context.get<String>(ContextKeys.USER_NAME)
            val isActive = context.get<Boolean>("isActive")

            // Then
            userId shouldBe 123
            userName shouldBe "Alice"
            isActive shouldBe true
        }

        test("should return null for non-existent keys") {
            // Given
            val context = FlippingExecutionContext()

            // When
            val result = context.get<String>("missingKey")

            // Then
            result shouldBe null
        }

        test("should throw when type mismatch occurs") {
            // Given
            val values = mapOf(ContextKeys.USER_ID to 123)
            val context = FlippingExecutionContext(values)

            // When / Then
            shouldThrow<IllegalStateException> {
                context.get<String>(ContextKeys.USER_ID)
            }
        }

        test("should throw when required key is missing") {
            // Given
            val context = FlippingExecutionContext()

            // When / Then
            shouldThrow<IllegalArgumentException> {
                context.get<String>("missingKey", required = true)
            }
        }

        test("should not throw when required key exists") {
            // Given
            val values = mapOf("key" to "value")
            val context = FlippingExecutionContext(values)

            // When
            val result = context.get<String>("key", required = true)

            // Then
            result shouldBe "value"
        }

        test("contains operator should return true for existing keys") {
            // Given
            val values = mapOf(ContextKeys.USER_ID to 123)
            val context = FlippingExecutionContext(values)

            // When
            val contains = ContextKeys.USER_ID in context

            // Then
            contains.shouldBeTrue()
        }

        test("contains operator should return false for non-existent keys") {
            // Given
            val context = FlippingExecutionContext()

            // When
            val contains = ContextKeys.USER_ID in context

            // Then
            contains.shouldBeFalse()
        }

        test("isEmpty should return true for new context") {
            // Given
            val context = FlippingExecutionContext()

            // When
            val isEmpty = context.isEmpty

            // Then
            isEmpty.shouldBeTrue()
        }

        test("isEmpty should return false after adding values") {
            // Given
            val values = mapOf("key" to "value")
            val context = FlippingExecutionContext(values)

            // When
            val isEmpty = context.isEmpty

            // Then
            isEmpty.shouldBeFalse()
        }

        test("should work with data class as value in context") {
            // Given
            data class User(val id: Int, val name: String)
            val user = User(1, "Alice")
            val values = mapOf("user" to user)
            val context = FlippingExecutionContext(values)

            // When
            val result = context.get<User>("user")

            // Then
            result shouldBe user
        }

        test("should support multiple types in same context") {
            // Given
            val values = mapOf(
                "string" to "text",
                "int" to 42,
                "double" to 3.14,
                "boolean" to true,
                "list" to listOf(1, 2, 3),
            )
            val context = FlippingExecutionContext(values)

            // When
            val string = context.get<String>("string")
            val int = context.get<Int>("int")
            val double = context.get<Double>("double")
            val boolean = context.get<Boolean>("boolean")
            val list = context.get<List<Int>>("list")

            // Then
            string shouldBe "text"
            int shouldBe 42
            double shouldBe 3.14
            boolean shouldBe true
            list shouldBe listOf(1, 2, 3)
        }

        test("plus operator should combine two contexts") {
            // Given
            val context1 = FlippingExecutionContext(ContextKeys.USER_ID to "user-123", ContextKeys.REGION to "EU")
            val context2 = FlippingExecutionContext("tier" to "premium", "enabled" to true)

            // When
            val combined = context1 + context2

            // Then
            combined.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
            combined.get<String>(ContextKeys.REGION) shouldBe "EU"
            combined.get<String>("tier") shouldBe "premium"
            combined.get<Boolean>("enabled") shouldBe true
        }

        test("plus operator should give precedence to right context for duplicate keys") {
            // Given
            val context1 = FlippingExecutionContext("tier" to "free", ContextKeys.REGION to "US")
            val context2 = FlippingExecutionContext("tier" to "premium")

            // When
            val combined = context1 + context2

            // Then
            combined.get<String>("tier") shouldBe "premium"
            combined.get<String>(ContextKeys.REGION) shouldBe "US"
        }

        test("plus operator should not modify original contexts") {
            // Given
            val context1 = FlippingExecutionContext("key1" to "value1")
            val context2 = FlippingExecutionContext("key2" to "value2")

            // When
            val combined = context1 + context2

            // Then
            ("key2" in context1).shouldBeFalse()
            ("key1" in context2).shouldBeFalse()
            ("key1" in combined).shouldBeTrue()
            ("key2" in combined).shouldBeTrue()
        }

        test("plus operator with empty context should return equivalent context") {
            // Given
            val context = FlippingExecutionContext(ContextKeys.USER_ID to "user-123")
            val empty = FlippingExecutionContext()

            // When
            val result1 = context + empty
            val result2 = empty + context

            // Then
            result1.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
            result2.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
        }
    })
