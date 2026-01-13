package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property

/**
 * Extension functions for [PropertyStore] providing convenient operations for working with
 * property storage.
 *
 * This file contains helpers for:
 * - Property retrieval with defaults and null-safety
 * - Atomic-like upsert operations
 * - Transform-based updates
 * - Store metadata queries
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */

/**
 * Creates a new property or updates it if it already exists (upsert operation).
 *
 * This is a convenience operation that:
 * - Adds the property if it doesn't exist
 * - Updates the property if it already exists
 *
 * **Race Condition Warning:** This operation is NOT atomic. Between the existence check
 * and the create/update operation, another coroutine could:
 * - Create the property (causing a duplicate creation attempt)
 * - Delete the property (causing an update on a non-existent property)
 *
 * For truly atomic upsert behavior, implementations should provide a native upsert operation.
 *
 * Example:
 * ```kotlin
 * // Create or update a property
 * val timeout = Property(name = "timeout", value = 5000)
 * store.createOrUpdateProperty(timeout)
 *
 * // Safe to call multiple times
 * store.createOrUpdateProperty(Property(name = "maxRetries", value = 3))
 * store.createOrUpdateProperty(Property(name = "maxRetries", value = 5)) // Updates to 5
 * ```
 *
 * @param T the type of the property value
 * @param property the property to create or update
 */
suspend fun <T> PropertyStore.createOrUpdateProperty(property: Property<T>) = if (property.name in this) {
    updateProperty(property)
} else {
    this += property
}

/**
 * Retrieves a property by name, throwing an exception if not found.
 *
 * This is useful when a property is required and its absence indicates a configuration error.
 * Prefer this over nullable [PropertyStore.get] when you want to fail fast on missing properties.
 *
 * Example:
 * ```kotlin
 * // Require a property to exist
 * val apiKeyProperty = store.getPropertyOrThrow<String>("apiKey")
 * val apiKey = apiKeyProperty.value
 *
 * // This will throw if the property doesn't exist
 * val requiredConfig = store.getPropertyOrThrow<Int>("requiredSetting")
 * ```
 *
 * @param T the expected type of the property
 * @param name the name of the property to retrieve
 * @return the property with the specified name
 * @throws PropertyNotFoundException if no property with the given name exists
 */
suspend fun <T> PropertyStore.getPropertyOrThrow(name: String): Property<T> = get(name) ?: throw PropertyNotFoundException(name)

/**
 * Retrieves a property's value directly, without the Property wrapper.
 *
 * This is a convenience method that combines property lookup with value extraction.
 * Returns `null` if the property doesn't exist or if the property's value is `null`.
 *
 * Example:
 * ```kotlin
 * // Get value directly
 * val timeout: Int? = store.getPropertyValue("timeout")
 * val region: String? = store.getPropertyValue("region")
 *
 * // Use with null-safety
 * val retries = store.getPropertyValue<Int>("maxRetries") ?: 3
 * ```
 *
 * @param T the expected type of the property value
 * @param name the name of the property
 * @return the property value if found, `null` otherwise
 */
suspend fun <T> PropertyStore.getPropertyValue(name: String): T? = get<T>(name)?.value

/**
 * Retrieves a property's value, or returns a default if not found.
 *
 * This is a type-safe convenience method that combines property lookup with null handling.
 * If the property doesn't exist or its value is `null`, returns the provided default value.
 *
 * Example:
 * ```kotlin
 * val maxRetries = store.getPropertyValueOrDefault("maxRetries", 3)
 * val timeout = store.getPropertyValueOrDefault("timeout", 5000L)
 * val enabled = store.getPropertyValueOrDefault("cacheEnabled", false)
 * ```
 *
 * @param T the expected type of the property value (inferred from default)
 * @param name the name of the property to retrieve
 * @param default the value to return if property is not found or value is null
 * @return the property value if found and non-null, otherwise [default]
 */
suspend fun <T> PropertyStore.getPropertyValueOrDefault(
    name: String,
    default: T,
): T = getPropertyValue(name) ?: default

/**
 * Returns the number of properties in the store.
 *
 * This is a convenience method that counts the entries from [PropertyStore.getAll].
 * Note that this may be an expensive operation for stores backed by external storage,
 * as it retrieves all properties.
 *
 * Example:
 * ```kotlin
 * val propertyCount = store.count()
 * logger.info("Store contains $propertyCount properties")
 *
 * if (store.count() > 1000) {
 *     logger.warn("Property store is getting large, consider cleanup")
 * }
 * ```
 *
 * @return the number of properties in the store
 */
suspend fun PropertyStore.count(): Int = getAll().size
