package com.yonatankarp.ff4k.property

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Instant values.
 *
 * @property name Unique name of the property
 * @property value Current instant value
 * @property description Optional description
 * @property fixedValues Set of allowed instant values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("instant")
data class PropertyInstant(
    override val name: String,
    override val value: Instant,
    override val description: String? = null,
    override val fixedValues: Set<Instant> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Instant> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
