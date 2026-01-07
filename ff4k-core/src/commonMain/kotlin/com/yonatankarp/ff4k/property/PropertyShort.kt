package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for Short values.
 *
 * @property name Unique name of the property
 * @property value Current short value
 * @property description Optional description
 * @property fixedValues Set of allowed short values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("short")
data class PropertyShort(
    override val name: String,
    override val value: Short,
    override val description: String? = null,
    override val fixedValues: Set<Short> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<Short>
