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

/** Active for a stable [percentage] of users, bucketed by the context value under [key]. */
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
        return (user.hashCode() and 0x7FFFFFFF) % 100 < percentage
    }
}
