package com.yonatankarp.ff4k.property.dsl

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.property.PropertyBigDecimal
import com.yonatankarp.ff4k.property.PropertyBigInteger
import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyByte
import com.yonatankarp.ff4k.property.PropertyDouble
import com.yonatankarp.ff4k.property.PropertyFloat
import com.yonatankarp.ff4k.property.PropertyInstant
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyLocalDate
import com.yonatankarp.ff4k.property.PropertyLocalDateTime
import com.yonatankarp.ff4k.property.PropertyLogLevel
import com.yonatankarp.ff4k.property.PropertyLong
import com.yonatankarp.ff4k.property.PropertyShort
import com.yonatankarp.ff4k.property.PropertyString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Generic DSL builder for creating Property instances.
 *
 * Example:
 * ```
 * val property = stringProperty("log.level") {
 *     value = "INFO"
 *     description = "Logging level"
 *     fixedValues {
 *         +"TRACE"
 *         +"DEBUG"
 *         +"INFO"
 *         +"WARN"
 *         +"ERROR"
 *     }
 *     readOnly = false
 * }
 * ```
 *
 * @param T The type of the property value
 * @param name The unique name of the property
 *
 * @author Yonatan Karp-Rudin
 */
@PropertyDsl
class PropertyBuilder<T>(private val name: String) {
    /**
     * The value of the property.
     */
    var value: T? = null

    /**
     * Optional human-readable description of the property.
     */
    var description: String? = null

    /**
     * Set of allowed values. If not empty, the property value must be one of these values.
     */
    var fixedValues: Set<T> = emptySet()

    /**
     * Indicates whether this property is read-only.
     */
    var readOnly: Boolean = false

    /**
     * DSL function for configuring fixed values.
     *
     * Example:
     * ```
     * fixedValues {
     *     +"value1"
     *     +"value2"
     * }
     * ```
     */
    fun fixedValues(block: FixedValuesBuilder<T>.() -> Unit) {
        this.fixedValues = FixedValuesBuilder<T>().apply(block).build()
    }

    /**
     * Builds a Property instance based on the configured type.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun build(): Property<T> {
        val actualValue = value ?: throw IllegalStateException("Property value must be set for property '$name'")

        return when (actualValue) {
            is String -> PropertyString(name, actualValue, description, fixedValues as Set<String>, readOnly)
            is Int -> PropertyInt(name, actualValue, description, fixedValues as Set<Int>, readOnly)
            is Long -> PropertyLong(name, actualValue, description, fixedValues as Set<Long>, readOnly)
            is Short -> PropertyShort(name, actualValue, description, fixedValues as Set<Short>, readOnly)
            is Byte -> PropertyByte(name, actualValue, description, fixedValues as Set<Byte>, readOnly)
            is Float -> PropertyFloat(name, actualValue, description, fixedValues as Set<Float>, readOnly)
            is Double -> PropertyDouble(name, actualValue, description, fixedValues as Set<Double>, readOnly)
            is Boolean -> PropertyBoolean(name, actualValue, description, fixedValues as Set<Boolean>, readOnly)
            is BigDecimal -> PropertyBigDecimal(name, actualValue, description, fixedValues as Set<BigDecimal>, readOnly)
            is BigInteger -> PropertyBigInteger(name, actualValue, description, fixedValues as Set<BigInteger>, readOnly)
            is Instant -> PropertyInstant(name, actualValue, description, fixedValues as Set<Instant>, readOnly)
            is LocalDate -> PropertyLocalDate(name, actualValue, description, fixedValues as Set<LocalDate>, readOnly)
            is LocalDateTime -> PropertyLocalDateTime(name, actualValue, description, fixedValues as Set<LocalDateTime>, readOnly)
            is PropertyLogLevel.LogLevel -> PropertyLogLevel(name, actualValue, description, fixedValues as Set<PropertyLogLevel.LogLevel>, readOnly)
            else -> throw IllegalArgumentException("Unsupported property type: ${actualValue::class}")
        } as Property<T>
    }
}
