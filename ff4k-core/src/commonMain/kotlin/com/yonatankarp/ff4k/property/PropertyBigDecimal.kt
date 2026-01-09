package com.yonatankarp.ff4k.property

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for [com.ionspin.kotlin.bignum.decimal.BigDecimal] values.
 *
 * @property name Unique name of the property
 * @property value Current [com.ionspin.kotlin.bignum.decimal.BigDecimal] value (stored as String)
 * @property description Optional description
 * @property fixedValues Set of allowed [com.ionspin.kotlin.bignum.decimal.BigDecimal] values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("bigDecimal")
data class PropertyBigDecimal(
    override val name: String,
    @Contextual override val value: BigDecimal,
    override val description: String? = null,
    override val fixedValues: Set<@Contextual BigDecimal> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<BigDecimal> {
    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
