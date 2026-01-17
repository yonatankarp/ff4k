package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.dsl.core.FF4kDsl
import com.yonatankarp.ff4k.dsl.core.ListCollector
import com.yonatankarp.ff4k.dsl.internal.feature as featureDsl

/**
 * DSL builder for defining multiple [Feature] instances in a nested block.
 *
 * Use this builder inside a [ff4k] or other DSL block to group feature definitions.
 * You can add:
 * - pre-built [Feature] objects,
 * - collections of features, or
 * - inline features using the DSL.
 *
 * ## Example
 *
 * ```kotlin
 * val featureList = FeaturesBuilder().apply {
 *     feature("dark-mode") {
 *         isEnabled = true
 *     }
 *     feature(Feature("beta-feature", isEnabled = false))
 *     features(listOf(
 *         Feature("feature-1"),
 *         Feature("feature-2")
 *     ))
 * }.build()
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class FeaturesBuilder : ListCollector<Feature>() {

    /**
     * Adds a pre-built [Feature] to this builder.
     *
     * @param feature The [Feature] instance to add
     */
    fun feature(feature: Feature) {
        +feature
    }

    /**
     * Adds a collection of [Feature] instances to this builder.
     *
     * @param features The collection of [Feature] objects to add
     */
    fun features(features: Collection<Feature>) {
        features.forEach { +it }
    }

    /**
     * Creates and adds a [Feature] inline using the DSL.
     *
     * @param uid Unique identifier of the feature
     * @param block DSL block to configure the [FeatureBuilder]
     */
    fun feature(uid: String, block: FeatureBuilder.() -> Unit) {
        +featureDsl(uid, block)
    }
}
