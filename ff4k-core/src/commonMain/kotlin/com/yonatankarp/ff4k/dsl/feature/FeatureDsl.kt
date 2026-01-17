package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.dsl.internal.feature as featureDsl

/**
 * Creates a single [Feature] using the FF4K Kotlin DSL.
 *
 * This is a top-level helper function to define a feature in a concise
 * DSL block. You can configure its properties, permissions, and
 * custom properties inside the [block].
 *
 * ## Example
 *
 * ```kotlin
 * val darkModeFeature = feature("dark-mode") {
 *     isEnabled = true
 *     description = "Enable dark mode UI"
 *     group = "ui"
 *     permission("ROLE_ADMIN")
 *     property("max-retries") {
 *         value = 3
 *     }
 * }
 * ```
 *
 * @param uid Unique identifier for the feature
 * @param block DSL block to configure the [FeatureBuilder]
 * @return A fully built [Feature] instance
 * @throws IllegalArgumentException if any property inside the block is invalid
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
fun feature(uid: String, block: FeatureBuilder.() -> Unit): Feature = featureDsl(uid, block)

/**
 * Builds a list of [Feature] instances using the FF4K Kotlin DSL.
 *
 * This function allows you to define multiple features in a nested DSL
 * block. Use `feature(uid) {}` inside the block to add individual features.
 *
 * ## Example
 *
 * ```kotlin
 * val features = features {
 *     feature("dark-mode") {
 *         isEnabled = true
 *     }
 *     feature("beta-features") {
 *         group = "experimental"
 *     }
 * }
 * ```
 *
 * @param block DSL block to configure [FeaturesBuilder] with multiple features
 * @return List of fully built [Feature] instances
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
fun features(block: FeaturesBuilder.() -> Unit): List<Feature> = FeaturesBuilder().apply(block).build()
