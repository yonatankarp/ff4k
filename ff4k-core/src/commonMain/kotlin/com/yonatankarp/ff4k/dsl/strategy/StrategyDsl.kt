package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.core.FF4kDsl
import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy
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

/**
 * Configures an [AllowListStrategy] for this feature.
 *
 * Only users whose ID is in the allow list will have the feature enabled.
 * Requires `userId` to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("vip-feature") {
 *     allowListStrategy {
 *         +"user-123"
 *         +"user-456"
 *         add("user-789")
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the list of allowed user IDs.
 * @see denyListStrategy for the inverse behavior
 */
fun FeatureBuilder.allowListStrategy(block: ListBuilder.() -> Unit) {
    val list = ListBuilder().apply(block).build()
    flippingStrategy = AllowListStrategy(list)
}

/**
 * Configures a [DenyListStrategy] for this feature.
 *
 * Users whose ID is in the deny list will have the feature disabled;
 * all other users will have it enabled. Requires `userId` to be set in the
 * [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("new-ui") {
 *     denyListStrategy {
 *         +"problematic-user-1"
 *         +"problematic-user-2"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the list of denied user IDs.
 * @see allowListStrategy for the inverse behavior
 */
fun FeatureBuilder.denyListStrategy(block: ListBuilder.() -> Unit) {
    val list = ListBuilder().apply(block).build()
    flippingStrategy = DenyListStrategy(list)
}

/**
 * DSL builder for constructing a set of identifiers used by list-based strategies.
 *
 * Provides multiple ways to add identifiers:
 * - Unary plus operator: `+"identifier"`
 * - [add] function: `add("identifier")`
 * - [addAll] function: `addAll("id1", "id2", "id3")`
 *
 * @see allowListStrategy
 * @see denyListStrategy
 */
@FF4kDsl
class ListBuilder {
    private val identifiers = mutableSetOf<String>()

    /**
     * Adds this string as an identifier to the list.
     *
     * ## Example
     *
     * ```kotlin
     * allowListStrategy {
     *     +"user-123"
     *     +"user-456"
     * }
     * ```
     */
    operator fun String.unaryPlus() {
        identifiers.add(this)
    }

    /**
     * Adds an identifier to the list.
     *
     * @param identifier The identifier to add.
     */
    fun add(identifier: String) {
        identifiers.add(identifier)
    }

    /**
     * Adds multiple identifiers to the list.
     *
     * @param identifiers The identifiers to add.
     */
    fun addAll(vararg identifiers: String) {
        this.identifiers.addAll(identifiers)
    }

    internal fun build(): Set<String> = identifiers.toSet()
}
