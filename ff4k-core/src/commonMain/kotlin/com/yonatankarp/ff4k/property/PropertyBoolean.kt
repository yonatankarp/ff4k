package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Boolean values.
 *
 * @property name Unique name of the property
 * @property value Current boolean value
 * @property description Optional description
 * @property fixedValues Set of allowed boolean values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("boolean")
data class PropertyBoolean(
    override val name: String,
    override val value: Boolean,
    override val description: String? = null,
    override val fixedValues: Set<Boolean> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Boolean>
