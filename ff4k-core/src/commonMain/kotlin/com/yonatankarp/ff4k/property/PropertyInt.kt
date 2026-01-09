package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Int values.
 *
 * @property name Unique name of the property
 * @property value Current integer value
 * @property description Optional description
 * @property fixedValues Set of allowed integer values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("int")
data class PropertyInt(
    override val name: String,
    override val value: Int,
    override val description: String? = null,
    override val fixedValues: Set<Int> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Int> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
