package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.dsl.internal.property as propertyDsl

/**
 * Creates a single [Property] using the FF4K DSL.
 *
 * This is a convenience top-level function for defining a property inline
 * with a DSL block, which allows you to set the value, description, fixed values,
 * and read-only status.
 *
 * ## Example
 *
 * ```kotlin
 * val maxRetries: Property<Int> = property("max-retries") {
 *     value = 3
 *     description = "Maximum retry attempts"
 *     fixedValues {
 *         +1
 *         +3
 *         +5
 *     }
 * }
 * ```
 *
 * @param T Type of the property value
 * @param name Name of the property
 * @param block DSL block used to configure the property
 * @return The fully-built [Property] of type [T]
 * @throws IllegalStateException if [PropertyBuilder.value] is not set
 * @throws IllegalArgumentException if the value type is unsupported
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
fun <T> property(name: String, block: PropertyBuilder<T>.() -> Unit): Property<T> = propertyDsl(name, block)

/**
 * Creates a list of [Property] instances using the FF4K DSL.
 *
 * This function allows defining multiple properties in a single block, returning
 * them as a list. Each property can be configured inline with the [PropertyBuilder].
 *
 * ## Example
 *
 * ```kotlin
 * val props: List<Property<*>> = properties {
 *     property("max-retries") {
 *         value = 3
 *         description = "Maximum retry attempts"
 *     }
 *     property("timeout-ms") {
 *         value = 3000L
 *     }
 * }
 * ```
 *
 * @param block DSL block used to define multiple properties
 * @return A list of fully-built [Property] instances
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
fun properties(block: PropertiesBuilder.() -> Unit): List<Property<*>> = PropertiesBuilder().apply(block).build()
