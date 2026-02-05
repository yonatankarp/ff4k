package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy

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
 * @author Yonatan Karp-Rudin
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
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.denyListStrategy(block: ListBuilder.() -> Unit) {
    val list = ListBuilder().apply(block).build()
    flippingStrategy = DenyListStrategy(list)
}
