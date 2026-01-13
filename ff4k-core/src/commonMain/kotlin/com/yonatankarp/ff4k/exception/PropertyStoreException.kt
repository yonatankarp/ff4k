package com.yonatankarp.ff4k.exception

/**
 * Base exception for all property-related operations.
 *
 * This sealed exception hierarchy represents errors that can occur when working with
 * [Property][com.yonatankarp.ff4k.property.Property] objects attached to features.
 * Being sealed allows for exhaustive when expressions when handling property-specific errors.
 *
 * Common scenarios that throw these exceptions:
 * - Attempting to access non-existent properties ([PropertyNotFoundException])
 * - Property validation failures
 * - Type mismatches in property access
 *
 * @param message The detail message describing the exception
 * @param cause The underlying cause of this exception, or null if none
 *
 * @see com.yonatankarp.ff4k.core.Feature.getProperty
 * @see com.yonatankarp.ff4k.property.Property
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
sealed class PropertyStoreException(message: String, cause: Throwable? = null) : FF4kException(message, cause)

/**
 * Thrown when attempting to retrieve a property that does not exist on a feature.
 *
 * This exception is thrown by [Feature.getProperty()][com.yonatankarp.ff4k.core.Feature.getProperty]
 * when the requested property name is not found in the feature's custom properties map.
 *
 * To avoid this exception, check if the property exists before retrieving it by inspecting
 * [Feature.customProperties][com.yonatankarp.ff4k.core.Feature.customProperties] or using
 * the [Feature.propertyNames][com.yonatankarp.ff4k.core.propertyNames] extension property.
 *
 * Example:
 * ```kotlin
 * val feature = Feature(uid = "my-feature")
 *
 * // Safe approach - check existence first
 * if ("maxRetries" in feature.propertyNames) {
 *     val property = feature.getProperty<Int>("maxRetries")
 * }
 *
 * // Or handle the exception
 * try {
 *     val property = feature.getProperty<Int>("maxRetries")
 * } catch (e: PropertyNotFoundException) {
 *     logger.warn("Property not found: ${e.message}")
 * }
 * ```
 *
 * @param propertyId The name/identifier of the property that was not found
 *
 * @see com.yonatankarp.ff4k.core.Feature.getProperty
 * @see com.yonatankarp.ff4k.core.Feature.customProperties
 */
class PropertyNotFoundException(propertyId: String) : PropertyStoreException("Property not found: $propertyId")

/**
 * Thrown when attempting to add a property that already exists in the store.
 *
 * This exception is thrown by property store operations when trying to create or add
 * a property with a name that is already present in the store. This prevents accidental
 * overwrites and maintains data integrity.
 *
 * To avoid this exception, either:
 * - Check if the property exists before adding it
 * - Use an update operation instead of create/add
 * - Remove the existing property first, then add the new one
 *
 * Example:
 * ```kotlin
 * val store = InMemoryPropertyStore()
 *
 * // Safe approach - check existence first
 * if ("timeout" !in store) {
 *     store += intProperty("timeout") { value = 30 }
 * }
 *
 * // Or handle the exception
 * try {
 *     store += intProperty("timeout") { value = 30 }
 * } catch (e: PropertyAlreadyExistsException) {
 *     store.updateProperty(intProperty("timeout") { value = 30 })
 * }
 * ```
 *
 * @param propertyName The name of the property that already exists
 *
 * @see com.yonatankarp.ff4k.core.PropertyStore.plusAssign
 * @see com.yonatankarp.ff4k.core.PropertyStore.updateProperty
 */
class PropertyAlreadyExistsException(propertyName: String) : PropertyStoreException("Property already exists: $propertyName")
