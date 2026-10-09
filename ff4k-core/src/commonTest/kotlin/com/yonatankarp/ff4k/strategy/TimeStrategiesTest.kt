package com.yonatankarp.ff4k.strategy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

class TimeStrategiesTest :
    FunSpec({
        val release = Instant.parse("2026-06-01T00:00:00Z")

        test("release date is active from the date onwards") {
            val strategy = ReleaseDateStrategy(release)
            strategy.clock = fixedClock("2026-05-31T23:59:59Z")
            strategy.evaluate(feature, emptyMap()) shouldBe false
            strategy.clock = fixedClock("2026-06-01T00:00:00Z")
            strategy.evaluate(feature, emptyMap()) shouldBe true
        }

        test("date range is inclusive at start and exclusive at end") {
            val strategy = DateRangeStrategy(release, Instant.parse("2026-06-02T00:00:00Z"))
            strategy.clock = fixedClock("2026-06-01T00:00:00Z")
            strategy.evaluate(feature, emptyMap()) shouldBe true
            strategy.clock = fixedClock("2026-06-02T00:00:00Z")
            strategy.evaluate(feature, emptyMap()) shouldBe false
            shouldThrow<IllegalArgumentException> { DateRangeStrategy(release, Instant.parse("2026-01-01T00:00:00Z")) }
        }

        test("daily hours respects the timezone") {
            val strategy = DailyHoursStrategy(9, 17, TimeZone.of("Europe/Berlin"))
            strategy.clock = fixedClock("2026-06-01T07:30:00Z") // 09:30 in Berlin
            strategy.evaluate(feature, emptyMap()) shouldBe true
            strategy.clock = fixedClock("2026-06-01T15:00:00Z") // 17:00 in Berlin
            strategy.evaluate(feature, emptyMap()) shouldBe false
            shouldThrow<IllegalArgumentException> { DailyHoursStrategy(17, 9) }
            shouldThrow<IllegalArgumentException> { DailyHoursStrategy(-1, 9) }
            shouldThrow<IllegalArgumentException> { DailyHoursStrategy(0, 25) }
        }

        test("weekday respects the timezone") {
            val strategy = WeekdayStrategy(setOf(DayOfWeek.TUESDAY), TimeZone.of("Asia/Tokyo"))
            strategy.clock = fixedClock("2026-06-01T20:00:00Z") // Tuesday 05:00 in Tokyo
            strategy.evaluate(feature, emptyMap()) shouldBe true
            strategy.clock = fixedClock("2026-06-01T10:00:00Z") // Monday in Tokyo
            strategy.evaluate(feature, emptyMap()) shouldBe false
        }

        test("the clock does not take part in equality") {
            val a = ReleaseDateStrategy(release).apply { clock = fixedClock("2026-01-01T00:00:00Z") }
            a shouldBe ReleaseDateStrategy(release)
        }
    })
