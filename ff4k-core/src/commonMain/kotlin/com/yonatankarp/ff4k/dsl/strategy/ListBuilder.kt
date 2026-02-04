package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.core.FF4kDsl

/**
 * DSL builder for constructing a set of identifiers used by list-based strategies.
 *
 * Provides multiple ways to add identifiers:
 * - Unary plus operator: `+"identifier"`
 * - [add] function: `add("identifier")`
 * - [addAll] function: `addAll("id1", "id2", "id3")`
 *
 * @see allowListStrategy
 * @see denyListStrategy
 * @see clientFilterStrategy
 * @see serverFilterStrategy
 * @see regionStrategy
 */
@FF4kDsl
class ListBuilder {
    private val identifiers = mutableSetOf<String>()

    /**
     * Adds this string as an identifier to the list.
     *
     * ## Example
     *
     * ```kotlin
     * allowListStrategy {
     *     +"user-123"
     *     +"user-456"
     * }
     * ```
     */
    operator fun String.unaryPlus() {
        identifiers.add(this)
    }

    /**
     * Adds an identifier to the list.
     *
     * @param identifier The identifier to add.
     */
    fun add(identifier: String) {
        identifiers.add(identifier)
    }

    /**
     * Adds multiple identifiers to the list.
     *
     * @param identifiers The identifiers to add.
     */
    fun addAll(vararg identifiers: String) {
        this.identifiers.addAll(identifiers)
    }

    internal fun build(): Set<String> = identifiers.toSet()
}
