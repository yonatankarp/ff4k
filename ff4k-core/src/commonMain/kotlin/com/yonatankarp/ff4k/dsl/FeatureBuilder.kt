package com.yonatankarp.ff4k.dsl

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.Property

/**
 * DSL builder for creating [Feature] instances with a fluent, declarative API.
 *
 * This builder provides a Kotlin DSL for constructing features with various configurations
 * including properties, permissions, groups, and flipping strategies.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val feature = feature("dark-mode") {
 *     enable()
 *     description = "Enable dark mode UI"
 * }
 * ```
 *
 * ## With Properties
 *
 * ```kotlin
 * val feature = feature("api-rate-limit") {
 *     isEnabled = true
 *     description = "API rate limiting configuration"
 *     inGroup("api-features")
 *
 *     // Inline property creation with type inference
 *     property("max-requests") {
 *         value = 1000
 *         description = "Maximum requests per hour"
 *     }
 *
 *     property("timeout-seconds") {
 *         value = 60
 *     }
 * }
 * ```
 *
 * ## With Permissions
 *
 * ```kotlin
 * val feature = feature("admin-panel") {
 *     enable()
 *     description = "Admin panel access"
 *
 *     // Multiple ways to add permissions
 *     permission("ROLE_ADMIN")
 *     permissions("ROLE_SUPER_ADMIN", "ROLE_MODERATOR")
 *
 *     // Or using nested DSL block
 *     permissions {
 *         +"ROLE_OWNER"
 *     }
 * }
 * ```
 *
 * ## Complete Example
 *
 * ```kotlin
 * val feature = feature("checkout-flow") {
 *     enable()
 *     description = "New streamlined checkout process"
 *     inGroup("checkout")
 *
 *     permissions {
 *         +"BETA_TESTERS"
 *         +"PREMIUM_USERS"
 *     }
 *
 *     property("max-retry-attempts") {
 *         value = 3
 *         description = "Maximum number of payment retry attempts"
 *         readOnly = true
 *     }
 *
 *     property("payment-timeout-ms") {
 *         value = 30000L
 *     }
 * }
 * ```
 *
 * @param uid Unique identifier for the feature
 *
 * @author Yonatan Karp-Rudin
 */
@FeatureDsl
class FeatureBuilder(private val uid: String) {
    /**
     * Whether this feature is enabled. Defaults to `false`.
     */
    var isEnabled: Boolean = false

    /**
     * Optional human-readable description of the feature.
     */
    var description: String? = null

    /**
     * Optional group name for organizing related features.
     */
    var group: String? = null

    private val permissions = mutableSetOf<String>()

    /**
     * Optional flipping strategy for advanced activation logic.
     */
    var flippingStrategy: FlippingStrategy? = null

    private val customProperties = mutableMapOf<String, Property<*>>()

    /**
     * Adds a single permission to the feature.
     *
     * Example:
     * ```
     * permission("ROLE_ADMIN")
     * ```
     */
    fun permission(name: String) {
        permissions.add(name)
    }

    /**
     * Adds multiple permissions to the feature using vararg syntax.
     *
     * Example:
     * ```
     * permissions("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR")
     * ```
     */
    fun permissions(vararg names: String) {
        permissions.addAll(names)
    }

    /**
     * Adds an existing property to the feature.
     *
     * Example:
     * ```
     * property(stringProperty("api-key") {
     *     value = "secret"
     * })
     * ```
     */
    fun <T : Any> property(property: Property<T>) {
        customProperties[property.name] = property
    }

    /**
     * DSL for inline property creation with automatic type inference.
     * The property type is determined at runtime based on the value set in the block.
     *
     * Example:
     * ```
     * property("max-requests") {
     *     value = 1000  // Automatically creates PropertyInt
     *     description = "Maximum requests per hour"
     * }
     *
     * property("api-key") {
     *     value = "secret"  // Automatically creates PropertyString
     * }
     * ```
     */
    fun property(name: String, block: PropertyBuilder<Any>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val builder = PropertyBuilder<Any>(name)
        builder.apply(block)
        val prop = builder.build()
        customProperties[prop.name] = prop
    }

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
 * Top-level DSL function for creating a [Feature] using a declarative builder syntax.
 *
 * Example:
 * ```kotlin
 * val feature = feature("dark-mode") {
 *     enable()
 *     description = "Enable dark mode UI"
 *     inGroup("ui-features")
 *
 *     property("theme-color") {
 *         value = "#1a1a1a"
 *     }
 * }
 * ```
 *
 * @param uid Unique identifier for the feature
 * @param block Configuration block for the feature
 * @return Configured [Feature] instance
 */
fun feature(uid: String, block: FeatureBuilder.() -> Unit): Feature = FeatureBuilder(uid).apply(block).build()

/**
 * DSL builder for adding permissions to a feature using a declarative syntax.
 *
 * This builder provides a clean, list-like syntax for adding multiple permissions
 * to a feature using the unary plus operator.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * permissions {
 *     +"ROLE_ADMIN"
 *     +"ROLE_SUPER_ADMIN"
 * }
 * ```
 *
 * ## Complete Example
 *
 * ```kotlin
 * val feature = feature("admin-panel") {
 *     enable()
 *     description = "Admin panel access"
 *
 *     permissions {
 *         +"ROLE_ADMIN"
 *         +"ROLE_SUPER_ADMIN"
 *         +"ROLE_MODERATOR"
 *         +"ROLE_OWNER"
 *     }
 * }
 * ```
 *
 * ## Alternative Approaches
 *
 * Besides the DSL block, you can also add permissions using:
 * - Single permission: `permission("ROLE_ADMIN")`
 * - Multiple permissions: `permissions("ROLE_ADMIN", "ROLE_USER")`
 *
 * @see FeatureBuilder.permission
 * @see FeatureBuilder.permissions
 *
 * @author Yonatan Karp-Rudin
 */
@FeatureDsl
class PermissionsBuilder {
    private val permissions = mutableSetOf<String>()

    /**
     * Adds a permission using the unary plus operator.
     *
     * This operator allows for a clean, declarative syntax when adding multiple
     * permissions to a feature.
     *
     * Example:
     * ```kotlin
     * +"ROLE_ADMIN"
     * +"ROLE_USER"
     * ```
     */
    operator fun String.unaryPlus() {
        permissions.add(this)
    }

    /**
     * Builds the immutable set of permissions.
     *
     * @return Set of permission names
     */
    internal fun build(): Set<String> = permissions.toSet()
}

/**
 * DSL function for adding permissions using a nested block with declarative syntax.
 *
 * This function provides a clean way to add multiple permissions to a feature using
 * the [PermissionsBuilder] DSL with unary plus operators.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * feature("admin-panel") {
 *     permissions {
 *         +"ROLE_ADMIN"
 *         +"ROLE_SUPER_ADMIN"
 *     }
 * }
 * ```
 *
 * ## Multiple Permissions
 *
 * ```kotlin
 * feature("content-management") {
 *     enable()
 *     description = "Content management system"
 *
 *     permissions {
 *         +"ROLE_EDITOR"
 *         +"ROLE_PUBLISHER"
 *         +"ROLE_ADMIN"
 *         +"ROLE_CONTENT_MANAGER"
 *     }
 * }
 * ```
 *
 * @param block Configuration block for adding permissions
 * @see PermissionsBuilder
 */
fun FeatureBuilder.permissions(block: PermissionsBuilder.() -> Unit) {
    val permissions = PermissionsBuilder().apply(block).build()
    permissions.forEach { permission(it) }
}
