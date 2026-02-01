package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that always evaluates to `true`.
 *
 * Useful as a base case in composite strategies or for testing.
 *
 * @see AlwaysFalseFlippingStrategy
 */
@Serializable
@SerialName("alwaysTrue")
data object AlwaysTrueFlippingStrategy : FlippingStrategy {

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean = true
}

/**
 * A strategy that always evaluates to `false`.
 *
 * Useful as a base case in composite strategies or for testing.
 *
 * @see AlwaysTrueFlippingStrategy
 */
@Serializable
@SerialName("alwaysFalse")
data object AlwaysFalseFlippingStrategy : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean = false
}
