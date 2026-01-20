package com.yonatankarp.ff4k.property

/**
 * Represents a strongly-typed configuration property.
 *
 * Properties are key-value pairs that provide a way to manage configuration settings
 * separately from feature flags. While feature flags are typically boolean (on/off),
 * properties can hold values of various types (String, Int, Boolean, etc.).
 *
 * ## Use Cases
 * - **Configuration**: API URLs, timeouts, connection limits
 * - **Feature Metadata**: Additional context for feature flags (e.g., threshold for a rollout)
 * - **Business Logic**: Pricing parameters, tax rates, localized strings
 *
 * ## Capabilities
 * - **Type Safety**: Properties are strongly typed
 * - **Fixed Values**: Can constrain values to a specific set (enum-like behavior)
 * - **Immutability**: Properties are immutable value objects
 * - **Serialization**: Built-in support for JSON serialization
 *
 * @param T The type of the property value.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
interface Property<T> {
    /**
     * Unique name of the property.
     */
    val name: String

    /**
     * Current value of the property.
     */
    val value: T

    /**
     * Optional human-readable description of the property.
     */
    val description: String?

    /**
     * Set of allowed values. If not empty, the property value must be one of these values.
     */
    val fixedValues: Set<T>

    /**
     * Indicates whether this property is read-only.
     * Some stores do not allow property edition.
     */
    val readOnly: Boolean

    /**
     * Checks if this property has fixed values defined.
     */
    val hasFixedValues: Boolean
        get() = fixedValues.isNotEmpty()
}
