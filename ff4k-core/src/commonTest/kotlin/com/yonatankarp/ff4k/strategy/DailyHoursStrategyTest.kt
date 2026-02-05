package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class DailyHoursStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate boundary conditions") {
            withData(
                nameFn = { "time: ${it.currentTime} -> expect: ${it.shouldBeEnabled}" },
                // 10:00 start (inclusive)
                DailyHoursEvaluationTestCase("2025-01-15T10:00:00Z", true),
                // 14:00 end (exclusive)
                DailyHoursEvaluationTestCase("2025-01-15T14:00:00Z", false),
                // 12:30 in range
                DailyHoursEvaluationTestCase("2025-01-15T12:30:00Z", true),
                // 09:59 before start
                DailyHoursEvaluationTestCase("2025-01-15T09:59:59Z", false),
                // 15:00 after end
                DailyHoursEvaluationTestCase("2025-01-15T15:00:00Z", false),
            ) { (currentTime, shouldBeEnabled) ->
                // Given - range 10:00 to 14:00
                val strategy = createStrategy(currentTime = currentTime)

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result shouldBe shouldBeEnabled
            }
        }

        context("timezone handling") {
            test("evaluates hours in the specified timezone") {
                // Given - 15:00 UTC = 10:00 America/New_York (EST, UTC-5)
                val strategy = createStrategy(
                    timeZone = TimeZone.of("America/New_York"),
                    currentTime = "2025-01-15T15:00:00Z",
                )

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeTrue()
            }

            test("returns false when outside range in specified timezone") {
                // Given - 14:00 UTC = 09:00 America/New_York (before 10:00)
                val strategy = createStrategy(
                    timeZone = TimeZone.of("America/New_York"),
                    currentTime = "2025-01-15T14:00:00Z",
                )

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeFalse()
            }
        }

        context("validation") {
            context("throws IllegalArgumentException for invalid configuration") {
                withData(
                    nameFn = { "start: ${it.start}, end: ${it.end}" },
                    InvalidDailyHoursTestCase(-1, 10), // start negative
                    InvalidDailyHoursTestCase(24, 25), // start > 23
                    InvalidDailyHoursTestCase(0, -1), // end negative
                    InvalidDailyHoursTestCase(0, 0), // end zero
                    InvalidDailyHoursTestCase(10, 25), // end > 24
                    InvalidDailyHoursTestCase(10, 10), // start == end
                    InvalidDailyHoursTestCase(14, 10), // start > end
                ) { (start, end) ->
                    shouldThrow<IllegalArgumentException> {
                        DailyHoursStrategy(startHour = start, endHour = end)
                    }
                }
            }

            test("allows endHour of 24 for end-of-day") {
                // Given - 23:30 UTC
                val strategy = createStrategy(
                    startHour = 22,
                    endHour = 24,
                    currentTime = "2025-01-15T23:30:00Z",
                )

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeTrue()
            }
        }
    }) {

    override fun createStrategyForPassingCase(): FlippingStrategy = DailyHoursStrategy(
        startHour = 8,
        endHour = 17,
        timezone = TimeZone.of("America/New_York"),
        clock = fixedClock(Instant.parse("2025-01-15T17:00:00Z")), // 17:00 UTC is 12:00 NY, within 8-17
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = DailyHoursStrategy(
        startHour = 8,
        endHour = 17,
        timezone = TimeZone.of("America/New_York"),
        clock = fixedClock(Instant.parse("2025-01-15T06:00:00Z")), // 06:00 is before 8
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"dailyHours","startHour":8,"endHour":17,"timezone":"America/New_York"}"""
}

private fun createStrategy(
    startHour: Int = 10,
    endHour: Int = 14,
    timeZone: TimeZone = TimeZone.UTC,
    currentTime: String,
) = DailyHoursStrategy(
    startHour = startHour,
    endHour = endHour,
    timezone = timeZone,
    clock = fixedClock(Instant.parse(currentTime)),
)

private data class DailyHoursEvaluationTestCase(val currentTime: String, val shouldBeEnabled: Boolean)

private data class InvalidDailyHoursTestCase(val start: Int, val end: Int)
