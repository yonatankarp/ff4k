package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Byte values.
 *
 * @property name Unique name of the property
 * @property value Current byte value
 * @property description Optional description
 * @property fixedValues Set of allowed byte values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("byte")
data class PropertyByte(
    override val name: String,
    override val value: Byte,
    override val description: String? = null,
    override val fixedValues: Set<Byte> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Byte>
