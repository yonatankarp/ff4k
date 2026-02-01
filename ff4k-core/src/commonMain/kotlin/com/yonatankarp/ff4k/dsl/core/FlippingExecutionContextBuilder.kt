package com.yonatankarp.ff4k.dsl.core

import com.yonatankarp.ff4k.core.FlippingExecutionContext

/**
 * A DSL builder for constructing [FlippingExecutionContext] instances.
 *
 * Provides a type-safe, expressive way to define context parameters using
 * Kotlin DSL syntax.
 *
 * Example usage:
 * ```kotlin
 * val ctx = context {
 *     "userId" to "user-123"
 *     "region" to "EU"
 *     this["tier"] = "premium"
 * }
 * ```
 *
 * @see context
 */
@FF4kDsl
class FlippingExecutionContextBuilder {
    private val values = mutableMapOf<String, Any>()

    /**
     * Adds a key-value pair to the context using infix notation.
     *
     * Example:
     * ```kotlin
     * context {
     *     "userId" to "user-123"
     * }
     * ```
     *
     * @param value The value to associate with this key
     */
    infix fun String.to(value: Any) {
        values[this] = value
    }

    /**
     * Adds a key-value pair to the context using indexed access notation.
     *
     * Example:
     * ```kotlin
     * context {
     *     this["userId"] = "user-123"
     * }
     * ```
     *
     * @param key The parameter key
     * @param value The value to associate with the key
     */
    operator fun set(key: String, value: Any) {
        values[key] = value
    }

    /**
     * Adds all entries from the given map to the context.
     *
     * @param map The map of parameters to add
     */
    fun putAll(map: Map<String, Any>) {
        values.putAll(map)
    }

    /**
     * Adds all given key-value pairs to the context.
     *
     * @param pairs The parameters to add
     */
    fun putAll(vararg pairs: Pair<String, Any>) {
        values.putAll(pairs)
    }

    /**
     * Builds and returns the [FlippingExecutionContext] with all configured parameters.
     *
     * @return A new [FlippingExecutionContext] containing all added parameters
     */
    fun build(): FlippingExecutionContext = FlippingExecutionContext(values.toMap())
}

/**
 * Creates a [FlippingExecutionContext] using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val ctx = context {
 *     "userId" to "user-123"
 *     "region" to "EU"
 *     "tier" to "premium"
 * }
 * ```
 *
 * @param block The builder block to configure the context
 * @return A new [FlippingExecutionContext] with the configured parameters
 */
fun context(block: FlippingExecutionContextBuilder.() -> Unit): FlippingExecutionContext = FlippingExecutionContextBuilder().apply(block).build()
