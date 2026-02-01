package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingStrategy

/**
 * Combines this strategy with [other] using logical AND.
 *
 * If this strategy is already an [AndStrategy], the [other] strategy is appended
 * to its list, creating a flat structure instead of nesting.
 *
 * Example:
 * ```kotlin
 * val strategy = strategyA and strategyB and strategyC
 * // Creates AndStrategy([a, b, c]) instead of AndStrategy([AndStrategy([a, b]), c])
 * ```
 *
 * @param other The strategy to combine with.
 * @return An [AndStrategy] that evaluates to `true` only if all strategies return `true`.
 */
infix fun FlippingStrategy.and(other: FlippingStrategy): AndStrategy = when (this) {
    is AndStrategy -> copy(strategies = strategies + other)
    else -> AndStrategy(listOf(this, other))
}

/**
 * Combines this strategy with [other] using logical OR.
 *
 * If this strategy is already an [OrStrategy], the [other] strategy is appended
 * to its list, creating a flat structure instead of nesting.
 *
 * Example:
 * ```kotlin
 * val strategy = strategyA or strategyB or strategyC
 * // Creates OrStrategy([a, b, c]) instead of OrStrategy([OrStrategy([a, b]), c])
 * ```
 *
 * @param other The strategy to combine with.
 * @return An [OrStrategy] that evaluates to `true` if any strategy returns `true`.
 */
infix fun FlippingStrategy.or(other: FlippingStrategy): OrStrategy = when (this) {
    is OrStrategy -> copy(strategies = strategies + other)
    else -> OrStrategy(listOf(this, other))
}

/**
 * Negates this strategy.
 *
 * Example:
 * ```kotlin
 * val strategy = !alwaysTrueStrategy  // evaluates to false
 * ```
 *
 * @return A [NotStrategy] that inverts the evaluation result.
 */
operator fun FlippingStrategy.not(): NotStrategy = NotStrategy(this)
