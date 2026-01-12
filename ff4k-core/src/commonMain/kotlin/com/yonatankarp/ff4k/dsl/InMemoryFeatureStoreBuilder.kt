package com.yonatankarp.ff4k.dsl

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.store.InMemoryFeatureStore

/**
 * DSL builder for creating [InMemoryFeatureStore] instances with a fluent, declarative API.
 *
 * This builder provides a Kotlin DSL for constructing in-memory feature stores with
 * initial features using both builder patterns and direct feature addition.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val store = inMemoryFeatureStore {
 *     feature("dark-mode") {
 *         enable()
 *         description = "Enable dark mode UI"
 *     }
 *
 *     feature("beta-features") {
 *         enable()
 *         inGroup("experimental")
 *     }
 * }
 * ```
 *
 * ## Using Operator Syntax
 *
 * ```kotlin
 * val existingFeature = Feature("existing", isEnabled = true)
 * val store = inMemoryFeatureStore {
 *     +existingFeature
 *     +Feature("another-feature", isEnabled = false)
 *
 *     feature("new-feature") {
 *         enable()
 *     }
 * }
 * ```
 *
 * ## From Collection
 *
 * ```kotlin
 * val featureList = listOf(
 *     Feature("feature-1", isEnabled = true),
 *     Feature("feature-2", isEnabled = false)
 * )
 * val store = inMemoryFeatureStore {
 *     features(featureList)
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin
 */
@FeatureStoreDsl
class InMemoryFeatureStoreBuilder {
    private val features = mutableMapOf<String, Feature>()

    /**
     * Add an existing feature to the store using the unary plus operator.
     *
     * This operator allows for a clean, declarative syntax when adding features.
     *
     * Example:
     * ```kotlin
     * +Feature("feature-1", isEnabled = true)
     * +Feature("feature-2", isEnabled = false)
     * ```
     */
    operator fun Feature.unaryPlus() {
        features[this.uid] = this
    }

    /**
     * Add an existing feature to the store.
     *
     * Example:
     * ```kotlin
     * val existingFeature = Feature("my-feature", isEnabled = true)
     * feature(existingFeature)
     * ```
     */
    fun feature(feature: Feature) {
        features[feature.uid] = feature
    }

    /**
     * Add multiple features from a collection to the store.
     *
     * Example:
     * ```kotlin
     * val featureList = listOf(
     *     Feature("feature-1", isEnabled = true),
     *     Feature("feature-2", isEnabled = false)
     * )
     * features(featureList)
     * ```
     */
    fun features(features: Collection<Feature>) {
        features.forEach { feature(it) }
    }

    /**
     * DSL for inline feature creation with automatic addition to the store.
     *
     * Example:
     * ```kotlin
     * feature("dark-mode") {
     *     enable()
     *     description = "Enable dark mode UI"
     *     inGroup("ui-features")
     * }
     * ```
     */
    fun feature(uid: String, block: FeatureBuilder.() -> Unit) {
        val builtFeature = FeatureBuilder(uid).apply(block).build()
        features[builtFeature.uid] = builtFeature
    }

    internal fun build(): InMemoryFeatureStore = InMemoryFeatureStore(features.toMap())
}

/**
 * Top-level DSL function for creating an [InMemoryFeatureStore] using a declarative builder syntax.
 *
 * This function provides a clean way to create an in-memory feature store with initial
 * features using a fluent DSL.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val store = inMemoryFeatureStore {
 *     feature("dark-mode") {
 *         enable()
 *         description = "Enable dark mode UI"
 *     }
 *
 *     feature("beta-features") {
 *         enable()
 *         inGroup("experimental")
 *     }
 * }
 * ```
 *
 * ## Using Operators
 *
 * ```kotlin
 * val store = inMemoryFeatureStore {
 *     +Feature("feature-1", isEnabled = true)
 *     +Feature("feature-2", isEnabled = false)
 *
 *     feature("feature-3") {
 *         enable()
 *     }
 * }
 * ```
 *
 * ## Empty Store
 *
 * ```kotlin
 * val store = inMemoryFeatureStore { }
 * ```
 *
 * ## With Existing Features
 *
 * ```kotlin
 * val existingFeatures = listOf(
 *     Feature("feature-1", isEnabled = true),
 *     Feature("feature-2", isEnabled = false)
 * )
 *
 * val store = inMemoryFeatureStore {
 *     features(existingFeatures)
 *
 *     feature("additional-feature") {
 *         enable()
 *     }
 * }
 * ```
 *
 * @param block Configuration block for adding initial features
 * @return Configured [InMemoryFeatureStore] instance
 */
fun inMemoryFeatureStore(
    block: InMemoryFeatureStoreBuilder.() -> Unit = {},
): InMemoryFeatureStore = InMemoryFeatureStoreBuilder().apply(block).build()
