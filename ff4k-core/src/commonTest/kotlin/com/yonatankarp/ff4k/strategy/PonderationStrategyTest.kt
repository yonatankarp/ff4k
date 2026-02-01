package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.serialization.FF4kJson
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString

internal class PonderationStrategyTest :
    FunSpec({

        context("evaluate") {
            test("always returns true when weight is 1.0") {
                // Given
                val strategy = PonderationStrategy(1.0)
                val context = FlippingExecutionContext()
                val store = InMemoryFeatureStore()

                // When / Then
                repeat(100) {
                    strategy.evaluate("test", store, context).shouldBeTrue()
                }
            }

            test("always returns false when weight is 0.0") {
                // Given
                val strategy = PonderationStrategy(0.0)
                val context = FlippingExecutionContext()
                val store = InMemoryFeatureStore()

                // When / Then
                repeat(100) {
                    strategy.evaluate("test", store, context).shouldBeFalse()
                }
            }

            test("returns approximately expected percentage") {
                // Given
                val strategy = PonderationStrategy(0.5)
                val context = FlippingExecutionContext()
                val store = InMemoryFeatureStore()
                val iterations = 10_000

                // When
                val enabled = (1..iterations).count {
                    strategy.evaluate("test", store, context)
                }

                // Then
                val actualPercentage = enabled.toDouble() / iterations
                actualPercentage.shouldBeBetween(0.5, 0.5, tolerance = 0.05)
            }

            test("handles null feature store") {
                // Given
                val strategy = PonderationStrategy(1.0)
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", null, context)

                // Then
                result.shouldBeTrue()
            }
        }

        context("Int constructor") {
            test("converts percentage to weight correctly") {
                PonderationStrategy(50).weight shouldBe 0.5
                PonderationStrategy(25).weight shouldBe 0.25
                PonderationStrategy(75).weight shouldBe 0.75
            }

            test("handles edge cases") {
                PonderationStrategy(0).weight shouldBe 0.0
                PonderationStrategy(100).weight shouldBe 1.0
            }

            test("evaluates correctly with percentage constructor") {
                // Given
                val strategy = PonderationStrategy(100)
                val context = FlippingExecutionContext()
                val store = InMemoryFeatureStore()

                // When / Then
                repeat(10) {
                    strategy.evaluate("test", store, context).shouldBeTrue()
                }
            }
        }

        context("validation") {
            test("throws IllegalArgumentException when weight is negative") {
                shouldThrow<IllegalArgumentException> {
                    PonderationStrategy(-0.1)
                }
            }

            test("throws IllegalArgumentException when weight is greater than 1.0") {
                shouldThrow<IllegalArgumentException> {
                    PonderationStrategy(1.1)
                }
            }

            test("throws IllegalArgumentException when percentage is negative") {
                shouldThrow<IllegalArgumentException> {
                    PonderationStrategy(-1)
                }
            }

            test("throws IllegalArgumentException when percentage is greater than 100") {
                shouldThrow<IllegalArgumentException> {
                    PonderationStrategy(101)
                }
            }
        }

        context("serialization") {
            test("round-trip serialization preserves strategy") {
                // Given
                val strategy = PonderationStrategy(0.75)

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }
        }
    })
