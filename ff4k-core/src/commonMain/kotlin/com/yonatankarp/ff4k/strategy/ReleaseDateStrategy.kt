package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A strategy that enables a feature after a specified release date.
 *
 * The feature is disabled before the release date and enabled once the
 * current time reaches or passes the release date.
 *
 * This strategy is useful for scheduling feature launches at a specific
 * point in time without requiring a deployment or manual toggle.
 *
 * @property releaseDate The instant after which the feature becomes enabled.
 * @property clock The clock used to determine the current time. Defaults to [Clock.System].
 *   This parameter is marked as [Transient] and is not included in serialization.
 *   It should only be injected for testing purposes; in production, the default
 *   [Clock.System] will always be used (including when deserializing from JSON).
 * @see DateRangeStrategy for enabling a feature within a specific time window
 */
@Serializable
@SerialName("releaseDate")
data class ReleaseDateStrategy(
    val releaseDate: Instant,
    @Transient private val clock: Clock = Clock.System,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val now = clock.now()
        return now >= releaseDate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ReleaseDateStrategy

        return releaseDate == other.releaseDate
    }

    override fun hashCode(): Int = releaseDate.hashCode()
}
