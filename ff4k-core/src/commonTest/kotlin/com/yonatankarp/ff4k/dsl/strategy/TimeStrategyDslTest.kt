package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * @author Yonatan Karp-Rudin
 */
internal class TimeStrategyDslTest :
    FunSpec({

        context("releaseDateStrategy") {
            test("sets ReleaseDateStrategy with Instant") {
                val date = Instant.parse("2025-01-01T00:00:00Z")
                val feature = feature("test") {
                    releaseDateStrategy(date)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe date
            }

            test("sets ReleaseDateStrategy with ISO string") {
                val dateString = "2025-01-01T00:00:00Z"
                val feature = feature("test") {
                    releaseDateStrategy(dateString)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe Instant.parse(dateString)
            }

            test("sets ReleaseDateStrategy with LocalDateTime") {
                val dateTime = LocalDateTime(2025, 1, 1, 0, 0)
                val timezone = TimeZone.UTC
                val feature = feature("test") {
                    releaseDateStrategy(dateTime, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe dateTime.toInstant(timezone)
            }
        }

        context("dateRangeStrategy") {
            test("sets DateRangeStrategy with Instant") {
                val start = Instant.parse("2025-01-01T00:00:00Z")
                val end = Instant.parse("2025-01-02T00:00:00Z")
                val feature = feature("test") {
                    dateRangeStrategy(start, end)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe start
                strategy.endDate shouldBe end
            }

            test("sets DateRangeStrategy with ISO string") {
                val start = "2025-01-01T00:00:00Z"
                val end = "2025-01-02T00:00:00Z"
                val feature = feature("test") {
                    dateRangeStrategy(start, end)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe Instant.parse(start)
                strategy.endDate shouldBe Instant.parse(end)
            }

            test("sets DateRangeStrategy with LocalDateTime") {
                val start = LocalDateTime(2025, 1, 1, 0, 0)
                val end = LocalDateTime(2025, 1, 2, 0, 0)
                val timezone = TimeZone.UTC
                val feature = feature("test") {
                    dateRangeStrategy(start, end, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe start.toInstant(timezone)
                strategy.endDate shouldBe end.toInstant(timezone)
            }
        }

        context("dailyHoursStrategy") {
            test("sets DailyHoursStrategy") {
                val startHour = 9
                val endHour = 17
                val timezone = TimeZone.of("Europe/Paris")

                val feature = feature("test") {
                    dailyHoursStrategy(startHour, endHour, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DailyHoursStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startHour shouldBe startHour
                strategy.endHour shouldBe endHour
                strategy.timezone shouldBe timezone
            }
        }

        test("weekdayStrategy sets WeekdayStrategy with timezone") {
            val timezone = TimeZone.of("Asia/Tokyo")
            val feature = feature("test") {
                weekdayStrategy(timezone) {
                    +DayOfWeek.MONDAY
                    +DayOfWeek.FRIDAY
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
            val strategy = feature.flippingStrategy
            strategy.allowedDays shouldContainExactlyInAnyOrder setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            strategy.timezone shouldBe timezone
        }

        test("weekdayStrategy defaults to UTC") {
            val feature = feature("test") {
                weekdayStrategy {
                    +DayOfWeek.MONDAY
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
            feature.flippingStrategy.timezone shouldBe TimeZone.UTC
        }
    })
