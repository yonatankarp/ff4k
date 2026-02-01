package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A composite strategy that evaluates to `true` only if **all** child strategies evaluate to `true`.
 *
 * Uses short-circuit evaluation: stops evaluating as soon as any strategy returns `false`.
 *
 * Note: An empty [strategies] list evaluates to `true` (vacuous truth).
 *
 * @property strategies The list of strategies to evaluate with logical AND.
 * @see OrStrategy
 * @see NotStrategy
 */
@Serializable
@SerialName("and")
data class AndStrategy(
    val strategies: List<@Polymorphic FlippingStrategy>,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        for (strategy in strategies) {
            if (strategy.evaluate(featureId, store, context).not()) return false
        }
        return true
    }
}

/**
 * A composite strategy that evaluates to `true` if **any** child strategy evaluates to `true`.
 *
 * Uses short-circuit evaluation: stops evaluating as soon as any strategy returns `true`.
 *
 * Note: An empty [strategies] list evaluates to `false`.
 *
 * @property strategies The list of strategies to evaluate with logical OR.
 * @see AndStrategy
 * @see NotStrategy
 */
@Serializable
@SerialName("or")
data class OrStrategy(
    val strategies: List<@Polymorphic FlippingStrategy>,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        for (strategy in strategies) {
            if (strategy.evaluate(featureId, store, context)) return true
        }
        return false
    }
}

/**
 * A composite strategy that negates the result of another strategy.
 *
 * @property strategy The strategy whose result will be negated.
 * @see AndStrategy
 * @see OrStrategy
 */
@Serializable
@SerialName("not")
data class NotStrategy(
    val strategy: @Polymorphic FlippingStrategy,
) : FlippingStrategy {
    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean = strategy.evaluate(featureId, store, context).not()
}
