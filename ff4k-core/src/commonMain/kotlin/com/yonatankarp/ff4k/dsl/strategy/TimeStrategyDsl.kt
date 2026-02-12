package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * Configures a [ReleaseDateStrategy] for this feature using an [Instant].
 *
 * The feature will be disabled before the release date and enabled once
 * the current time reaches or passes the release date.
 *
 * ## Example
 *
 * ```kotlin
 * feature("new-feature") {
 *     releaseDateStrategy(Instant.parse("2025-01-01T00:00:00Z"))
 * }
 * ```
 *
 * @param releaseDate The instant after which the feature becomes enabled.
 * @see dateRangeStrategy for enabling a feature within a time window
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.releaseDateStrategy(releaseDate: Instant) {
    flippingStrategy = ReleaseDateStrategy(releaseDate)
}

/**
 * Configures a [ReleaseDateStrategy] for this feature using an ISO-8601 date string.
 *
 * The feature will be disabled before the release date and enabled once
 * the current time reaches or passes the release date.
 *
 * ## Example
 *
 * ```kotlin
 * feature("new-feature") {
 *     releaseDateStrategy("2025-01-01T00:00:00Z")
 * }
 * ```
 *
 * @param date The release date in ISO-8601 format (e.g., "2025-01-01T00:00:00Z").
 * @throws IllegalArgumentException if the date string is not valid ISO-8601 format.
 * @see dateRangeStrategy for enabling a feature within a time window
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.releaseDateStrategy(date: String) {
    flippingStrategy = ReleaseDateStrategy(Instant.parse(date))
}

/**
 * Configures a [ReleaseDateStrategy] for this feature using a [LocalDateTime] and timezone.
 *
 * The feature will be disabled before the release date and enabled once
 * the current time reaches or passes the release date.
 *
 * ## Example
 *
 * ```kotlin
 * feature("new-feature") {
 *     releaseDateStrategy(
 *         dateTime = LocalDateTime(2025, 1, 1, 0, 0),
 *         timezone = TimeZone.of("America/New_York")
 *     )
 * }
 * ```
 *
 * @param dateTime The local date and time of the release.
 * @param timezone The timezone to interpret the local date time. Defaults to UTC.
 * @see dateRangeStrategy for enabling a feature within a time window
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.releaseDateStrategy(
    dateTime: LocalDateTime,
    timezone: TimeZone = TimeZone.UTC,
) {
    flippingStrategy = ReleaseDateStrategy(dateTime.toInstant(timezone))
}

/**
 * Configures a [DateRangeStrategy] for this feature using [Instant] values.
 *
 * The feature will be enabled only when the current time is within the
 * specified range: `[startDate, endDate)` (start inclusive, end exclusive).
 *
 * ## Example
 *
 * ```kotlin
 * feature("holiday-sale") {
 *     dateRangeStrategy(
 *         startDate = Instant.parse("2025-12-20T00:00:00Z"),
 *         endDate = Instant.parse("2025-12-26T00:00:00Z")
 *     )
 * }
 * ```
 *
 * @param startDate The instant from which the feature becomes enabled (inclusive).
 * @param endDate The instant at which the feature becomes disabled (exclusive).
 * @see releaseDateStrategy for enabling a feature after a single date
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.dateRangeStrategy(startDate: Instant, endDate: Instant) {
    flippingStrategy = DateRangeStrategy(startDate, endDate)
}

/**
 * Configures a [DateRangeStrategy] for this feature using ISO-8601 date strings.
 *
 * The feature will be enabled only when the current time is within the
 * specified range: `[startDate, endDate)` (start inclusive, end exclusive).
 *
 * ## Example
 *
 * ```kotlin
 * feature("holiday-sale") {
 *     dateRangeStrategy(
 *         startDate = "2025-12-20T00:00:00Z",
 *         endDate = "2025-12-26T00:00:00Z"
 *     )
 * }
 * ```
 *
 * @param startDate The start date in ISO-8601 format (e.g., "2025-01-01T00:00:00Z").
 * @param endDate The end date in ISO-8601 format (e.g., "2025-01-31T23:59:59Z").
 * @throws IllegalArgumentException if either date string is not valid ISO-8601 format.
 * @see releaseDateStrategy for enabling a feature after a single date
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.dateRangeStrategy(startDate: String, endDate: String) {
    flippingStrategy = DateRangeStrategy(
        startDate = Instant.parse(startDate),
        endDate = Instant.parse(endDate),
    )
}

/**
 * Configures a [DateRangeStrategy] for this feature using [LocalDateTime] values and timezone.
 *
 * The feature will be enabled only when the current time is within the
 * specified range: `[startDate, endDate)` (start inclusive, end exclusive).
 *
 * ## Example
 *
 * ```kotlin
 * feature("holiday-sale") {
 *     dateRangeStrategy(
 *         startDate = LocalDateTime(2025, 12, 20, 0, 0),
 *         endDate = LocalDateTime(2025, 12, 26, 0, 0),
 *         timezone = TimeZone.of("America/New_York")
 *     )
 * }
 * ```
 *
 * @param startDate The local date and time when the feature becomes enabled.
 * @param endDate The local date and time when the feature becomes disabled.
 * @param timezone The timezone to interpret the local date times. Defaults to UTC.
 * @see releaseDateStrategy for enabling a feature after a single date
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.dateRangeStrategy(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    timezone: TimeZone = TimeZone.UTC,
) {
    flippingStrategy = DateRangeStrategy(
        startDate = startDate.toInstant(timezone),
        endDate = endDate.toInstant(timezone),
    )
}

/**
 * Configures a [DailyHoursStrategy] for this feature.
 *
 * The feature will be enabled only during the specified hours of the day.
 * Hours are evaluated in the specified timezone.
 *
 * ## Example
 *
 * ```kotlin
 * feature("business-hours-only") {
 *     dailyHoursStrategy(
 *         startHour = 9,
 *         endHour = 17,
 *         timeZone = TimeZone.of("America/New_York")
 *     )
 * }
 * ```
 *
 * @param startHour The hour when the feature becomes enabled (0-23, inclusive).
 * @param endHour The hour when the feature becomes disabled (1-24, exclusive).
 * @param timezone The timezone to use for hour calculations. Defaults to UTC.
 * @throws IllegalArgumentException if startHour is not in 0..23, endHour is not in 1..24,
 *         or endHour is not greater than startHour.
 * @see weekdayStrategy for day-of-week based enabling
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.dailyHoursStrategy(
    startHour: Int,
    endHour: Int,
    timezone: TimeZone = TimeZone.UTC,
) {
    flippingStrategy = DailyHoursStrategy(startHour, endHour, timezone)
}

/**
 * Configures a [WeekdayStrategy] for this feature using a DSL builder.
 *
 * The feature will be enabled only on the specified days of the week.
 * Days are evaluated in the specified timezone.
 *
 * ## Example
 *
 * ```kotlin
 * feature("weekday-only") {
 *     weekdayStrategy(TimeZone.of("Europe/London")) {
 *         +DayOfWeek.MONDAY
 *         +DayOfWeek.TUESDAY
 *         +DayOfWeek.WEDNESDAY
 *         +DayOfWeek.THURSDAY
 *         +DayOfWeek.FRIDAY
 *     }
 * }
 * ```
 *
 * @param timezone The timezone to use for day calculations. Defaults to UTC.
 * @param block A DSL block to configure the allowed days of the week.
 * @see dailyHoursStrategy for hour-based enabling
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.weekdayStrategy(
    timezone: TimeZone = TimeZone.UTC,
    block: WeekdayBuilder.() -> Unit,
) {
    val days = WeekdayBuilder().apply(block).build()
    flippingStrategy = WeekdayStrategy(days, timezone)
}
