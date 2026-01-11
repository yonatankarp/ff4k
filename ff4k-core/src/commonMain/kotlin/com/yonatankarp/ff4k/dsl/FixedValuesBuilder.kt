package com.yonatankarp.ff4k.dsl

/**
 * DSL builder for creating a set of fixed values.
 *
 * Example with unary plus (for non-numeric types):
 * ```
 * fixedValues {
 *     +"TRACE"
 *     +"DEBUG"
 *     +"INFO"
 * }
 * ```
 *
 * Example with add method (recommended for numeric types):
 * ```
 * fixedValues {
 *     add(10)
 *     add(20)
 *     add(30)
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@PropertyDsl
class FixedValuesBuilder<T> {
    private val values = mutableSetOf<T>()

    /**
     * Adds a value to the fixed values set.
     */
    operator fun T.unaryPlus() {
        values.add(this)
    }

    /**
     * Adds a value to the fixed values set.
     * Alternative to unary plus for better compatibility with numeric types.
     */
    fun add(value: T) {
        values.add(value)
    }

    /**
     * Builds the immutable set of fixed values.
     */
    internal fun build(): Set<T> = values.toSet()
}
