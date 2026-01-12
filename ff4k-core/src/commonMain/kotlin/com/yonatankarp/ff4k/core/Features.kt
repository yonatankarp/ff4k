package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property

/**
 * Extension functions for [Feature] providing convenient operations for working with
 * immutable feature instances.
 *
 * This file contains helpers for:
 * - Property access and manipulation
 * - Permission management
 * - Feature state checking
 *
 * All modification operations return new [Feature] instances, maintaining immutability.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */

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

// ============================================================================
// Property Access Extensions
// ============================================================================

/**
 * Retrieves a custom property's value by name, returning a default if not found.
 *
 * This is a type-safe convenience method that combines property lookup with null handling.
 * If the property doesn't exist or is of the wrong type, returns the provided default value.
 *
 * Example:
 * ```kotlin
 * val maxRetries = feature.getPropertyValueOrDefault("maxRetries", 3)
 * val timeout = feature.getPropertyValueOrDefault("timeout", 5000L)
 * val enabled = feature.getPropertyValueOrDefault("cacheEnabled", false)
 * ```
 *
 * @param T the expected type of the property value (inferred from default)
 * @param name the name of the property to retrieve
 * @param default the value to return if property is not found or type doesn't match
 * @return the property value if found and type matches, otherwise [default]
 */
inline fun <reified T> Feature.getPropertyValueOrDefault(name: String, default: T): T = getProperty<T>(name)?.value ?: default

/**
 * Retrieves a custom property by name, throwing an exception if not found.
 *
 * This is useful when a property is required and its absence indicates a configuration error.
 * Prefer this over [Feature.getProperty] when you want to fail fast on missing properties.
 *
 * Example:
 * ```kotlin
 * val apiKeyProp = feature.getPropertyOrThrow<String>("apiKey")
 * val apiKey = apiKeyProp.value
 * ```
 *
 * @param T the expected type of the property
 * @param name the name of the property to retrieve
 * @return the property with the specified name
 * @throws PropertyNotFoundException if no property with the given name exists
 */
inline fun <reified T> Feature.getPropertyOrThrow(name: String): Property<T> = getProperty(name) ?: throw PropertyNotFoundException(name)

/**
 * Checks if a property exists and has a specific value.
 *
 * Useful for conditional logic based on property values. Returns `false` if the property
 * doesn't exist, is of the wrong type, or has a different value.
 *
 * Example:
 * ```kotlin
 * if (feature.hasPropertyWithValue("environment", "production")) {
 *     enableStrictValidation()
 * }
 *
 * if (feature.hasPropertyWithValue("version", 2)) {
 *     useNewAlgorithm()
 * }
 * ```
 *
 * @param T the expected type of the property value
 * @param name the name of the property to check
 * @param value the value to compare against
 * @return `true` if property exists and its value equals [value], `false` otherwise
 */
fun <T> Feature.hasPropertyWithValue(name: String, value: T): Boolean = getProperty<T>(name)?.value == value

/**
 * Returns all custom properties whose values are of the specified type.
 *
 * This is useful for filtering properties by type, such as getting all integer properties
 * or all string properties. The type is determined at runtime using reified generics.
 *
 * Example:
 * ```kotlin
 * // Get all integer properties
 * val intProps: Map<String, Property<Int>> = feature.getPropertiesOfType<Int>()
 *
 * // Get all string properties
 * val stringProps: Map<String, Property<String>> = feature.getPropertiesOfType<String>()
 *
 * intProps.forEach { (name, prop) ->
 *     println("$name = ${prop.value}")
 * }
 * ```
 *
 * @param T the type to filter by
 * @return map of property names to properties where the value is of type [T]
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Feature.getPropertiesOfType(): Map<String, Property<T>> = customProperties.filterValues { it.value is T }
    .mapValues { it.value as Property<T> }

// ============================================================================
// Property Manipulation Extensions
// ============================================================================

/**
 * Returns a new [Feature] with multiple properties added.
 *
 * This is a convenience vararg overload of [addProperties] for adding properties inline.
 * If any property name already exists, it will be replaced.
 *
 * Example:
 * ```kotlin
 * val updated = feature.addProperties(
 *     intProperty("maxRetries") { value = 3 },
 *     stringProperty("environment") { value = "production" },
 *     booleanProperty("cacheEnabled") { value = true }
 * )
 * ```
 *
 * @param properties the properties to add
 * @return a new [Feature] with the properties added
 * @see addProperties for the Collection version
 */
fun Feature.addProperties(vararg properties: Property<*>): Feature = addProperties(properties.toList())

/**
 * Returns a new [Feature] with multiple properties added.
 *
 * Properties are added by name - if a property with the same name already exists,
 * it will be replaced. This maintains immutability by returning a new instance.
 *
 * Example:
 * ```kotlin
 * val properties = listOf(
 *     intProperty("timeout") { value = 5000 },
 *     stringProperty("region") { value = "us-east-1" }
 * )
 * val updated = feature.addProperties(properties)
 * ```
 *
 * @param properties the collection of properties to add
 * @return a new [Feature] with the properties added
 */
fun Feature.addProperties(properties: Collection<Property<*>>): Feature {
    val newProperties = properties.associateBy { it.name }
    return copy(customProperties = customProperties + newProperties)
}

/**
 * Returns a new [Feature] with the specified properties removed.
 *
 * This is a convenience vararg overload of [removeProperties] for removing properties inline.
 * Property names that don't exist are silently ignored.
 *
 * Example:
 * ```kotlin
 * val updated = feature.removeProperties("oldConfig", "deprecatedSetting", "unused")
 * ```
 *
 * @param names the names of properties to remove
 * @return a new [Feature] with the properties removed
 * @see removeProperties for the Collection version
 */
