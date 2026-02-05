package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe

internal class UserPonderationStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate") {
            test("same user always gets the same result") {
                // Given
                val strategy = UserPonderationStrategy(0.5)
                val store = InMemoryFeatureStore()
                val context1 = FlippingExecutionContext(ContextKeys.USER_ID to "user123")
                val context2 = FlippingExecutionContext(ContextKeys.USER_ID to "user123")

                // When
                val result1 = strategy.evaluate("test", store, context1)
                val result2 = strategy.evaluate("test", store, context2)

                // Then
                result1 shouldBe result2
            }

            test("distribution across different users is approximately expected percentage") {
                // Given
                val strategy = UserPonderationStrategy(0.5)
                val store = InMemoryFeatureStore()

                // When
                val enabled = (1..1000).count { userId ->
                    val context =
                        FlippingExecutionContext(ContextKeys.USER_ID to "user$userId")
                    strategy.evaluate("test", store, context)
                }

                // Then
                val actualPercentage = enabled.toDouble() / 1000
                // Should be approximately 50% across different users
                actualPercentage.shouldBeBetween(0.5, 0.5, tolerance = 0.05)
            }

            test("always returns true when weight is 1.0") {
                // Given
                val strategy = UserPonderationStrategy(1.0)
                val store = InMemoryFeatureStore()

                // When / Then
                repeat(100) { userId ->
                    val context =
                        FlippingExecutionContext(ContextKeys.USER_ID to "user$userId")
                    strategy.evaluate("test", store, context).shouldBeTrue()
                }
            }

            test("always returns false when weight is 0.0") {
                // Given
                val strategy = UserPonderationStrategy(0.0)
                val store = InMemoryFeatureStore()

                // When / Then
                repeat(100) { userId ->
                    val context =
                        FlippingExecutionContext(ContextKeys.USER_ID to "user$userId")
                    strategy.evaluate("test", store, context).shouldBeFalse()
                }
            }
        }

        context("Int constructor") {
            context("converts percentage to weight correctly") {
                withData(
                    UserPercentageToWeightTestCase(50, 0.5),
                    UserPercentageToWeightTestCase(25, 0.25),
                    UserPercentageToWeightTestCase(75, 0.75),
                    UserPercentageToWeightTestCase(0, 0.0),
                    UserPercentageToWeightTestCase(100, 1.0),
                ) { (percentage, expectedWeight) ->
                    UserPonderationStrategy(percentage).weight shouldBe expectedWeight
                }
            }

            test("evaluates correctly with percentage constructor") {
                // Given
                val strategy = UserPonderationStrategy(100)
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext(ContextKeys.USER_ID to "user123")

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
                        UserPonderationStrategy(weight)
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
                        UserPonderationStrategy(percentage)
                    }
                }
            }
        }
    }) {

    override fun createStrategyForPassingCase(): FlippingStrategy = UserPonderationStrategy(weight = 1.0)

    override fun createStrategyForFailingCase(): FlippingStrategy = UserPonderationStrategy(weight = 0.0)

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "test-user")

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "test-user")

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"userPonderation","weight":1.0}"""

    override fun requiredContextKeys(): Set<String> = setOf(ContextKeys.USER_ID)
}

private data class UserPercentageToWeightTestCase(val percentage: Int, val expectedWeight: Double)
