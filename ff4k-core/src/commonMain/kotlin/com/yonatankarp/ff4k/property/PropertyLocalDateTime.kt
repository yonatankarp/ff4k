package com.yonatankarp.ff4k.property

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for LocalDateTime values.
 *
 * @property name Unique name of the property
 * @property value Current local date time value
 * @property description Optional description
 * @property fixedValues Set of allowed local date time values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("localDateTime")
data class PropertyLocalDateTime(
    override val name: String,
    override val value: LocalDateTime,
    override val description: String? = null,
    override val fixedValues: Set<LocalDateTime> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<LocalDateTime> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
