package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.strategy.ClientFilterStrategy
import com.yonatankarp.ff4k.strategy.RegionFilterStrategy
import com.yonatankarp.ff4k.strategy.ServerFilterStrategy

/**
 * Configures a [ClientFilterStrategy] for this feature.
 *
 * The feature will be enabled only for requests from the specified client
 * hostnames. Requires [com.yonatankarp.ff4k.core.ContextKeys.CLIENT_HOSTNAME]
 * to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("internal-only") {
 *     clientFilterStrategy {
 *         +"client-a.internal.com"
 *         +"client-b.internal.com"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of allowed client hostnames.
 * @see serverFilterStrategy
 * @see regionStrategy
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.clientFilterStrategy(
    block: ListBuilder.() -> Unit,
) {
    val grantedClients = ListBuilder().apply(block).build()
    flippingStrategy = ClientFilterStrategy(grantedClients)
}

/**
 * Configures a [ServerFilterStrategy] for this feature.
 *
 * The feature will be enabled only on the specified server hostnames.
 * Useful for canary deployments or instance-specific feature toggles.
 * Requires [com.yonatankarp.ff4k.core.ContextKeys.SERVER_HOSTNAME]
 * to be set in the [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("canary-feature") {
 *     serverFilterStrategy {
 *         +"server-1.prod.com"
 *         +"server-2.prod.com"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of target server hostnames.
 * @see clientFilterStrategy
 * @see regionStrategy
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.serverFilterStrategy(
    block: ListBuilder.() -> Unit,
) {
    val targetServers = ListBuilder().apply(block).build()
    flippingStrategy = ServerFilterStrategy(targetServers)
}

/**
 * Configures a [RegionFilterStrategy] for this feature.
 *
 * The feature will be enabled only for the specified geographic regions.
 * Requires [com.yonatankarp.ff4k.core.ContextKeys.REGION] to be set in the
 * [com.yonatankarp.ff4k.core.FlippingExecutionContext].
 *
 * ## Example
 *
 * ```kotlin
 * feature("eu-only") {
 *     regionStrategy {
 *         +"eu-central-1"
 *         +"eu-west-1"
 *     }
 * }
 * ```
 *
 * @param block A DSL block to configure the set of allowed regions.
 * @see clientFilterStrategy
 * @see serverFilterStrategy
 * @author Yonatan Karp-Rudin
 */
fun FeatureBuilder.regionStrategy(
    block: ListBuilder.() -> Unit,
) {
    val allowedRegions = ListBuilder().apply(block).build()
    flippingStrategy = RegionFilterStrategy(allowedRegions)
}
