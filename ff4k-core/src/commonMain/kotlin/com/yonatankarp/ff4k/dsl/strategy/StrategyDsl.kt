package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import com.yonatankarp.ff4k.strategy.UserPonderationStrategy

/**
 * Configures a [PonderationStrategy] for this feature with a decimal weight.
 *
 * Each evaluation is independent - the same user may get different results
 * on subsequent calls.
 *
 * ## Example
 *
 * ```kotlin
 * feature("gradual-rollout") {
 *     ponderationStrategy(0.25) // 25% chance of being enabled
 * }
 * ```
 *
 * @param weight The probability (0.0 to 1.0) that the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0.0..1.0
 * @see userPonderationStrategy for user-sticky behavior
 */
fun FeatureBuilder.ponderationStrategy(weight: Double) {
    flippingStrategy = PonderationStrategy(weight)
}

/**
 * Configures a [PonderationStrategy] for this feature with an integer percentage.
 *
 * Each evaluation is independent - the same user may get different results
 * on subsequent calls.
 *
 * ## Example
 *
 * ```kotlin
 * feature("gradual-rollout") {
 *     ponderationStrategy(25) // 25% chance of being enabled
 * }
 * ```
 *
 * @param weight The percentage (0 to 100) that the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0..100
 * @see userPonderationStrategy for user-sticky behavior
 */
fun FeatureBuilder.ponderationStrategy(weight: Int) {
    flippingStrategy = PonderationStrategy(weight)
}

/**
 * Configures a [UserPonderationStrategy] for this feature with a decimal weight.
 *
 * Unlike [ponderationStrategy], this strategy is deterministic per user -
 * the same user will always get the same result. Requires `userId` to be
 * set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("beta-feature") {
 *     userPonderationStrategy(0.1) // 10% of users will have access
 * }
 * ```
 *
 * @param weight The probability (0.0 to 1.0) of users for whom the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0.0..1.0
 * @see ponderationStrategy for random (non-sticky) behavior
 */
fun FeatureBuilder.userPonderationStrategy(weight: Double) {
    flippingStrategy = UserPonderationStrategy(weight)
}

/**
 * Configures a [UserPonderationStrategy] for this feature with an integer percentage.
 *
 * Unlike [ponderationStrategy], this strategy is deterministic per user -
 * the same user will always get the same result. Requires `userId` to be
 * set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("beta-feature") {
 *     userPonderationStrategy(10) // 10% of users will have access
 * }
 * ```
 *
 * @param weight The percentage (0 to 100) of users for whom the feature is enabled.
 * @throws IllegalArgumentException if weight is not in range 0..100
 * @see ponderationStrategy for random (non-sticky) behavior
 */
fun FeatureBuilder.userPonderationStrategy(weight: Int) {
    flippingStrategy = UserPonderationStrategy(weight)
}
