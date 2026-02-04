package com.yonatankarp.ff4k.dsl.strategy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

internal class WeekdayBuilderTest :
    FunSpec({

        test("unary plus operator adds days") {
            val result = WeekdayBuilder().apply {
                +DayOfWeek.MONDAY
                +DayOfWeek.FRIDAY
            }.build()

            result shouldContainExactlyInAnyOrder setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        }

        test("add function adds day") {
            val result = WeekdayBuilder().apply {
                add(DayOfWeek.TUESDAY)
                add(DayOfWeek.THURSDAY)
            }.build()

            result shouldContainExactlyInAnyOrder setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
        }

        test("addAll function adds multiple days") {
            val result = WeekdayBuilder().apply {
                addAll(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            }.build()

            result shouldContainExactlyInAnyOrder setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }

        test("weekdays adds Monday through Friday") {
            val result = WeekdayBuilder().apply {
                weekdays()
            }.build()

            result shouldContainExactlyInAnyOrder setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            )
        }

        test("weekends adds Saturday and Sunday") {
            val result = WeekdayBuilder().apply {
                weekends()
            }.build()

            result shouldContainExactlyInAnyOrder setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }

        test("allDays adds all seven days") {
            val result = WeekdayBuilder().apply {
                allDays()
            }.build()

            result shouldContainExactlyInAnyOrder DayOfWeek.entries.toSet()
        }

        test("mixed methods work together") {
            val result = WeekdayBuilder().apply {
                +DayOfWeek.MONDAY
                weekends()
            }.build()

            result shouldContainExactlyInAnyOrder setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            )
        }

        test("deduplicates entries") {
            val result = WeekdayBuilder().apply {
                +DayOfWeek.MONDAY
                +DayOfWeek.MONDAY
                add(DayOfWeek.MONDAY)
            }.build()

            result shouldBe setOf(DayOfWeek.MONDAY)
        }

        test("empty builder produces empty set") {
            val result = WeekdayBuilder().build()

            result shouldBe emptySet()
        }
    })
