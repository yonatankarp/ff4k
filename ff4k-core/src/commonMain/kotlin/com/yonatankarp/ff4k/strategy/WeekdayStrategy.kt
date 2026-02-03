package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A strategy that enables a feature only on specific days of the week.
 *
 * The feature is enabled when the current day (in the specified timezone)
 * matches one of the allowed days.
 *
 * This strategy is useful for:
 * - Weekday-only features
 * - Weekend promotions
 * - Scheduled maintenance windows on specific days
 *
 * @property allowedDays The set of days on which the feature is enabled.
 * @property timezone The timezone used to determine the current day. Defaults to [TimeZone.UTC].
 * @property clock The clock used to determine the current time. Defaults to [Clock.System].
 *   This parameter is marked as [Transient] and is not included in serialization.
 *   It should only be injected for testing purposes; in production, the default
 *   [Clock.System] will always be used (including when deserializing from JSON).
 * @see DailyHoursStrategy for hour-based enabling
 * @see DateRangeStrategy for absolute time range enabling
 */
@Serializable
@SerialName("weekday")
data class WeekdayStrategy(
    val allowedDays: Set<DayOfWeek>,
    val timezone: TimeZone = TimeZone.UTC,
    @Transient private val clock: Clock = Clock.System,
) : FlippingStrategy {

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val now = clock.now()
        val localDate = now.toLocalDateTime(timezone).date
        val dayOfWeek = localDate.dayOfWeek
        return dayOfWeek in allowedDays
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as WeekdayStrategy

        if (allowedDays != other.allowedDays) return false
        if (timezone != other.timezone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = allowedDays.hashCode()
        result = 31 * result + timezone.hashCode()
        return result
    }
}
