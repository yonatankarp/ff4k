package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that disables a feature for users in a deny list.
 *
 * Requires [ContextKeys.USER_ID] to be set in the [FlippingExecutionContext].
 * Returns `true` if no user ID is present (allow by default) or if the user is not in the deny list.
 *
 * @property denyList The set of user IDs for whom the feature is disabled.
 */
@Serializable
@SerialName("denyList")
data class DenyListStrategy(
    val denyList: Set<String>,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        if (denyList.isEmpty()) return true

        val userId = context.get<String>(ContextKeys.USER_ID) ?: return true

        return userId !in denyList
    }
}