fun Feature.removeProperties(vararg names: String): Feature = removeProperties(names.toList())

/**
 * Returns a new [Feature] with the specified properties removed.
 *
 * Removes all properties whose names are in the provided collection. Property names
 * that don't exist are silently ignored. This maintains immutability by returning
 * a new instance.
 *
 * Example:
 * ```kotlin
 * val propertiesToRemove = setOf("tempConfig", "beta", "experimental")
 * val updated = feature.removeProperties(propertiesToRemove)
 * ```
 *
 * @param names the collection of property names to remove
 * @return a new [Feature] with the properties removed
 */
fun Feature.removeProperties(names: Collection<String>): Feature = copy(customProperties = customProperties - names.toSet())

/**
 * Returns a new [Feature] with all custom properties removed.
 *
 * This is useful for resetting a feature to its base configuration without any
 * custom properties. The returned feature will have an empty [Feature.customProperties] map.
 *
 * Example:
 * ```kotlin
 * val cleanFeature = feature.clearProperties()
 * assert(cleanFeature.customProperties.isEmpty())
 * ```
 *
 * @return a new [Feature] with no custom properties
 */
fun Feature.clearProperties(): Feature = copy(customProperties = emptyMap())

// ============================================================================
// Permission Management Extensions
// ============================================================================

/**
 * Returns a new [Feature] with the specified permissions granted.
 *
 * This is a convenience vararg overload of [grantPermissions] for granting permissions inline.
 * Permissions that already exist will not be duplicated (since [Feature.permissions] is a Set).
 *
 * Example:
 * ```kotlin
 * val updated = feature.grantPermissions("ADMIN", "BETA_TESTER", "POWER_USER")
 * ```
 *
 * @param permissions the permissions to grant
 * @return a new [Feature] with the permissions added
 * @see grantPermissions for the Collection version
 */
fun Feature.grantPermissions(vararg permissions: String): Feature = grantPermissions(permissions.toList())

/**
 * Returns a new [Feature] with the specified permissions granted.
 *
 * Adds the provided permissions to the feature's permission set. Permissions that already
 * exist will not be duplicated. This maintains immutability by returning a new instance.
 *
 * Example:
 * ```kotlin
 * val newPermissions = setOf("ADMIN", "MODERATOR")
 * val updated = feature.grantPermissions(newPermissions)
 * ```
 *
 * @param permissions the collection of permissions to grant
 * @return a new [Feature] with the permissions added
 */
fun Feature.grantPermissions(permissions: Collection<String>): Feature = copy(permissions = this.permissions + permissions)

/**
 * Returns a new [Feature] with the specified permissions revoked.
 *
 * This is a convenience vararg overload of [revokePermissions] for revoking permissions inline.
 * Permissions that don't exist are silently ignored.
 *
 * Example:
 * ```kotlin
 * val updated = feature.revokePermissions("BETA_TESTER", "DEPRECATED_ROLE")
 * ```
 *
 * @param permissions the permissions to revoke
 * @return a new [Feature] with the permissions removed
 * @see revokePermissions for the Collection version
 */
fun Feature.revokePermissions(vararg permissions: String): Feature = revokePermissions(permissions.toList())

/**
 * Returns a new [Feature] with the specified permissions revoked.
 *
 * Removes the provided permissions from the feature's permission set. Permissions that
 * don't exist are silently ignored. This maintains immutability by returning a new instance.
 *
 * Example:
 * ```kotlin
 * val permissionsToRevoke = setOf("GUEST", "TRIAL")
 * val updated = feature.revokePermissions(permissionsToRevoke)
 * ```
 *
 * @param permissions the collection of permissions to revoke
 * @return a new [Feature] with the permissions removed
 */
fun Feature.revokePermissions(permissions: Collection<String>): Feature = copy(permissions = this.permissions - permissions.toSet())

/**
 * Checks if this feature has at least one of the specified permissions.
 *
 * Returns `true` if the feature's permission set contains any of the provided permissions.
 * Useful for checking if a user with any of the given roles can access this feature.
 *
 * Example:
 * ```kotlin
 * // Check if feature requires admin or moderator access
 * if (feature.hasAnyPermission("ADMIN", "MODERATOR")) {
 *     // Feature requires elevated privileges
 *     checkUserElevatedAccess()
 * }
 * ```
 *
 * @param permissions the permissions to check for
 * @return `true` if at least one permission is in [Feature.permissions], `false` otherwise
 */
fun Feature.hasAnyPermission(vararg permissions: String): Boolean = permissions.any { it in this.permissions }

/**
 * Checks if this feature has all of the specified permissions.
 *
 * Returns `true` only if the feature's permission set contains every provided permission.
 * Useful for checking if a feature requires multiple roles simultaneously.
 *
 * Example:
 * ```kotlin
 * // Check if feature requires both admin AND developer access
 * if (feature.hasAllPermissions("ADMIN", "DEVELOPER")) {
 *     // Feature requires multiple roles
 *     verifyMultiRoleAccess()
 * }
 * ```
 *
 * @param permissions the permissions to check for
 * @return `true` if all permissions are in [Feature.permissions], `false` otherwise
 */
fun Feature.hasAllPermissions(vararg permissions: String): Boolean = permissions.all { it in this.permissions }

/**
 * Returns a new [Feature] with all permissions removed.
 *
 * This is useful for making a feature publicly accessible (no permission restrictions).
 * The returned feature will have an empty [Feature.permissions] set.
 *
 * Example:
 * ```kotlin
 * // Make feature public by removing all permission restrictions
 * val publicFeature = feature.clearPermissions()
 * assert(publicFeature.permissions.isEmpty())
 * ```
 *
 * @return a new [Feature] with no permissions
 */
fun Feature.clearPermissions(): Feature = copy(permissions = emptySet())
