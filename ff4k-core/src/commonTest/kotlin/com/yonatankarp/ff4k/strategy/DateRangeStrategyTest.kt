package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

internal class DateRangeStrategyTest :
    FlippingStrategyContractTest({

        val startDate = Instant.parse("2025-01-01T00:00:00.000Z")
        val endDate = Instant.parse("2025-01-30T00:00:00.000Z")

        context("evaluate boundary conditions") {
            test("throws IllegalArgumentException when startDate is after endDate") {
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    DateRangeStrategy(
                        startDate = endDate,
                        endDate = startDate,
                    )
                }
            }

            test("returns true when now equals startDate (inclusive)") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(startDate),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns false when now equals endDate (exclusive)") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(endDate),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }

            test("returns true when now is 1ms after startDate") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(startDate.plus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns true when now is 1ms before endDate") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(endDate.minus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns false when now is 1ms before startDate") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(startDate.minus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }

            test("returns false when now is 1ms after endDate") {
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(endDate.plus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }
        }
    }) {

    private val startDate = Instant.parse("2025-01-01T00:00:00.000Z")
    private val endDate = Instant.parse("2025-01-30T00:00:00.000Z")

    override fun createStrategyForPassingCase(): FlippingStrategy = DateRangeStrategy(
        startDate = startDate,
        endDate = endDate,
        clock = fixedClock(startDate.plus(1.days)),
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = DateRangeStrategy(
        startDate = startDate,
        endDate = endDate,
        clock = fixedClock(startDate.minus(1.days)),
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"dateRange","startDate":"2025-01-01T00:00:00Z","endDate":"2025-01-30T00:00:00Z"}"""
}
