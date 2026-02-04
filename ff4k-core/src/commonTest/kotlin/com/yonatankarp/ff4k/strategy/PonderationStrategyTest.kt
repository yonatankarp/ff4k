package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe

internal class PonderationStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate") {
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
    }) {

    override fun createStrategyForPassingCase(): FlippingStrategy = PonderationStrategy(1.0)

    override fun createStrategyForFailingCase(): FlippingStrategy = PonderationStrategy(0.0)

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"ponderation","weight":1.0}"""
}
