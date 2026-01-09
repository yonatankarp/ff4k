package com.yonatankarp.ff4k.property

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for [com.ionspin.kotlin.bignum.integer.BigInteger] values.
 *
 * @property name Unique name of the property
 * @property value Current big [com.ionspin.kotlin.bignum.integer.BigInteger] (stored as String)
 * @property description Optional description
 * @property fixedValues Set of allowed [com.ionspin.kotlin.bignum.integer.BigInteger] values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("bigInteger")
data class PropertyBigInteger(
    override val name: String,
    @Contextual override val value: BigInteger,
    override val description: String? = null,
    override val fixedValues: Set<@Contextual BigInteger> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<BigInteger> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
