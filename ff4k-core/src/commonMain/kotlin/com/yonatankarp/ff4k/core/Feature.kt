package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property
import kotlinx.serialization.Serializable

/**
 * Represents a feature flag (feature toggle) identified by a unique identifier.
 *
 * Feature flags enable/disable functionality at runtime, supporting continuous delivery
 * and controlled feature rollouts as introduced by Martin Fowler.
 *
 * ## Core Capabilities
 *
 * - **Runtime Control**: Enable or disable features without code changes or redeployment
 * - **Security Management**: Limit feature access to specific user roles/permissions
 * - **Grouping**: Organize features into groups for bulk operations
 * - **Advanced Strategies**: Use [FlippingStrategy] for A/B testing, gradual rollouts, etc.
 * - **Custom Properties**: Attach metadata and configuration to features
 *
 * ## Immutability
 *
 * This class is immutable by design. All state-changing operations ([enable], [disable],
 * [toggle], [addProperty]) return a new [Feature] instance rather than modifying the current one.
 *
 * Example usage:
 * ```
 * val feature = Feature(
 *     uid = "new-checkout-flow",
 *     isEnabled = true,
 *     description = "New streamlined checkout process",
 *     group = "checkout",
 *     permissions = setOf("BETA_TESTERS", "ADMIN")
 * )
 *
 * // State changes return new instances
 * val disabled = feature.disable()
 * val toggled = feature.toggle()
 *
 * // Add custom properties
 * val withProperty = feature.addProperty(
 *     intProperty("maxRetries") {
 *     value = 3
 *   }
 * )
 * ```
 *
 * @throws IllegalArgumentException if [uid] is blank or [group] is blank (when non-null)
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@Serializable
data class Feature(
    /**
     * Unique identifier for this feature.
     *
     * Must not be blank. This identifier is used to reference the feature
     * throughout the application and in feature stores.
     */
    val uid: String,

    /**
     * Whether this feature is currently enabled.
     *
     * Defaults to `false`. When `false`, the feature is disabled and should
     * not be active. When `true`, the feature is enabled (subject to
     * [permissions] and [flippingStrategy] evaluation).
     */
    val isEnabled: Boolean = false,

    /**
     * Optional human-readable description of this feature.
     *
     * Used for documentation and display purposes. Can be `null` if no
     * description is provided.
     */
    val description: String? = null,

    /**
     * Optional group name for organizing related features.
     *
     * Features can be grouped together for bulk operations or organizational
     * purposes. If provided, must not be blank. Can be `null` if the feature
     * doesn't belong to any group.
     */
    val group: String? = null,

    /**
     * Set of roles or permissions required to access this feature.
     *
     * Even if the feature is enabled, users must have one of these permissions
     * to use it. An empty set (default) means no permission restrictions apply.
     */
    val permissions: Set<String> = emptySet(),

    /**
     * Optional strategy for advanced feature activation logic.
     *
     * Strategies enable sophisticated feature rollout patterns like:
     * - Percentage-based gradual rollouts
     * - Region or location-based activation
     * - Time-window activation
     * - A/B testing and experimentation
     *
     * Can be `null` if no advanced strategy is needed (simple on/off toggle).
     */
    val flippingStrategy: FlippingStrategy? = null,

    /**
     * Custom properties attached to this feature.
     *
     * Allows storing additional metadata and configuration specific to this
     * feature. Properties are keyed by name and can be of any type.
     * Defaults to an empty map.
     */
    val customProperties: Map<String, Property<*>> = emptyMap(),
) {
    init {
        require(uid.isNotBlank()) { "Feature identifier (param#0) cannot be null nor empty" }
        require(group == null || group.isNotBlank()) { "Feature group (param#3) cannot be blank" }
    }

    /**
     * Returns a new [Feature] instance with [isEnabled] set to `true`.
     *
     * @return a copy of this feature in enabled state
     */
    fun enable() = copy(isEnabled = true)

    /**
     * Returns a new [Feature] instance with [isEnabled] set to `false`.
     *
     * @return a copy of this feature in disabled state
     */
    fun disable() = copy(isEnabled = false)

    /**
     * Returns a new [Feature] instance with [isEnabled] toggled.
     *
     * If currently enabled, returns disabled feature; if disabled, returns enabled feature.
     *
     * @return a copy of this feature with inverted enabled state
     */
    fun toggle() = copy(isEnabled = isEnabled.not())

    /**
     * Returns a new [Feature] instance with the given property added to [customProperties].
     *
     * If a property with the same name already exists, it will be replaced.
     *
     * @param property the property to add
     * @return a copy of this feature with the property added
     */
    fun addProperty(property: Property<*>) = copy(customProperties = customProperties + (property.name to property))

    /**
     * Retrieves a custom property by name.
     *
     * **Note:** Due to type erasure, the type parameter T cannot be verified at runtime.
     * Callers are responsible for ensuring they request the correct type.
     *
     * @param propertyId the name of the property to retrieve
     * @return the property with the specified name
     * @throws PropertyNotFoundException if no property with the given name exists
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(propertyId: String): Property<T> = customProperties[propertyId] as? Property<T>
        ?: throw PropertyNotFoundException(propertyId)

    /**
     * The simple class name of the [flippingStrategy], or `null` if no strategy is set.
     *
     * Useful for displaying strategy information in UIs or logs.
     */
    val displayStrategyClassName: String? get() = if (flippingStrategy != null) flippingStrategy::class.simpleName else null
}

