package com.yonatankarp.ff4k.core

import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Extension functions and coroutine utilities for [FlippingExecutionContext].
 *
 * This file provides:
 * - **Immutable builder extensions**: Create modified contexts without mutating the original
 * - **Coroutine context propagation**: Automatically propagate context through suspend calls
 *
 * ## Coroutine Context Propagation
 *
 * [FlippingExecutionContext] implements [kotlin.coroutines.CoroutineContext.Element],
 * enabling automatic propagation through coroutine scopes. This replaces ThreadLocal-based
 * context storage (which doesn't work with coroutines or Kotlin Multiplatform).
 *
 * Example:
 * ```kotlin
 * withFlippingContext(FlippingExecutionContext("userId" to "user-123")) {
 *     // All suspend calls within this block can access the context
 *     ff4k.check("my-feature") // uses implicit context
 *
 *     // Nested scopes can override values
 *     withFlippingParameters("region" to "EU") {
 *         ff4k.check("eu-feature") // sees userId + region
 *     }
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */

// ============================================================================
// Immutable Builder Extensions
// ============================================================================

/**
 * Creates a new context with an additional parameter.
 * Does not modify the original context.
 *
 * Example:
 * ```kotlin
 * val context = FlippingExecutionContext("userId" to "123")
 * val withRegion = context.withParameter("region", "EU")
 * // context is unchanged, withRegion has both userId and region
 * ```
 *
 * @param key The parameter key
 * @param value The parameter value
 * @return A new [FlippingExecutionContext] with the added parameter
 */
fun FlippingExecutionContext.withParameter(key: String, value: Any?): FlippingExecutionContext = FlippingExecutionContext((values + (key to value)).toMutableMap())

/**
 * Creates a new context with additional parameters.
 * Does not modify the original context.
 *
 * Example:
 * ```kotlin
 * val context = FlippingExecutionContext()
 * val populated = context.withParameters(
 *     "userId" to "123",
 *     "region" to "EU",
 *     "tier" to "premium"
 * )
 * ```
 *
 * @param pairs The parameters to add
 * @return A new [FlippingExecutionContext] with the added parameters
 */
fun FlippingExecutionContext.withParameters(vararg pairs: Pair<String, Any?>): FlippingExecutionContext = FlippingExecutionContext((values + pairs.toMap()).toMutableMap())

/**
 * Merges this context with another, with the other context's values taking precedence.
 * Does not modify either original context.
 *
 * Note: This is intentionally not an operator function to avoid conflict with
 * [CoroutineContext.plus] which is inherited via [CoroutineContext.Element].
 *
 * Example:
 * ```kotlin
 * val base = FlippingExecutionContext("env" to "prod", "region" to "US")
 * val override = FlippingExecutionContext("region" to "EU")
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
 * withFlippingContext(FlippingExecutionContext("env" to "prod")) {
 *     // env = "prod"
 *     withFlippingContext(FlippingExecutionContext("env" to "staging")) {
 *         // env = "staging" (replaced, not merged)
 *     }
 *     // env = "prod" (restored)
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
 * withFlippingContext(FlippingExecutionContext("userId" to "123", "tier" to "free")) {
 *     // userId=123, tier=free
 *
 *     withFlippingParameters("tier" to "premium", "region" to "EU") {
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
    vararg parameters: Pair<String, Any?>,
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
 *     val userId: String? = context["userId"]
 *     // ...
 * }
 * ```
 *
 * @return The current [FlippingExecutionContext], or an empty context if none is set
 */
suspend fun currentFlippingContext(): FlippingExecutionContext = coroutineContext[FlippingExecutionContext] ?: FlippingExecutionContext()
