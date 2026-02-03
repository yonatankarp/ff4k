package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A strategy that enables a feature only during specific hours of the day.
 *
 * The feature is enabled when the current hour (in the specified timezone) is
 * within the range `[startHour, endHour)` (start inclusive, end exclusive).
 *
 * This strategy is useful for:
 * - Business hours availability (e.g., 9 AM to 5 PM)
 * - Off-peak processing windows
 * - Time-restricted features
 *
 * @property startHour The hour when the feature becomes enabled (0-23, inclusive).
 * @property endHour The hour when the feature becomes disabled (1-24, inclusive).
 * @property timezone The timezone used to determine the current hour. Defaults to [TimeZone.UTC].
 * @property clock The clock used to determine the current time. Defaults to [Clock.System].
 *   This parameter is marked as [Transient] and is not included in serialization.
 *   It should only be injected for testing purposes; in production, the default
 *   [Clock.System] will always be used (including when deserializing from JSON).
 * @throws IllegalArgumentException if startHour is not in 0..23, endHour is not in 1..24,
 *   or endHour is not greater than startHour.
 * @see WeekdayStrategy for day-of-week based enabling
 * @see DateRangeStrategy for absolute time range enabling
 */
@Serializable
@SerialName("dailyHours")
data class DailyHoursStrategy(
    val startHour: Int,
    val endHour: Int,
    val timezone: TimeZone = TimeZone.UTC,
    @Transient private val clock: Clock = Clock.System,
) : FlippingStrategy {

    init {
        require(startHour in 0..23) {
            "Start hour must be between 0 and 23, got: $startHour"
        }
        require(endHour in 1..24) {
            "End hour must be between 1 and 24, got: $endHour"
        }
        require(endHour > startHour) {
            "End hour must be after start hour. Start: $startHour, End: $endHour"
        }
    }

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val now = clock.now()
        val localTime = now.toLocalDateTime(timezone)
        val currentHour = localTime.hour

        return currentHour in startHour..<endHour
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DailyHoursStrategy

        if (startHour != other.startHour) return false
        if (endHour != other.endHour) return false
        if (timezone != other.timezone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startHour
        result = 31 * result + endHour
        result = 31 * result + timezone.hashCode()
        return result
    }
}
