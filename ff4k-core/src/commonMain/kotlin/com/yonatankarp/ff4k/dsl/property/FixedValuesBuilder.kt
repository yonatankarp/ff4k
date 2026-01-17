package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.dsl.core.SetCollector

/**
 * DSL builder for defining a set of allowed, immutable values for a property.
 *
 * Use this builder inside a [PropertyBuilder] to specify which values
 * are valid for the property. Duplicate values are automatically removed.
 *
 * The builder supports the unary plus operator (`+`) and [add] function
 * to add values to the set.
 *
 * ## Example
 *
 * ```kotlin
 * val allowedLevels = FixedValuesBuilder<String>().apply {
 *     +"DEBUG"
 *     +"INFO"
 *     +"WARN"
 *     +"ERROR"
 * }.build()
 * ```
 *
 * When used in a property DSL:
 *
 * ```kotlin
 * property("log-level") {
 *     value = "INFO"
 *     fixedValues {
 *         +"DEBUG"
 *         +"INFO"
 *         +"WARN"
 *         +"ERROR"
 *     }
 * }
 * ```
 *
 * @param T Type of the values in the set
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kPropertyDsl
class FixedValuesBuilder<T> : SetCollector<T>()
