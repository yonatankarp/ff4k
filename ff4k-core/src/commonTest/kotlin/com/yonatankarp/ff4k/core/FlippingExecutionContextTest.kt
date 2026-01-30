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
            val context = FlippingExecutionContext()
            context["userId"] = 123
            context["userName"] = "Alice"
            context["isActive"] = true

            // When
            val userId = context.get<Int>("userId")
            val userName = context.get<String>("userName")
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
            val context = FlippingExecutionContext()
            context["userId"] = 123

            // When / Then
            shouldThrow<IllegalStateException> {
                context.get<String>("userId")
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
            val context = FlippingExecutionContext()
            context["key"] = "value"

            // When
            val result = context.get<String>("key", required = true)

            // Then
            result shouldBe "value"
        }

        test("contains operator should return true for existing keys") {
            // Given
            val context = FlippingExecutionContext()
            context["userId"] = 123

            // When
            val contains = "userId" in context

            // Then
            contains.shouldBeTrue()
        }

        test("contains operator should return false for non-existent keys") {
            // Given
            val context = FlippingExecutionContext()

            // When
            val contains = "userId" in context

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
            val context = FlippingExecutionContext()
            context["key"] = "value"

            // When
            val isEmpty = context.isEmpty

            // Then
            isEmpty.shouldBeFalse()
        }

        test("should handle null values correctly") {
            // Given
            val context = FlippingExecutionContext()
            context["nullableValue"] = null

            // When
            val value = context.get<String?>("nullableValue")
            val contains = "nullableValue" in context

            // Then
            value shouldBe null
            contains.shouldBeTrue()
        }

        test("should work with data class as value in context") {
            // Given
            data class User(val id: Int, val name: String)
            val context = FlippingExecutionContext()
            val user = User(1, "Alice")
            context["user"] = user

            // When
            val result = context.get<User>("user")

            // Then
            result shouldBe user
        }

        test("should support multiple types in same context") {
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
            string shouldBe "text"
            int shouldBe 42
            double shouldBe 3.14
            boolean shouldBe true
            list shouldBe listOf(1, 2, 3)
        }
    })
