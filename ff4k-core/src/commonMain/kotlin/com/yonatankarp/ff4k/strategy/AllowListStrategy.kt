package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature only for users in an allowed list.
 *
 * Requires [ContextKeys.USER_ID] to be set in the [FlippingExecutionContext].
 * Throws [IllegalStateException] if no user ID is present.
 * Returns `false` if the list is empty.
 *
 * @property allowedList The set of user IDs for whom the feature is enabled.
 * @throws IllegalStateException if [ContextKeys.USER_ID] is not present in the context
 */
@Serializable
@SerialName("allowList")
data class AllowListStrategy(
    val allowedList: Set<String>,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        if (allowedList.isEmpty()) return false

        val userId = context.getOrThrow<String>(ContextKeys.USER_ID)
        return userId in allowedList
    }
}
