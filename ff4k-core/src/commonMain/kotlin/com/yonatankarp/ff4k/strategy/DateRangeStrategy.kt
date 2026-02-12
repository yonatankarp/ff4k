package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A strategy that enables a feature only within a specified time range.
 *
 * The feature is enabled when the current time is at or after [startDate]
 * and before [endDate] (i.e., the range is `[startDate, endDate)`).
 *
 * This strategy is useful for:
 * - Limited-time promotions or events
 * - Scheduled maintenance windows
 * - Time-boxed experiments
 *
 * @property startDate The instant from which the feature becomes enabled (inclusive).
 * @property endDate The instant at which the feature becomes disabled (exclusive).
 * @property clock The clock used to determine the current time. Defaults to [Clock.System].
 *   This parameter is marked as [Transient] and is not included in serialization.
 *   It should only be injected for testing purposes; in production, the default
 *   [Clock.System] will always be used (including when deserializing from JSON).
 * @see ReleaseDateStrategy for enabling a feature after a single date
 */
@Serializable
@SerialName("dateRange")
data class DateRangeStrategy(
    val startDate: Instant,
    val endDate: Instant,
    @Transient private val clock: Clock = Clock.System,
) : FlippingStrategy {

    init {
        require(startDate <= endDate) { "endDate ($endDate) must be after startDate ($startDate) in DateRangeStrategy" }
    }

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val now = clock.now()
        return now in startDate..<endDate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DateRangeStrategy

        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        return result
    }
}
