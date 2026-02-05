package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.datatest.withData
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
            context("converts percentage to weight correctly") {
                withData(
                    PercentageToWeightTestCase(50, 0.5),
                    PercentageToWeightTestCase(25, 0.25),
                    PercentageToWeightTestCase(75, 0.75),
                    PercentageToWeightTestCase(0, 0.0),
                    PercentageToWeightTestCase(100, 1.0),
                ) { (percentage, expectedWeight) ->
                    PonderationStrategy(percentage).weight shouldBe expectedWeight
                }
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
            context("throws IllegalArgumentException for invalid weight") {
                withData(
                    nameFn = { "weight: $it" },
                    -0.1,
                    1.1,
                    -1.0,
                    2.0,
                ) { weight ->
                    shouldThrow<IllegalArgumentException> {
                        PonderationStrategy(weight)
                    }
                }
            }

            context("throws IllegalArgumentException for invalid percentage") {
                withData(
                    nameFn = { "percentage: $it" },
                    -1,
                    101,
                    -50,
                    150,
                ) { percentage ->
                    shouldThrow<IllegalArgumentException> {
                        PonderationStrategy(percentage)
                    }
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

private data class PercentageToWeightTestCase(val percentage: Int, val expectedWeight: Double)
