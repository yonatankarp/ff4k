package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Double values.
 *
 * @property name Unique name of the property
 * @property value Current double value
 * @property description Optional description
 * @property fixedValues Set of allowed double values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("double")
data class PropertyDouble(
    override val name: String,
    override val value: Double,
    override val description: String? = null,
    override val fixedValues: Set<Double> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Double>
