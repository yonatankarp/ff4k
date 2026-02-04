package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature only for specific geographic regions.
 *
 * Requires [ContextKeys.REGION] to be set in the [FlippingExecutionContext].
 * Throws [IllegalStateException] if no region is present.
 *
 * An empty [grantedRegions] set means that the feature is disabled for all regions
 * (i.e. [evaluate] will always return `false` as long as a region is present).
 *
 * @property grantedRegions The set of regions (e.g., "EU", "US", "APAC") for which the feature is enabled.
 *                           If this set is empty, no regions are granted access.
 * @throws IllegalStateException if [ContextKeys.REGION] is not present in the context
 */
@Serializable
@SerialName("region")
data class RegionFilterStrategy(
    val grantedRegions: Set<String> = emptySet(),
) : FlippingStrategy {

    constructor(vararg allowedRegions: String) : this(allowedRegions.toSet())

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val region = context.getOrThrow<String>(ContextKeys.REGION)
        return region in grantedRegions
    }
}
