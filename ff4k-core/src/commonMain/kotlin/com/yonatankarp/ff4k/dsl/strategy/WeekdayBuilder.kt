package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.core.FF4kDsl
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import kotlinx.datetime.DayOfWeek

/**
 * DSL builder for constructing a set of [DayOfWeek] used by [WeekdayStrategy].
 *
 * Provides multiple ways to add days:
 * - Unary plus operator: `+DayOfWeek.MONDAY`
 * - [add] function: `add(DayOfWeek.MONDAY)`
 * - [addAll] function: `addAll(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)`
 * - Convenience functions: [weekdays], [weekends], [allDays]
 *
 * @see weekdayStrategy
 */
@FF4kDsl
class WeekdayBuilder {
    private val days = mutableSetOf<DayOfWeek>()

    /**
     * Adds this day to the allowed days.
     *
     * ## Example
     *
     * ```kotlin
     * weekdayStrategy {
     *     +DayOfWeek.MONDAY
     *     +DayOfWeek.FRIDAY
     * }
     * ```
     */
    operator fun DayOfWeek.unaryPlus() {
        days.add(this)
    }

    /**
     * Adds a day to the allowed days.
     *
     * @param day The day to add.
     */
    fun add(day: DayOfWeek) {
        days.add(day)
    }

    /**
     * Adds multiple days to the allowed days.
     *
     * @param days The days to add.
     */
    fun addAll(vararg days: DayOfWeek) {
        this.days.addAll(days)
    }

    /**
     * Adds all weekdays (Monday through Friday) to the allowed days.
     *
     * ## Example
     *
     * ```kotlin
     * feature("workday-feature") {
     *     weekdayStrategy {
     *         weekdays() // Monday through Friday
     *     }
     * }
     * ```
     */
    fun weekdays() {
        days.addAll(
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            ),
        )
    }

    /**
     * Adds weekend days (Saturday and Sunday) to the allowed days.
     *
     * ## Example
     *
     * ```kotlin
     * feature("weekend-promo") {
     *     weekdayStrategy {
     *         weekends() // Saturday and Sunday
     *     }
     * }
     * ```
     */
    fun weekends() {
        days.addAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
    }

    /**
     * Adds all days of the week to the allowed days.
     *
     * ## Example
     *
     * ```kotlin
     * feature("always-available") {
     *     weekdayStrategy {
     *         allDays() // All seven days
     *     }
     * }
     * ```
     */
    fun allDays() {
        days.addAll(DayOfWeek.entries)
    }

    internal fun build(): Set<DayOfWeek> = days.toSet()
}
