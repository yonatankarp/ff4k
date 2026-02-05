package com.yonatankarp.ff4k.dsl.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * Tests for SetCollector and ListCollector DSL base classes.
 */
internal class DslCollectorsTest :
    FunSpec({

        /**
         * Concrete implementation of SetCollector for testing.
         */
        class TestSetCollector<T> : SetCollector<T>()

        /**
         * Concrete implementation of ListCollector for testing.
         */
        class TestListCollector<T> : ListCollector<T>()

        test("SetCollector builds empty set when no values added") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.build()

            // Then
            result.shouldBeEmpty()
        }

        test("SetCollector adds value using unary plus operator") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.apply {
                +"value1"
            }.build()

            // Then
            result shouldBe setOf("value1")
        }

        test("SetCollector adds multiple values using unary plus operator") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.apply {
                +"value1"
                +"value2"
                +"value3"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2", "value3")
        }

        test("SetCollector adds value using add method") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.apply {
                add("value1")
            }.build()

            // Then
            result shouldBe setOf("value1")
        }

        test("SetCollector removes duplicates automatically") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.apply {
                +"value1"
                +"value2"
                +"value1"
                +"value3"
                +"value2"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2", "value3")
        }

        test("SetCollector works with Int values") {
            // Given
            val collector = TestSetCollector<Int>()

            // When
            val result = collector.apply {
                add(1)
                add(2)
                add(3)
            }.build()

            // Then
            result shouldBe setOf(1, 2, 3)
        }

        test("SetCollector works with Boolean values") {
            // Given
            val collector = TestSetCollector<Boolean>()

            // When
            val result = collector.apply {
                +true
                +false
            }.build()

            // Then
            result shouldBe setOf(true, false)
        }

        test("SetCollector combines unary plus and add methods") {
            // Given
            val collector = TestSetCollector<String>()

            // When
            val result = collector.apply {
                +"value1"
                add("value2")
                +"value3"
            }.build()

            // Then
            result shouldBe setOf("value1", "value2", "value3")
        }

        test("ListCollector builds empty list when no items added") {
            // Given
            val collector = TestListCollector<String>()

            // When
            val result = collector.build()

            // Then
            result.shouldBeEmpty()
        }

        test("ListCollector adds item using unary plus operator") {
            // Given
            val collector = TestListCollector<String>()

            // When
            val result = collector.apply {
                +"item1"
            }.build()

            // Then
            result shouldBe listOf("item1")
        }

        test("ListCollector adds multiple items using unary plus operator") {
            // Given
            val collector = TestListCollector<String>()

            // When
            val result = collector.apply {
                +"item1"
                +"item2"
                +"item3"
            }.build()

            // Then
            result shouldBe listOf("item1", "item2", "item3")
        }

        test("ListCollector preserves insertion order") {
            // Given
            val collector = TestListCollector<String>()

            // When
            val result = collector.apply {
                +"third"
                +"first"
                +"second"
            }.build()

            // Then
            result shouldContainExactly listOf("third", "first", "second")
        }

        test("ListCollector allows duplicates") {
            // Given
            val collector = TestListCollector<String>()

            // When
            val result = collector.apply {
                +"item1"
                +"item2"
                +"item1"
                +"item3"
                +"item1"
            }.build()

            // Then
            result shouldContainExactly listOf("item1", "item2", "item1", "item3", "item1")
        }

        test("ListCollector works with Int values") {
            // Given
            val collector = TestListCollector<Int>()

            // When
            val result = collector.apply {
                add(1)
                add(2)
                add(3)
            }.build()

            // Then
            result shouldBe listOf(1, 2, 3)
        }

        test("ListCollector works with custom objects") {
            // Given
            data class Item(val id: Int, val name: String)
            val item1 = Item(1, "first")
            val item2 = Item(2, "second")
            val collector = TestListCollector<Item>()

            // When
            val result = collector.apply {
                +item1
                +item2
            }.build()

            // Then
            result shouldBe listOf(item1, item2)
        }
    })
