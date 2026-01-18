package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.FlippingExecutionContext

/**
 * Extension functions for [FF4k] providing convenient operations for conditional
 * execution and batch feature checks.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */

// ============================================================================
// Conditional Execution Extensions
// ============================================================================

/**
 * Execute a block if the feature is enabled.
 *
 * Returns the block result if the feature is enabled, `null` otherwise.
 * Useful for conditional execution with a result value.
 *
 * Example:
 * ```kotlin
 * val result = ff4k.ifEnabled("new-algorithm") {
 *     runNewAlgorithm()
 * }
 *
 * // With execution context
 * val result = ff4k.ifEnabled("personalized-ui", context) {
 *     buildPersonalizedUI(userId)
 * }
 * ```
 *
 * @param T the return type of the block
 * @param featureId the unique identifier of the feature to check
 * @param executionContext optional explicit context for strategy evaluation
 * @param block the block to execute if the feature is enabled
 * @return the block result if the feature is enabled, `null` otherwise
 */
suspend inline fun <T> FF4k.ifEnabled(
    featureId: String,
    executionContext: FlippingExecutionContext? = null,
    block: () -> T,
): T? = if (check(featureId, executionContext)) block() else null

/**
 * Execute one block if the feature is enabled, another if disabled.
 *
 * Returns the result of the appropriate block based on the feature state.
 * Useful for A/B style feature flags where both paths produce a result.
 *
 * Example:
 * ```kotlin
 * val result = ff4k.ifEnabledOrElse("feature",
 *     enabled = { runNewCode() },
 *     disabled = { runOldCode() }
 * )
 *
 * // With execution context
 * val price = ff4k.ifEnabledOrElse("dynamic-pricing", context,
 *     enabled = { calculateDynamicPrice(userId) },
 *     disabled = { getStaticPrice() }
 * )
 * ```
 *
 * @param T the return type of both blocks (must be the same)
 * @param featureId the unique identifier of the feature to check
 * @param executionContext optional explicit context for strategy evaluation
 * @param enabled the block to execute if the feature is enabled
 * @param disabled the block to execute if the feature is disabled
 * @return the result of the executed block
 */
suspend inline fun <T> FF4k.ifEnabledOrElse(
    featureId: String,
    executionContext: FlippingExecutionContext? = null,
    enabled: () -> T,
    disabled: () -> T,
): T = if (check(featureId, executionContext)) enabled() else disabled()

/**
 * Execute a block with side effects if the feature is enabled.
 *
 * Returns [Unit], useful for side-effect operations where no return value is needed.
 * This is semantically clearer than [ifEnabled] when the block result is not used.
 *
 * Example:
 * ```kotlin
 * ff4k.whenEnabled("logging") {
 *     logger.info("Feature is active")
 * }
 *
 * // With execution context
 * ff4k.whenEnabled("analytics", context) {
 *     trackEvent("page_view", userId)
 * }
 * ```
 *
 * @param featureId the unique identifier of the feature to check
 * @param executionContext optional explicit context for strategy evaluation
 * @param block the side-effect block to execute if the feature is enabled
 */
suspend inline fun FF4k.whenEnabled(
    featureId: String,
    executionContext: FlippingExecutionContext? = null,
    block: () -> Unit,
) {
    if (check(featureId, executionContext)) {
        block()
    }
}

// ============================================================================
// Batch Check Extensions
// ============================================================================

/**
 * Check if all features in the list are enabled.
 *
 * Returns `true` only if every specified feature is enabled.
 * Useful for gating functionality that requires multiple features to be active.
 *
 * Example:
 * ```kotlin
 * if (ff4k.checkAll("feature1", "feature2", "feature3")) {
 *     // All features are enabled
 *     enableAdvancedMode()
 * }
 *
 * // With execution context
 * if (ff4k.checkAll("premium", "beta", executionContext = context)) {
 *     showPremiumBetaFeatures()
 * }
 * ```
 *
 * @param featureIds the unique identifiers of the features to check
 * @param executionContext optional explicit context for strategy evaluation
 * @return `true` if all features are enabled, `false` if any is disabled
 */
suspend fun FF4k.checkAll(
    vararg featureIds: String,
    executionContext: FlippingExecutionContext? = null,
): Boolean = featureIds.all { check(it, executionContext) }

/**
 * Check if any feature in the list is enabled.
 *
 * Returns `true` if at least one of the specified features is enabled.
 * Useful for OR-style feature gating where any of several flags enables functionality.
 *
 * Example:
 * ```kotlin
 * if (ff4k.checkAny("feature1", "feature2", "feature3")) {
 *     // At least one feature is enabled
 *     showFeatureSection()
 * }
 *
 * // With execution context
 * if (ff4k.checkAny("admin", "moderator", "power-user", executionContext = context)) {
 *     showModeratorTools()
 * }
 * ```
 *
 * @param featureIds the unique identifiers of the features to check
 * @param executionContext optional explicit context for strategy evaluation
 * @return `true` if any feature is enabled, `false` if all are disabled
 */
suspend fun FF4k.checkAny(
    vararg featureIds: String,
    executionContext: FlippingExecutionContext? = null,
): Boolean = featureIds.any { check(it, executionContext) }
