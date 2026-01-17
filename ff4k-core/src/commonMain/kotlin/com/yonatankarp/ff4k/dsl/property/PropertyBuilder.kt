package com.yonatankarp.ff4k.dsl.property

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.yonatankarp.ff4k.dsl.core.FF4kDsl
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
 * DSL builder for creating a strongly-typed [Property] instance.
 *
 * Use this builder inside a [PropertiesBuilder] or a `feature` block to define
 * a property with a specific type, value, optional description, fixed allowed values,
 * and read-only status.
 *
 * The builder automatically determines the correct [Property] subclass based on
 * the type of [value].
 *
 * ## Example
 *
 * ```kotlin
 * property("max-retries") {
 *     value = 3
 *     description = "Maximum retry attempts"
 *     fixedValues {
 *         add(1)
 *         add(3)
 *         add(5)
 *     }
 * }
 * ```
 *
 * ## Notes
 * - [value] **must** be set before calling [build], otherwise [IllegalStateException] is thrown.
 * - Supported types: All types that are implemented the [Property] interface internal to the library.
 * - [fixedValues] duplicates are automatically removed.
 * - [readOnly] marks the property as immutable after creation.
 *
 * @param T Type of the property value
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@FF4kDsl
class PropertyBuilder<T> internal constructor(
    private val name: String,
) {
    /**
     * Value of the property. **Required** before building.
     */
    var value: T? = null

    /**
     * Optional description of the property.
     */
    var description: String? = null

    /**
     * Optional set of allowed values. Duplicates are removed automatically.
     */
    var fixedValues: Set<T> = emptySet()

    /**
     * Marks the property as read-only if `true`.
     */
    var readOnly: Boolean = false

    /**
     * DSL helper to define [fixedValues] inline.
     *
     * @param block DSL block to define allowed values using [FixedValuesBuilder]
     */
    fun fixedValues(block: FixedValuesBuilder<T>.() -> Unit) {
        fixedValues = FixedValuesBuilder<T>().apply(block).build()
    }

    /**
     * Builds a concrete [Property] instance based on the type of [value].
     *
     * @return A fully-built [Property] instance of type [T]
     * @throws IllegalStateException if [value] is null
     * @throws IllegalArgumentException if [value] has an unsupported type
     */
    internal fun build(): Property<T> {
        val actualValue = value ?: throw IllegalStateException("Property value must be set for property '$name'")

        @Suppress("UNCHECKED_CAST")
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
