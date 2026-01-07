package com.yonatankarp.ff4k.property

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for date only values.
 *
 * @property name Unique name of the property
 * @property value Current local date value
 * @property description Optional description
 * @property fixedValues Set of allowed local date values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("localDate")
data class PropertyLocalDate(
    override val name: String,
    override val value: LocalDate,
    override val description: String? = null,
    override val fixedValues: Set<LocalDate> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<LocalDate>
