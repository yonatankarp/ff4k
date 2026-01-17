package com.yonatankarp.ff4k.dsl.core

/**
 * Base DSL collector for accumulating a unique set of values.
 *
 * This abstract class is intended to be extended by DSL builders that collect
 * values where **uniqueness is required** (e.g. permissions, fixed values).
 *
 * Values can be added either via the unary `+` operator (idiomatic DSL usage)
 * or explicitly using [add]. The collected values are exposed only through
 * [build], ensuring immutability outside the DSL scope.
 *
 * This class is marked with [FF4kDsl] to prevent accidental scope leakage
 * when used inside nested DSL blocks.
 *
 * @param T the type of elements collected by this builder
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
abstract class SetCollector<T> {

    /**
     * Internal mutable storage for collected values.
     *
     * This set is only mutated during DSL construction and is never exposed
     * directly to consumers.
     */
    protected val values = mutableSetOf<T>()

    /**
     * Adds this value to the collector.
     *
     * This operator enables idiomatic DSL usage such as:
     *
     * ```kotlin
     * fixedValues {
     *     +"A"
     *     +"B"
     * }
     * ```
     *
     * Duplicate values are ignored, as the underlying collection is a [Set].
     *
     * @receiver the value to add to the collector
     */
    operator fun T.unaryPlus() {
        values.add(this)
    }

    /**
     * Adds a value to the collector explicitly.
     *
     * This method is useful when the unary `+` operator cannot be used,
     * such as with numeric types where `+` is already defined as a sign operator.
     *
     * @param value the value to add to the collector
     */
    fun add(value: T) {
        values.add(value)
    }

    /**
     * Builds an immutable [Set] containing all collected values.
     *
     * This method is intended for internal DSL use only and should be called
     * once the DSL configuration phase is complete.
     *
     * @return an immutable set of all collected values
     */
    internal fun build(): Set<T> = values.toSet()
}

/**
 * Base DSL collector for accumulating an ordered list of values.
 *
 * This abstract class is intended to be extended by DSL builders that collect
 * values where **ordering matters** or where duplicates are allowed
 * (e.g. feature lists, property lists).
 *
 * Values are typically added using the unary `+` operator to provide
 * fluent DSL syntax. The collected items are exposed only through [build],
 * ensuring immutability outside the DSL scope.
 *
 * This class is marked with [FF4kDsl] to enforce proper DSL scoping rules.
 *
 * @param T the type of elements collected by this builder
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
abstract class ListCollector<T> {

    /**
     * Internal mutable storage for collected items.
     *
     * This list is only mutated during DSL construction and is never exposed
     * directly to consumers.
     */
    protected val items = mutableListOf<T>()

    /**
     * Adds this value to the collector.
     *
     * This operator enables idiomatic DSL usage such as:
     *
     * ```kotlin
     * features {
     *     +featureA
     *     +featureB
     * }
     * ```
     *
     * Items are added in the order they are encountered.
     *
     * @receiver the value to add to the collector
     */
    operator fun T.unaryPlus() {
        items.add(this)
    }

    /**
     * Adds a value to the collector explicitly.
     *
     * This method is useful when the unary `+` operator cannot be used,
     * such as with numeric types where `+` is already defined as a sign operator.
     *
     * @param value the value to add to the collector
     */
    fun add(value: T) {
        items.add(value)
    }

    /**
     * Builds an immutable [List] containing all collected items.
     *
     * This method is intended for internal DSL use only and should be called
     * once the DSL configuration phase is complete.
     *
     * @return an immutable list of all collected items
     */
    internal fun build(): List<T> = items.toList()
}
