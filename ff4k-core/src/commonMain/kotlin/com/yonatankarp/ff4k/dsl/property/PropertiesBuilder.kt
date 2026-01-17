package com.yonatankarp.ff4k.dsl.property

import com.yonatankarp.ff4k.dsl.core.FF4kDsl
import com.yonatankarp.ff4k.dsl.core.ListCollector
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.dsl.internal.property as propertyDsl

/**
 * DSL builder for defining multiple [Property] instances in a single block.
 *
 * Use this builder inside a [ff4k] or other DSL block to group property definitions.
 * You can add:
 * - pre-built [Property] objects, or
 * - inline properties using the DSL.
 *
 * ## Example
 *
 * ```kotlin
 * val properties = PropertiesBuilder().apply {
 *     property("max-retries") {
 *         value = 3
 *         description = "Maximum retry attempts"
 *     }
 *     property(PropertyString("api-key", "secret"))
 * }.build()
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class PropertiesBuilder : ListCollector<Property<*>>() {

    /**
     * Adds a pre-built [Property] to this builder.
     *
     * @param property The [Property] instance to add
     */
    fun property(property: Property<*>) {
        +property
    }

    /**
     * Creates and adds a [Property] inline using the DSL.
     *
     * @param name Name of the property
     * @param block DSL block to configure the [PropertyBuilder]
     */
    fun <T> property(name: String, block: PropertyBuilder<T>.() -> Unit) {
        +propertyDsl(name, block)
    }
}
