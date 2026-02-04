package com.yonatankarp.ff4k.core

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

// ============================================================================
// Immutable Builder Extensions
// ============================================================================

/**
 * Creates a new context with an additional parameter.
 * Does not modify the original context.
 *
 * Example:
 * ```kotlin
 * val context = FlippingExecutionContext(ContextKeys.USER_ID to "123")
 * val withRegion = context.withParameter(ContextKeys.REGION, "EU")
 * // context is unchanged, withRegion has both userId and region
 * ```
 *
 * @param key The parameter key
 * @param value The parameter value
 * @return A new [FlippingExecutionContext] with the added parameter
 */
fun FlippingExecutionContext.withParameter(key: String, value: Any): FlippingExecutionContext = FlippingExecutionContext((values + (key to value)))

/**
 * Creates a new context with additional parameters.
 * Does not modify the original context.
 *
 * Example:
 * ```kotlin
 * val context = FlippingExecutionContext()
 * val populated = context.withParameters(
 *     ContextKeys.USER_ID to "123",
 *     ContextKeys.REGION to "EU",
 *     "tier" to "premium"
 * )
 * ```
 *
 * @param pairs The parameters to add
 * @return A new [FlippingExecutionContext] with the added parameters
 */
fun FlippingExecutionContext.withParameters(vararg pairs: Pair<String, Any>): FlippingExecutionContext = FlippingExecutionContext((values + pairs.toMap()))

/**
 * Merges this context with another, with the other context's values taking precedence.
 * Does not modify either original context.
 *
 * Note: This is intentionally not an operator function to avoid conflict with
 * [CoroutineContext.plus] which is inherited via [CoroutineContext.Element].
 *
 * Example:
 * ```kotlin
 * val base = FlippingExecutionContext(ContextKeys.ENVIRONMENT to "prod", ContextKeys.REGION to "US")
 * val override = FlippingExecutionContext(ContextKeys.REGION to "EU")
 * val merged = base.mergeWith(override)
 * // merged has env=prod, region=EU
 * ```
 *
 * @param other The context to merge with
 * @return A new [FlippingExecutionContext] with merged parameters
 */
fun FlippingExecutionContext.mergeWith(other: FlippingExecutionContext): FlippingExecutionContext = FlippingExecutionContext((values + other.values).toMutableMap())

// ============================================================================
// Coroutine Context Propagation
// ============================================================================

/**
 * Execute a block with the given [FlippingExecutionContext].
 *
 * The context is available to all suspend calls within the block via [currentFlippingContext].
 * When the block completes (normally or exceptionally), the previous context is automatically restored.
 *
 * Contexts can be nested - inner contexts completely replace outer ones:
 * ```kotlin
 * withFlippingContext(FlippingExecutionContext(ContextKeys.ENVIRONMENT to "prod")) {
 *     // environment = "prod"
 *     withFlippingContext(FlippingExecutionContext(ContextKeys.ENVIRONMENT to "staging")) {
 *         // environment = "staging" (replaced, not merged)
 *     }
 *     // environment = "prod" (restored)
 * }
 * ```
 *
 * For merging behavior, use [withFlippingParameters] instead.
 *
 * @param context The context to use within the block
 * @param block The suspending block to execute
 * @return The result of the block
 */
suspend inline fun <T> withFlippingContext(
    context: FlippingExecutionContext,
    crossinline block: suspend () -> T,
): T = withContext(context) { block() }

/**
 * Execute a block with additional parameters merged into the current context.
 *
 * If no context exists in the current coroutine scope, creates a new one with the given parameters.
 * If a context exists, merges the new parameters (new values override existing ones).
 *
 * Example:
 * ```kotlin
 * withFlippingContext(FlippingExecutionContext(ContextKeys.USER_ID to "123", "tier" to "free")) {
 *     // userId=123, tier=free
 *
 *     withFlippingParameters("tier" to "premium", ContextKeys.REGION to "EU") {
 *         // userId=123, tier=premium, region=EU (merged)
 *     }
 *
 *     // userId=123, tier=free (restored)
 * }
 * ```
 *
 * @param parameters The parameters to add/override
 * @param block The suspending block to execute
 * @return The result of the block
 */
suspend inline fun <T> withFlippingParameters(
    vararg parameters: Pair<String, Any>,
    crossinline block: suspend () -> T,
): T {
    val current = currentFlippingContext()
    val merged = current.withParameters(*parameters)
    return withContext(merged) { block() }
}

/**
 * Retrieve the current [FlippingExecutionContext] from the coroutine context.
 *
 * Returns an empty context if none has been set via [withFlippingContext] or [withFlippingParameters].
 *
 * Example:
 * ```kotlin
 * suspend fun myFunction() {
 *     val context = currentFlippingContext()
 *     val userId: String? = context[ContextKeys.USER_ID]
 *     // ...
 * }
 * ```
 *
 * @return The current [FlippingExecutionContext], or an empty context if none is set
 */
suspend fun currentFlippingContext(): FlippingExecutionContext = currentCoroutineContext()[FlippingExecutionContext] ?: FlippingExecutionContext()

/**
 * Retrieves a required parameter from the context, throwing if not present.
 *
 * Use this when a parameter is mandatory for your strategy or feature flag evaluation.
 * Provides a descriptive error message indicating which parameter is missing.
 *
 * Example:
 * ```kotlin
 * class MyStrategy : FlippingStrategy {
 *     override fun evaluate(context: FlippingExecutionContext): Boolean {
 *         val userId: String = context.getOrThrow(ContextKeys.USER_ID)
 *         // userId is guaranteed to be non-null here
 *         return userId.startsWith("premium_")
 *     }
 * }
 * ```
 *
 * @param key The parameter key to retrieve
 * @return The value associated with the key, cast to type [T]
 * @throws IllegalStateException if the key is not present in the context
 */
inline fun <reified T> FlippingExecutionContext.getOrThrow(key: String): T = get<T>(key) ?: error("To work with ${this::class.simpleName} you must provide '$key' parameter in execution context")
