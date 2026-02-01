package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * A strategy that randomly enables a feature based on a weight.
 *
 * Each evaluation is independent - the same user may get different results
 * on subsequent calls. For user-sticky behavior, use [UserPonderationStrategy].
 *
 * @property weight The probability (0.0 to 1.0) that the feature is enabled.
 * @see UserPonderationStrategy
 */
@Serializable
@SerialName("ponderation")
data class PonderationStrategy(
    val weight: Double = HALF,
) : FlippingStrategy {

    constructor(weight: Int) : this(weight / 100.0)

    init {
        require(weight in 0.0..1.0) { "Weight must be between 0.0 and 1.0, got: $weight" }
    }

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean = when {
        weight == 0.0 -> false
        weight == 1.0 -> true
        else -> Random.nextDouble() < weight
    }

    companion object {
        private const val HALF = 0.5
    }
}