/**
 * Checks if this feature is disabled.
 *
 * This is a convenience extension property that returns the inverse of [Feature.isEnabled].
 * Useful for more readable conditional logic when checking for disabled features.
 *
 * Example:
 * ```kotlin
 * if (feature.isDisabled) {
 *     logger.info("Feature ${feature.uid} is currently disabled")
 * }
 * ```
 *
 * @return `true` if the feature is disabled (isEnabled = false), `false` otherwise
 */
val Feature.isDisabled: Boolean
    get() = isEnabled.not()

/**
 * Returns the set of all custom property names attached to this feature.
 *
 * This extension property provides convenient access to the keys of [Feature.customProperties].
 * Useful for checking property existence or iterating over available properties.
 *
 * Example:
 * ```kotlin
 * // Check if property exists before retrieving
 * if ("maxRetries" in feature.propertyNames) {
 *     val retries = feature.getProperty<Int>("maxRetries")
 * }
 *
 * // List all properties
 * feature.propertyNames.forEach { name ->
 *     println("Property: $name")
 * }
 * ```
 *
 * @return set of property names, or empty set if no custom properties exist
 */
val Feature.propertyNames: Set<String>
    get() = customProperties.keys

/**
 * Checks if this feature has any permission restrictions.
 *
 * This extension property returns `true` if the feature has at least one permission/role
 * requirement, indicating that access control is enforced. Returns `false` for features
 * with no permission restrictions (publicly accessible when enabled).
 *
 * Example:
 * ```kotlin
 * if (feature.hasPermissions) {
 *     // Check user permissions before granting access
 *     val userRoles = getUserRoles()
 *     if (feature.permissions.any { it in userRoles }) {
 *         enableFeature()
 *     }
 * }
 * ```
 *
 * @return `true` if [Feature.permissions] is not empty, `false` otherwise
 */
val Feature.hasPermissions: Boolean
    get() = permissions.isNotEmpty()

/**
 * Checks if this feature has a flipping strategy configured.
 *
 * This extension property returns `true` if a [FlippingStrategy] is attached to this feature,
 * indicating that advanced activation logic (A/B testing, gradual rollout, etc.) is in use.
 *
 * Example:
 * ```kotlin
 * if (feature.hasFlippingStrategy) {
 *     // Evaluate strategy with context
 *     val context = FlippingExecutionContext()
 *     context["userId"] = currentUserId
 *     val shouldActivate = feature.flippingStrategy?.evaluate(
 *         feature.uid,
 *         featureStore,
 *         context
 *     ) ?: false
 * }
 * ```
 *
 * @return `true` if [Feature.flippingStrategy] is not null, `false` otherwise
 */
val Feature.hasFlippingStrategy: Boolean
    get() = flippingStrategy != null
