package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

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

            test("throws IllegalStateException when user ID is missing from context") {
                // Given
                val strategy = UserPonderationStrategy(0.5)
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When/Then
                shouldThrow<IllegalStateException> {
                    strategy.evaluate("test", store, context)
                }.message shouldContain ContextKeys.USER_ID
            }
        }

        context("Int constructor") {
            test("converts percentage to weight correctly") {
                UserPonderationStrategy(50).weight shouldBe 0.5
                UserPonderationStrategy(25).weight shouldBe 0.25
                UserPonderationStrategy(75).weight shouldBe 0.75
            }

            test("handles edge cases") {
                UserPonderationStrategy(0).weight shouldBe 0.0
                UserPonderationStrategy(100).weight shouldBe 1.0
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
            test("throws IllegalArgumentException when weight is negative") {
                shouldThrow<IllegalArgumentException> {
                    UserPonderationStrategy(-0.1)
                }
            }

            test("throws IllegalArgumentException when weight is greater than 1.0") {
                shouldThrow<IllegalArgumentException> {
                    UserPonderationStrategy(1.1)
                }
            }

            test("throws IllegalArgumentException when percentage is negative") {
                shouldThrow<IllegalArgumentException> {
                    UserPonderationStrategy(-1)
                }
            }

            test("throws IllegalArgumentException when percentage is greater than 100") {
                shouldThrow<IllegalArgumentException> {
                    UserPonderationStrategy(101)
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
}
