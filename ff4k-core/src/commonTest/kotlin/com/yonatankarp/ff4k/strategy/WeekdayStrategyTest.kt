package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class WeekdayStrategyTest :
    FlippingStrategyContractTest({

        // Reference dates:
        // 2025-01-13 = Monday
        // 2025-01-14 = Tuesday
        // 2025-01-15 = Wednesday
        // 2025-01-16 = Thursday
        // 2025-01-17 = Friday
        // 2025-01-18 = Saturday
        // 2025-01-19 = Sunday

        context("evaluate with allowed days") {
            test("returns true when current day is in allowedDays") {
                // Given - Monday
                val strategy = createStrategy(currentTime = "2025-01-13T12:00:00Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeTrue()
            }

            test("returns false when current day is not in allowedDays") {
                // Given - Tuesday (not in allowed days)
                val strategy = createStrategy(currentTime = "2025-01-14T12:00:00Z")

                // When
                val result = strategy.evaluate(
                    featureId = "test",
                    store = InMemoryFeatureStore(),
                    context = FlippingExecutionContext(),
                )

                // Then
                result.shouldBeFalse()
            }

            test("returns true for weekend day when weekends are allowed") {
                // Given - Saturday
                val strategy = createStrategy(
                    allowedDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                    currentTime = "2025-01-18T12:00:00Z",
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

            test("returns false for weekend day when only weekdays are allowed") {
                // Given - Sunday
                val strategy = createStrategy(
                    allowedDays = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                    ),
                    currentTime = "2025-01-19T12:00:00Z",
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

            test("returns false when allowedDays is empty") {
                // Given - any day with empty allowedDays
                val strategy = createStrategy(
                    allowedDays = emptySet(),
                    currentTime = "2025-01-15T12:00:00Z",
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

            test("returns true when all days are allowed") {
                // Given - any day with all days allowed
                val strategy = createStrategy(
                    allowedDays = DayOfWeek.entries.toSet(),
                    currentTime = "2025-01-15T12:00:00Z",
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

        context("timezone handling") {
            test("evaluates day in the specified timezone") {
                // Given - 2025-01-13 23:00 UTC = 2025-01-14 08:00 Tokyo (Tuesday in Tokyo)
                val strategy = createStrategy(
                    allowedDays = setOf(DayOfWeek.TUESDAY),
                    timezone = TimeZone.of("Asia/Tokyo"),
                    currentTime = "2025-01-13T23:00:00Z",
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

            test("returns false when day differs in specified timezone") {
                // Given - 2025-01-13 23:00 UTC = still Monday in New York (UTC-5)
                val strategy = createStrategy(
                    allowedDays = setOf(DayOfWeek.TUESDAY),
                    timezone = TimeZone.of("America/New_York"),
                    currentTime = "2025-01-13T23:00:00Z",
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
    }) {

    override fun createStrategyForPassingCase(): FlippingStrategy = WeekdayStrategy(
        allowedDays = setOf(DayOfWeek.WEDNESDAY),
        timezone = TimeZone.of("America/New_York"),
        clock = fixedClock(Instant.parse("2025-01-15T12:00:00Z")), // Wednesday
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = WeekdayStrategy(
        allowedDays = setOf(DayOfWeek.MONDAY),
        timezone = TimeZone.of("America/New_York"),
        clock = fixedClock(Instant.parse("2025-01-15T12:00:00Z")), // Wednesday, not Monday
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"weekday","allowedDays":["WEDNESDAY"],"timezone":"America/New_York"}"""
}

private fun createStrategy(
    allowedDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
    timezone: TimeZone = TimeZone.UTC,
    currentTime: String,
) = WeekdayStrategy(
    allowedDays = allowedDays,
    timezone = timezone,
    clock = fixedClock(Instant.parse(currentTime)),
)
