package com.yonatankarp.ff4k.dsl

import com.yonatankarp.ff4k.core.FlippingStrategy

/**
 * Convenience function to enable the feature.
 * Sets [FeatureBuilder.isEnabled] to `true`.
 *
 * Example:
 * ```kotlin
 * feature("my-feature") {
 *     enable()
 * }
 * ```
 */
fun FeatureBuilder.enable() {
    isEnabled = true
}

/**
 * Convenience function to disable the feature.
 * Sets [FeatureBuilder.isEnabled] to `false`.
 *
 * Example:
 * ```kotlin
 * feature("my-feature") {
 *     disable()
 * }
 * ```
 */
fun FeatureBuilder.disable() {
    isEnabled = false
}

/**
 * Convenience function to assign the feature to a group.
 *
 * Example:
 * ```kotlin
 * feature("my-feature") {
 *     inGroup("payment-features")
 * }
 * ```
 *
 * @param groupName The name of the group
 */
fun FeatureBuilder.inGroup(groupName: String) {
    group = groupName
}

/**
 * Convenience function to set the flipping strategy.
 *
 * Example:
 * ```kotlin
 * feature("my-feature") {
 *     strategy(PercentageBasedStrategy(50))
 * }
 * ```
 *
 * @param strategy The flipping strategy to use
 */
fun FeatureBuilder.strategy(strategy: FlippingStrategy) {
    flippingStrategy = strategy
}
