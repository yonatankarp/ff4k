package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Float values.
 *
 * @property name Unique name of the property
 * @property value Current float value
 * @property description Optional description
 * @property fixedValues Set of allowed float values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("float")
data class PropertyFloat(
    override val name: String,
    override val value: Float,
    override val description: String? = null,
    override val fixedValues: Set<Float> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Float>
