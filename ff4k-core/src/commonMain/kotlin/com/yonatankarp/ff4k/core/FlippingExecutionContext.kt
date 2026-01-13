package com.yonatankarp.ff4k.core

import kotlin.coroutines.CoroutineContext

/**
 * A type-safe execution context for feature flipping strategies.
 *
 * This context holds key-value pairs that can be used during [FlippingStrategy] evaluation.
 * Values are stored with runtime type checking, ensuring type safety when retrieving values.
 *
 * Implements [CoroutineContext.Element] to enable automatic propagation through suspend
 * function calls without ThreadLocal. Use [withFlippingContext] to set the context for
 * a coroutine scope, and [currentFlippingContext] to retrieve it.
 *
 * Example usage:
 * ```
 * // Creating and using context directly
 * val context = FlippingExecutionContext("userId" to 123, "userName" to "Alice")
 * val id: Int? = context["userId"]           // Returns 123
 * val name: String? = context["userName"]    // Returns "Alice"
 *
 * // Using with coroutines (implicit context propagation)
 * withFlippingContext(FlippingExecutionContext("userId" to "user-123")) {
 *     ff4k.check("my-feature") // context automatically available
 * }
 *
 * // Immutable modification (creates new instance)
 * val newContext = context.withParameter("region", "EU")
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
data class FlippingExecutionContext(
    @PublishedApi internal val values: MutableMap<String, Any?> = mutableMapOf(),
) : CoroutineContext.Element {
    /**
     * Key for retrieving [FlippingExecutionContext] from a [CoroutineContext].
     */
    companion object Key : CoroutineContext.Key<FlippingExecutionContext>

    override val key: CoroutineContext.Key<*> get() = Key

    /**
     * Creates a context with initial key-value pairs.
     *
     * @param pairs Initial parameters to populate the context
     */
    constructor(vararg pairs: Pair<String, Any?>) : this(mutableMapOf(*pairs))

    /**
     * Retrieves a value from the context with runtime type checking.
     *
     * @param key the key to look up
     * @param required if `true`, throws an exception when the key is not found; defaults to `false`
     * @return the value cast to type [T], or `null` if the key doesn't exist and [required] is `false`
     * @throws IllegalArgumentException if [required] is `true` and the key is not found
     * @throws IllegalStateException if the value exists but is not of type [T]
     */
    inline operator fun <reified T> get(
        key: String,
        required: Boolean = false,
    ): T? {
        if (key !in values) {
            require(required.not()) { "Parameter '$key' has not been found but it's required to evaluate strategy" }
            return null
        }

        return when (val value = values[key]) {
            null -> null
            is T -> value
            else -> error("Expected type ${T::class.simpleName} for key '$key', but found ${value::class.simpleName}")
        }
    }

    /**
     * Stores a value in the context.
     *
     * @param key the key under which to store the value
     * @param value the value to store
     */
    operator fun <T> set(key: String, value: T) {
        values[key] = value as Any?
    }

    /**
     * Checks if the context contains a value for the given key.
     *
     * @param key the key to check
     * @return `true` if the key exists in the context, `false` otherwise
     */
    operator fun contains(key: String): Boolean = key in values

    /**
     * Indicates whether the context is empty.
     *
     * @return `true` if the context contains no entries, `false` otherwise
     */
    val isEmpty: Boolean get() = values.isEmpty()
}
