package com.yonatankarp.ff4k.core

/**
 * Defines a strategy for determining whether a feature should be enabled.
 *
 * A flipping strategy encapsulates the logic that controls feature activation based on
 * runtime conditions, configuration parameters, and execution context. Common use cases include:
 * - **Gradual rollout**: Enable features for a percentage of users
 * - **Region-based**: Enable features only in specific geographical regions
 * - **Time-based**: Enable features during specific time windows
 * - **User-based**: Enable features for specific user segments or roles
 * - **Custom logic**: Any business-specific activation rules
 *
 * Example implementations:
 * ```
 * // Percentage-based rollout
 * class PercentageStrategy(override val initParams: Map<String, String>) : FlippingStrategy {
 *     override suspend fun evaluate(
 *         featureId: String,
 *         store: FeatureStore?,
 *         context: FlippingExecutionContext
 *     ): Boolean {
 *         val percentage = initParams["percentage"]?.toInt() ?: 0
 *         val userId = context.get<String>("userId") ?: return false
 *         return userId.hashCode() % 100 < percentage
 *     }
 * }
 *
 * // Region-based strategy
 * class RegionStrategy(override val initParams: Map<String, String>) : FlippingStrategy {
 *     override suspend fun evaluate(
 *         featureId: String,
 *         store: FeatureStore?,
 *         context: FlippingExecutionContext
 *     ): Boolean {
 *         val allowedRegions = initParams["regions"]?.split(",") ?: emptyList()
 *         val userRegion = context.get<String>("region") ?: return false
 *         return userRegion in allowedRegions
 *     }
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
fun interface FlippingStrategy {
    /**
     * Evaluates whether the feature should be enabled based on the execution context.
     *
     * This method contains the core decision logic for the strategy. It examines the
     * provided context and determines whether the feature identified by [featureId]
     * should be activated.
     *
     * @param featureId the unique identifier of the feature being evaluated
     * @param store optional feature store for accessing feature metadata or other features
     * @param context execution context containing runtime parameters (user info, region, etc.)
     * @return `true` if the feature should be enabled, `false` otherwise
     */
    suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean
}
