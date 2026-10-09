package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Active when the context value under [key] is one of [allowed]. Combine with [not] for a deny list.
 * A missing key evaluates to false.
 */
@Serializable
@SerialName("contextFilter")
data class ContextFilterStrategy(
    val key: String,
    val allowed: Set<String>,
) : FlippingStrategy {
    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = context[key]?.toString() in allowed
}
