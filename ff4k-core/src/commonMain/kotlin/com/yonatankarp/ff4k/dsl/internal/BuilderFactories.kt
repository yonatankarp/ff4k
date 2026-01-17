package com.yonatankarp.ff4k.dsl.internal

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.dsl.feature.FeatureBuilder
import com.yonatankarp.ff4k.dsl.property.PropertyBuilder
import com.yonatankarp.ff4k.property.Property

/**
 * Internal helper to build a [Feature] using [FeatureBuilder].
 *
 * This function is used by the public DSL helper `feature(...)` to create
 * a [Feature] instance in a concise, reusable manner.
 *
 * @param uid Unique identifier for the feature
 * @param block DSL block to configure the [FeatureBuilder]
 * @return A fully built [Feature] instance
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
internal fun feature(
    uid: String,
    block: FeatureBuilder.() -> Unit,
): Feature = FeatureBuilder(uid).apply(block).build()

/**
 * Internal helper to build a [Property] using [PropertyBuilder].
 *
 * This function is used by the public DSL helper `property(...)` to create
 * a [Property] instance in a concise, reusable manner.
 *
 * @param name Name of the property
 * @param block DSL block to configure the [PropertyBuilder]
 * @return A fully built [Property] instance of type [T]
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
internal fun <T> property(
    name: String,
    block: PropertyBuilder<T>.() -> Unit,
): Property<T> = PropertyBuilder<T>(name).apply(block).build()
