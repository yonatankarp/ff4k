package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature for a consistent percentage of users.
 *
 * Unlike [PonderationStrategy], this strategy is deterministic per user -
 * the same user will always get the same result. Users are bucketed based
 * on their user ID hash.
 *
 * Requires [ContextKeys.USER_ID] to be set in the [FlippingExecutionContext].
 * Throws [IllegalStateException] if no user ID is present.
 *
 * @property weight The percentage of users (0.0 to 1.0) for whom the feature is enabled.
 * @throws IllegalStateException if [ContextKeys.USER_ID] is not present in the context
 * @see PonderationStrategy
 */
@Serializable
@SerialName("userPonderation")
data class UserPonderationStrategy(
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
    ): Boolean {
        val userId = context.getOrThrow<String>(ContextKeys.USER_ID)

        val bucket = (userId.hash() and 0x7FFFFFFF) % 100 // 0-99
        val threshold = (weight * 100).toInt()

        return bucket < threshold
    }

    private fun String.hash(): Int {
        var h = 0
        for (c in this) {
            h = 31 * h + c.code
        }
        return h
    }

    companion object {
        private const val HALF = 0.5
    }
}
