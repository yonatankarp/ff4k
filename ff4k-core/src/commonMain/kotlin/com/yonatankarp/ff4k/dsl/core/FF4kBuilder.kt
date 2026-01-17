package com.yonatankarp.ff4k.dsl.core

import com.yonatankarp.ff4k.FF4k
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.dsl.feature.FeaturesBuilder
import com.yonatankarp.ff4k.dsl.property.PropertiesBuilder
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.store.InMemoryPropertyStore

/**
 * Root DSL builder for configuring an [FF4k] instance.
 *
 * This class represents the **root scope** of the FF4k Kotlin DSL.
 * It is used to declaratively register features and properties
 * into an [FF4k] engine using structured, type-safe Kotlin blocks.
 *
 * Instances of this builder are **not created manually**.
 * Instead, an instance is provided as the receiver of the [ff4k] DSL function.
 *
 * ---
 *
 * ### DSL structure overview
 *
 * ```kotlin
 * val ff4k = ff4k {
 *     feature(existingFeature)
 *
 *     features {
 *         feature("dark-mode") {
 *             isEnabled = true
 *         }
 *     }
 *
 *     properties {
 *         property("max-retries") {
 *             value = 3
 *         }
 *     }
 * }
 * ```
 *
 * ---
 *
 * ### Execution model
 *
 * All mutation methods on this builder are declared as `suspend` functions.
 * This allows FF4k to support asynchronous or remote implementations of
 * [FeatureStore] and [PropertyStore].
 *
 * The DSL block is executed **sequentially**, and all side effects are applied
 * immediately to the underlying [FF4k] instance.
 *
 * ---
 *
 * ### DSL scoping
 *
 * This class is annotated with [FF4kDsl] to prevent accidental receiver leakage
 * when nesting DSL blocks (e.g. feature builders inside the root DSL).
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class FF4kBuilder internal constructor(
    private val ff4k: FF4k,
) {
    /**
     * Registers an already constructed [Feature] instance.
     *
     * This method is useful when features are created programmatically
     * or loaded from an external source rather than defined inline
     * using the DSL.
     *
     * If a feature with the same UID already exists, the behavior depends
     * on the configured [FeatureStore] implementation.
     *
     * @param feature the feature to register
     */
    suspend fun feature(feature: Feature) {
        ff4k.addFeature(feature)
    }

    /**
     * Registers an already constructed [Property] instance.
     *
     * This method is useful when properties are created programmatically
     * or shared across multiple features.
     *
     * If a property with the same name already exists, the behavior depends
     * on the configured [PropertyStore] implementation.
     *
     * @param property the property to register
     */
    suspend fun property(property: Property<*>) {
        ff4k.addProperty(property)
    }

    /**
     * Registers multiple features using a nested DSL block.
     *
     * This is the **preferred way** to define features inline using the DSL.
     * Each feature defined inside the block is built and registered
     * sequentially.
     *
     * Example:
     *
     * ```kotlin
     * features {
     *     feature("beta-feature") {
     *         isEnabled = true
     *     }
     *
     *     feature("experimental-ui") {
     *         group = "experimental"
     *     }
     * }
     * ```
     *
     * @param block DSL block used to define one or more features
     */
    suspend fun features(block: FeaturesBuilder.() -> Unit) {
        for (feature in FeaturesBuilder().apply(block).build()) {
            ff4k.addFeature(feature)
        }
    }

    /**
     * Registers multiple properties using a nested DSL block.
     *
     * This method allows defining reusable or global properties
     * using a declarative DSL.
     *
     * Example:
     *
     * ```kotlin
     * properties {
     *     property("timeout-ms") {
     *         value = 5000L
     *     }
     * }
     * ```
     *
     * @param block DSL block used to define one or more properties
     */
    suspend fun properties(block: PropertiesBuilder.() -> Unit) {
        for (property in PropertiesBuilder().apply(block).build()) {
            ff4k.addProperty(property)
        }
    }
}

/**
 * Creates and configures an [FF4k] instance using the Kotlin DSL.
 *
 * This function is the **primary entry point** for the FF4k Kotlin DSL.
 * It initializes an [FF4k] engine, executes the provided DSL [block],
 * and returns the fully configured instance.
 *
 * ---
 *
 * ### Minimal example
 *
 * ```kotlin
 * val ff4k = ff4k { }
 * ```
 *
 * ---
 *
 * ### Feature definition example
 *
 * ```kotlin
 * val ff4k = ff4k {
 *     features {
 *         feature("dark-mode") {
 *             isEnabled = true
 *             description = "Enable dark UI"
 *         }
 *     }
 * }
 * ```
 *
 * ---
 *
 * ### Custom stores
 *
 * ```kotlin
 * val ff4k = ff4k(
 *     featureStore = customFeatureStore,
 *     propertyStore = customPropertyStore
 * ) {
 *     // DSL configuration
 * }
 * ```
 *
 * ---
 *
 * @param featureStore the [FeatureStore] implementation used to persist features
 * @param propertyStore the [PropertyStore] implementation used to persist properties
 * @param source the source identifier associated with created entities
 * @param autoCreate whether missing features or properties should be auto-created
 * @param block DSL block used to configure the FF4k instance
 *
 * @return a fully configured [FF4k] instance
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
suspend fun ff4k(
    featureStore: FeatureStore = InMemoryFeatureStore(),
    propertyStore: PropertyStore = InMemoryPropertyStore(),
    source: FF4k.Source = FF4k.Source.KotlinApi,
    autoCreate: Boolean = false,
    block: suspend FF4kBuilder.() -> Unit,
): FF4k {
    val ff4k = FF4k(
        featureStore = featureStore,
        propertyStore = propertyStore,
        source = source,
        autoCreate = autoCreate,
    )
    FF4kBuilder(ff4k).block()
    return ff4k
}
