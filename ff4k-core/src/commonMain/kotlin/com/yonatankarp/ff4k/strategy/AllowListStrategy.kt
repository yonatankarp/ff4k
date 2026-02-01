package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature only for users in an allowed list.
 *
 * Requires [ContextKeys.USER_ID] to be set in the [FlippingExecutionContext].
 * Returns `false` if no user ID is present or the list is empty.
 *
 * @property allowedList The set of user IDs for whom the feature is enabled.
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

        val userId = context.get<String>(ContextKeys.USER_ID) ?: return false
        return userId in allowedList
    }
}
