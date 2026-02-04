package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.ClientFilterStrategy
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import com.yonatankarp.ff4k.strategy.RegionFilterStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.ServerFilterStrategy
import com.yonatankarp.ff4k.strategy.UserPonderationStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Configures a [PonderationStrategy] for this feature with a decimal weight.
 *
 * Each evaluation is independent - the same user may get different results
 * on subsequent calls.
 *
 * ## Example
 *
 * ```kotlin
 * feature("gradual-rollout") {
 *     ponderationStrategy(0.25) // 25% chance of being enabled
 * }
 * ```
 *
 * @param weight The probability (0.0 to 1.0) that the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0.0..1.0
 * @see userPonderationStrategy for user-sticky behavior
 */
fun FeatureBuilder.ponderationStrategy(weight: Double) {
    flippingStrategy = PonderationStrategy(weight)
}

/**
 * Configures a [PonderationStrategy] for this feature with an integer percentage.
 *
 * Each evaluation is independent - the same user may get different results
 * on subsequent calls.
 *
 * ## Example
 *
 * ```kotlin
 * feature("gradual-rollout") {
 *     ponderationStrategy(25) // 25% chance of being enabled
 * }
 * ```
 *
 * @param weight The percentage (0 to 100) that the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0..100
 * @see userPonderationStrategy for user-sticky behavior
 */
fun FeatureBuilder.ponderationStrategy(weight: Int) {
    flippingStrategy = PonderationStrategy(weight)
}

/**
 * Configures a [UserPonderationStrategy] for this feature with a decimal weight.
 *
 * Unlike [ponderationStrategy], this strategy is deterministic per user -
 * the same user will always get the same result. Requires `userId` to be
 * set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("beta-feature") {
 *     userPonderationStrategy(0.1) // 10% of users will have access
 * }
 * ```
 *
 * @param weight The probability (0.0 to 1.0) of users for whom the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0.0..1.0
 * @see ponderationStrategy for random (non-sticky) behavior
 */
fun FeatureBuilder.userPonderationStrategy(weight: Double) {
    flippingStrategy = UserPonderationStrategy(weight)
}

/**
 * Configures a [UserPonderationStrategy] for this feature with an integer percentage.
 *
 * Unlike [ponderationStrategy], this strategy is deterministic per user -
 * the same user will always get the same result. Requires `userId` to be
 * set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("beta-feature") {
 *     userPonderationStrategy(10) // 10% of users will have access
 * }
 * ```
 *
 * @param weight The percentage (0 to 100) of users for whom the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0..100
 * @see ponderationStrategy for random (non-sticky) behavior
 */
fun FeatureBuilder.userPonderationStrategy(weight: Int) {
    flippingStrategy = UserPonderationStrategy(weight)
}

/**
 * Configures an [AllowListStrategy] for this feature.
 *
 * Only users whose ID is in the allow list will have the feature enabled.
 * Requires `userId` to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("vip-feature") {
 *     allowListStrategy {
 *         +"user-123"
 *         +"user-456"
 *         add("user-789")
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the list of allowed user IDs.
 * @see denyListStrategy for the inverse behavior
 */
fun FeatureBuilder.allowListStrategy(block: ListBuilder.() -> Unit) {
    val list = ListBuilder().apply(block).build()
    flippingStrategy = AllowListStrategy(list)
}

/**
 * Configures a [DenyListStrategy] for this feature.
 *
 * Users whose ID is in the deny list will have the feature disabled;
 * all other users will have it enabled. Requires `userId` to be set in the
 * [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("new-ui") {
 *     denyListStrategy {
 *         +"problematic-user-1"
 *         +"problematic-user-2"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the list of denied user IDs.
 * @see allowListStrategy for the inverse behavior
 */
fun FeatureBuilder.denyListStrategy(block: ListBuilder.() -> Unit) {
    val list = ListBuilder().apply(block).build()
    flippingStrategy = DenyListStrategy(list)
}

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
 */
fun FeatureBuilder.weekdayStrategy(
    timezone: TimeZone = TimeZone.UTC,
    block: WeekdayBuilder.() -> Unit,
) {
    val days = WeekdayBuilder().apply(block).build()
    flippingStrategy = WeekdayStrategy(days, timezone)
}

/**
 * Configures a [ClientFilterStrategy] for this feature.
 *
 * The feature will be enabled only for requests from the specified client
 * hostnames. Requires [com.yonatankarp.ff4k.core.ContextKeys.CLIENT_HOSTNAME]
 * to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("internal-only") {
 *     clientFilterStrategy {
 *         +"client-a.internal.com"
 *         +"client-b.internal.com"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of allowed client hostnames.
 * @see serverFilterStrategy
 * @see regionStrategy
 */
fun FeatureBuilder.clientFilterStrategy(
    block: ListBuilder.() -> Unit,
) {
    val grantedClients = ListBuilder().apply(block).build()
    flippingStrategy = ClientFilterStrategy(grantedClients)
}

/**
 * Configures a [ServerFilterStrategy] for this feature.
 *
 * The feature will be enabled only on the specified server hostnames.
 * Useful for canary deployments or instance-specific feature toggles.
 * Requires [com.yonatankarp.ff4k.core.ContextKeys.SERVER_HOSTNAME]
 * to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("canary-feature") {
 *     serverFilterStrategy {
 *         +"server-1.prod.com"
 *         +"server-2.prod.com"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of target server hostnames.
 * @see clientFilterStrategy
 * @see regionStrategy
 */
fun FeatureBuilder.serverFilterStrategy(
    block: ListBuilder.() -> Unit,
) {
    val targetServers = ListBuilder().apply(block).build()
    flippingStrategy = ServerFilterStrategy(targetServers)
}

/**
 * Configures a [RegionFilterStrategy] for this feature.
 *
 * The feature will be enabled only for the specified geographic regions.
 * Requires [com.yonatankarp.ff4k.core.ContextKeys.REGION] to be set in the
 * [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("eu-only") {
 *     regionStrategy {
 *         +"eu-central-1"
 *         +"eu-west-1"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of allowed regions.
 * @see clientFilterStrategy
 * @see serverFilterStrategy
 */
fun FeatureBuilder.regionStrategy(
    block: ListBuilder.() -> Unit,
) {
    val allowedRegions = ListBuilder().apply(block).build()
    flippingStrategy = RegionFilterStrategy(allowedRegions)
}
