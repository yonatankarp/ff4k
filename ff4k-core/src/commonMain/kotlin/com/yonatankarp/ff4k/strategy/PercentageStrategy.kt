package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** Active for a random [percentage] of evaluations. */
@Serializable
@SerialName("percentage")
data class PercentageStrategy(val percentage: Int) : FlippingStrategy {
    init {
        require(percentage in 0..100) { "Percentage must be between 0 and 100, got: $percentage" }
    }

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = Random.nextInt(100) < percentage
}

/**
 * Active for a stable [percentage] of users, bucketed by the context value under [key] and the feature id.
 * Each feature picks its users independently, and raising [percentage] keeps the users already active.
 */
@Serializable
@SerialName("userPercentage")
data class UserPercentageStrategy(
    val percentage: Int,
    val key: String = "userId",
) : FlippingStrategy {
    init {
        require(percentage in 0..100) { "Percentage must be between 0 and 100, got: $percentage" }
    }

    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean {
        val user = context[key]?.toString() ?: return false
        return rolloutBucket(feature.id, user) < percentage
    }
}

internal fun rolloutBucket(
    featureId: String,
    user: String,
): Int = mix(user.hashCode() xor mix(featureId.hashCode())).mod(100)

// MurmurHash3 finalizer. Hashing "feature:user" with hashCode() alone only shifts every bucket by a constant,
// which leaves rollouts of different features correlated.
private fun mix(value: Int): Int {
    var hash = value
    hash = (hash xor (hash ushr 16)) * 0x85ebca6b.toInt()
    hash = (hash xor (hash ushr 13)) * 0xc2b2ae35.toInt()
    return hash xor (hash ushr 16)
}
