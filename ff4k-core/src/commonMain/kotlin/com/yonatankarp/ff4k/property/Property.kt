package com.yonatankarp.ff4k.property

/**
 * Abstraction of Property.
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

    /**
     * Validates that the current value is in the fixed values set.
     * Returns true if no fixed values are defined or if the value is valid.
     */
    val isValid: Boolean
        get() = fixedValues.isEmpty() || fixedValues.contains(value)
}
