package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FlippingStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("and")
data class AndStrategy(val strategies: List<FlippingStrategy>) : FlippingStrategy {
    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = strategies.all { it.evaluate(feature, context) }
}

@Serializable
@SerialName("or")
data class OrStrategy(val strategies: List<FlippingStrategy>) : FlippingStrategy {
    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = strategies.any { it.evaluate(feature, context) }
}

@Serializable
@SerialName("not")
data class NotStrategy(val strategy: FlippingStrategy) : FlippingStrategy {
    override suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean = !strategy.evaluate(feature, context)
}

infix fun FlippingStrategy.and(other: FlippingStrategy): AndStrategy = when (this) {
    is AndStrategy -> copy(strategies = strategies + other)
    else -> AndStrategy(listOf(this, other))
}

infix fun FlippingStrategy.or(other: FlippingStrategy): OrStrategy = when (this) {
    is OrStrategy -> copy(strategies = strategies + other)
    else -> OrStrategy(listOf(this, other))
}

operator fun FlippingStrategy.not(): NotStrategy = NotStrategy(this)
