package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.dsl.core.FF4kDsl
import com.yonatankarp.ff4k.dsl.core.SetCollector
import com.yonatankarp.ff4k.dsl.property.PropertyBuilder
import com.yonatankarp.ff4k.property.Property

/**
 * DSL builder used to define a single feature in the FF4K configuration.
 *
 * A feature represents a toggleable capability that can be enabled or disabled,
 * grouped, protected by permissions, and controlled by a flipping strategy.
 *
 * Instances of this builder are created internally by the FF4K DSL and are not
 * intended to be instantiated directly.
 *
 * Example:
 * ```kotlin
 * feature("new-ui") {
 *     isEnabled = true
 *     description = "Enables the new UI experience"
 *     group = "ui"
 *
 *     permissions("ADMIN", "BETA_USER")
 *
 *     property<String>("theme") {
 *         value = "dark"
 *     }
 * }
 * ```
 *
 * @property isEnabled Whether the feature is enabled by default
 * @property description Optional human-readable description of the feature
 * @property group Optional logical group name for categorization
 * @property flippingStrategy Optional strategy controlling dynamic enablement
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class FeatureBuilder internal constructor(
    private val uid: String,
) {
    /** Whether the feature is enabled by default. */
    var isEnabled: Boolean = false

    /** Optional human-readable description of the feature. */
    var description: String? = null

    /** Optional logical group name for the feature. */
    var group: String? = null

    /** Optional strategy used to dynamically determine feature enablement. */
    var flippingStrategy: FlippingStrategy? = null

    private val permissions = mutableSetOf<String>()
    private val customProperties = mutableMapOf<String, Property<*>>()

    /**
     * Adds a single permission required to access this feature.
     *
     * @param name the permission identifier
     */
    fun permission(name: String) {
        permissions.add(name)
    }

    /**
     * Adds multiple permissions required to access this feature.
     *
     * @param names permission identifiers
     */
    fun permissions(vararg names: String) {
        permissions.addAll(names)
    }

    /**
     * Adds permissions using a dedicated permissions DSL block.
     *
     * Example:
     * ```kotlin
     * permissions {
     *     +"ADMIN"
     *     +"BETA_USER"
     * }
     * ```
     *
     * @param block DSL block defining permissions
     */
    fun permissions(block: PermissionsBuilder.() -> Unit) {
        permissions.addAll(PermissionsBuilder().apply(block).build())
    }

    /**
     * Attaches a pre-built custom property to this feature.
     *
     * If a property with the same name already exists, it will be replaced.
     *
     * @param property the property to attach
     */
    fun property(property: Property<*>) {
        customProperties[property.name] = property
    }

    /**
     * Defines and attaches a custom property using a property DSL block.
     *
     * @param name the unique name of the property
     * @param block DSL block used to configure the property
     * @param T the property's value type
     */
    fun <T> property(name: String, block: PropertyBuilder<T>.() -> Unit) {
        val prop = PropertyBuilder<T>(name).apply(block).build()
        customProperties[prop.name] = prop
    }

    /**
     * Builds an immutable [Feature] instance from the collected configuration.
     *
     * This method is used internally by the FF4K DSL.
     *
     * @return a fully configured [Feature]
     */
    internal fun build(): Feature = Feature(
        uid = uid,
        isEnabled = isEnabled,
        description = description,
        group = group,
        permissions = permissions.toSet(),
        flippingStrategy = flippingStrategy,
        customProperties = customProperties.toMap(),
    )
}

/**
 * DSL builder used to collect permission identifiers for a feature.
 *
 * This builder supports adding permissions using the unary `+` operator.
 *
 * Example:
 * ```
 * permissions {
 *     +"ADMIN"
 *     +"BETA_USER"
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class PermissionsBuilder : SetCollector<String>()
