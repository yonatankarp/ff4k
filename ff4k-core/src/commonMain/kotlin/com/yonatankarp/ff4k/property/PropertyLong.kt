package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Long values.
 *
 * @property name Unique name of the property
 * @property value Current long value
 * @property description Optional description
 * @property fixedValues Set of allowed long values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("long")
data class PropertyLong(
    override val name: String,
    override val value: Long,
    override val description: String? = null,
    override val fixedValues: Set<Long> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Long> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
