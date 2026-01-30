package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.dsl.internal.property
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Tests for FixedValuesBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FixedValuesBuilderTest :
    FunSpec({

        test("builds empty set when no values added") {
            // When
            val result = FixedValuesBuilder<String>().build()

            // Then
            result.shouldBeEmpty()
        }

        test("builds set with single value") {
            // When
            val result = FixedValuesBuilder<String>().apply {
                +"value1"
            }.build()

            // Then
            result shouldBe setOf("value1")
        }

        test("builds set with multiple values") {
            // When
            val result = FixedValuesBuilder<String>().apply {
                +"value1"
                +"value2"
                +"value3"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2", "value3")
        }

        test("removes duplicates automatically") {
            // When
            val result = FixedValuesBuilder<String>().apply {
                +"value1"
                +"value2"
                +"value1"
                +"value2"
                +"value3"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2", "value3")
        }

        test("works with Int values using add method") {
            // When
            val result = FixedValuesBuilder<Int>().apply {
                add(1)
                add(2)
                add(3)
            }.build()

            // Then
            result shouldBe setOf(1, 2, 3)
        }

        test("works with Boolean values") {
            // When
            val result = FixedValuesBuilder<Boolean>().apply {
                +true
                +false
            }.build()

            // Then
            result shouldBe setOf(true, false)
        }

        test("builds immutable set") {
            // When
            val result = FixedValuesBuilder<String>().apply {
                +"value1"
                +"value2"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2")
            result.size shouldBe 2
        }

        test("can be reused in DSL context") {
            // When
            val property = property(name = "test") {
                value = "a"
                fixedValues {
                    +"a"
                    +"b"
                    +"c"
                }
            }

            // Then
            property.fixedValues shouldBe setOf("a", "b", "c")
        }
    })
