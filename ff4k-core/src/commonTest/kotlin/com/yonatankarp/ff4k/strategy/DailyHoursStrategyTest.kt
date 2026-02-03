package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class DailyHoursStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate boundary conditions") {
            test("returns true when current hour equals startHour (inclusive)") {
                // Given - 10:00 UTC
                val strategy = createStrategy(currentTime = "2025-01-15T10:00:00Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeTrue()
            }

            test("returns false when current hour equals endHour (exclusive)") {
                // Given - 14:00 UTC
                val strategy = createStrategy(currentTime = "2025-01-15T14:00:00Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeFalse()
            }

            test("returns true when current hour is within range") {
                // Given - 12:30 UTC
                val strategy = createStrategy(currentTime = "2025-01-15T12:30:00Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeTrue()
            }

            test("returns false when current hour is before startHour") {
                // Given - 09:59 UTC
                val strategy = createStrategy(currentTime = "2025-01-15T09:59:59Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeFalse()
            }

            test("returns false when current hour is after endHour") {
                // Given - 15:00 UTC
                val strategy = createStrategy(currentTime = "2025-01-15T15:00:00Z")

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
            test("throws IllegalArgumentException when startHour is negative") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = -1, endHour = 10)
                }
            }

            test("throws IllegalArgumentException when startHour is greater than 23") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 24, endHour = 25)
                }
            }

            test("throws IllegalArgumentException when endHour is negative") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 0, endHour = -1)
                }
            }

            test("throws IllegalArgumentException when endHour is zero") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 0, endHour = 0)
                }
            }

            test("throws IllegalArgumentException when endHour is greater than 24") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 10, endHour = 25)
                }
            }

            test("throws IllegalArgumentException when endHour equals startHour") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 10, endHour = 10)
                }
            }

            test("throws IllegalArgumentException when endHour is before startHour") {
                shouldThrow<IllegalArgumentException> {
                    DailyHoursStrategy(startHour = 14, endHour = 10)
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
