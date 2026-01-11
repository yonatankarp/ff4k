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
sealed class PropertyException(message: String, cause: Throwable? = null) : FF4kException(message, cause)

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
class PropertyNotFoundException(propertyId: String) : PropertyException("Property not found: $propertyId")
