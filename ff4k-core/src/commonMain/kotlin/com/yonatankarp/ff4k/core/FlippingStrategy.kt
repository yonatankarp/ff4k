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
interface FlippingStrategy {
    /**
     * Configuration parameters for this strategy.
     *
     * These parameters are provided during strategy initialization and define the behavior
     * of the strategy. For example:
     * - A percentage-based strategy might have `{"percentage": "25"}`
     * - A region-based strategy might have `{"regions": "US,EU,APAC"}`
     * - A time-based strategy might have `{"startTime": "09:00", "endTime": "17:00"}`
     */
    val initParams: Map<String, String>

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
    // TODO - will be implemented in Phase 2
    suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean = true
}
