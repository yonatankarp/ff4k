package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FlippingStrategy
import com.yonatankarp.ff4k.serialization.TimeZoneSerializer
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant

/** Active from [releaseDate] onwards. */
@Serializable
@SerialName("releaseDate")
data class ReleaseDateStrategy(val releaseDate: Instant) : FlippingStrategy {
    @Transient
    internal var clock: Clock = Clock.System

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = clock.now() >= releaseDate
}

/** Active from [start] (inclusive) until [end] (exclusive). */
@Serializable
@SerialName("dateRange")
data class DateRangeStrategy(
    val start: Instant,
    val end: Instant,
) : FlippingStrategy {
    init {
        require(start <= end) { "end ($end) must not be before start ($start)" }
    }

    @Transient
    internal var clock: Clock = Clock.System

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = clock.now() in start..<end
}

/** Active every day from [startHour] (inclusive) until [endHour] (exclusive) in [timezone]. */
@Serializable
@SerialName("dailyHours")
data class DailyHoursStrategy(
    val startHour: Int,
    val endHour: Int,
    @Serializable(with = TimeZoneSerializer::class)
    val timezone: TimeZone = TimeZone.UTC,
) : FlippingStrategy {
    init {
        require(startHour in 0..23) { "startHour must be between 0 and 23, got: $startHour" }
        require(endHour in 1..24) { "endHour must be between 1 and 24, got: $endHour" }
        require(endHour > startHour) { "endHour ($endHour) must be after startHour ($startHour)" }
    }

    @Transient
    internal var clock: Clock = Clock.System

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = clock.now().toLocalDateTime(timezone).hour in startHour..<endHour
}

/** Active on the given [days] of the week in [timezone]. */
@Serializable
@SerialName("weekday")
data class WeekdayStrategy(
    val days: Set<DayOfWeek>,
    @Serializable(with = TimeZoneSerializer::class)
    val timezone: TimeZone = TimeZone.UTC,
) : FlippingStrategy {
    @Transient
    internal var clock: Clock = Clock.System

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = clock.now().toLocalDateTime(timezone).dayOfWeek in days
}
