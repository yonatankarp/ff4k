package com.yonatankarp.ff4k.core

/**
 * Standard keys for [FlippingExecutionContext] parameters.
 *
 * These constants provide consistent, type-safe key names for commonly used
 * context parameters. Using these keys instead of raw strings helps prevent
 * typos and enables IDE auto-completion.
 *
 * Example usage:
 * ```kotlin
 * val context = FlippingExecutionContext(
 *     ContextKeys.USER_ID to "user-123",
 *     ContextKeys.REGION to "EU"
 * )
 *
 * val userId = context.get<String>(ContextKeys.USER_ID)
 * ```
 *
 * @see FlippingExecutionContext
 */
object ContextKeys {
    /**
     * Key for the user's unique identifier.
     * Used by strategies like [UserPonderationStrategy] for consistent user bucketing.
     */
    const val USER_ID = "userId"

    /**
     * Key for the user's display name.
     */
    const val USER_NAME = "userName"

    /**
     * Key for the geographic region (e.g., "EU", "US", "APAC").
     * Useful for region-based feature rollouts.
     */
    const val REGION = "region"

    /**
     * Key for the application environment (e.g., "production", "staging", "development").
     * Useful for environment-specific feature toggles.
     */
    const val ENVIRONMENT = "environment"
}
