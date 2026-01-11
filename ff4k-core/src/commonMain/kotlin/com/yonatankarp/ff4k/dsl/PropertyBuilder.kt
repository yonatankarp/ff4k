package com.yonatankarp.ff4k.dsl

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
 * Generic DSL builder for creating [Property] instances with type inference.
 *
 * This builder provides a Kotlin DSL for constructing properties with automatic type detection
 * based on the value provided. Supports all standard Kotlin types, date/time types, and
 * arbitrary-precision numeric types.
 *
 * ## Supported Types
 *
 * - Primitives: String, Int, Long, Short, Byte, Float, Double, Boolean
 * - Date/Time: Instant, LocalDate, LocalDateTime
 * - Arbitrary Precision: BigDecimal, BigInteger
 * - Custom: LogLevel (PropertyLogLevel.LogLevel)
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val property = stringProperty("database.url") {
 *     value = "jdbc:postgresql://localhost/mydb"
 *     description = "Database connection URL"
 * }
 * ```
 *
 * ## With Fixed Values
 *
 * ```kotlin
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
 * }
 * ```
 *
 * ## Read-Only Properties
 *
 * ```kotlin
 * val property = intProperty("max.connections") {
 *     value = 100
 *     description = "Maximum database connections"
 *     readOnly = true
 * }
 * ```
 *
 * ## Numeric Types
 *
 * ```kotlin
 * val intProp = intProperty("retry.count") {
 *     value = 3
 *     fixedValues {
 *         add(1)
 *         add(3)
 *         add(5)
 *     }
 * }
 *
 * val doubleProp = doubleProperty("threshold") {
 *     value = 0.95
 * }
 * ```
 *
 * ## Date/Time Types
 *
 * ```kotlin
 * val instantProp = instantProperty("created.at") {
 *     value = Instant.parse("2023-01-01T00:00:00Z")
 * }
 *
 * val dateProp = localDateProperty("birth.date") {
 *     value = LocalDate.parse("2023-01-01")
 * }
 * ```
 *
 * ## Usage in Features
 *
 * Properties can be added directly to features:
 *
 * ```kotlin
 * val feature = feature("api-config") {
 *     enable()
 *
 *     property("max-requests") {
 *         value = 1000
 *         description = "Maximum requests per hour"
 *     }
 * }
 * ```
 *
 * Or created standalone and stored in a PropertyStore:
 *
 * ```kotlin
 * val property = stringProperty("api.key") {
 *     value = "secret-key"
 *     readOnly = true
 * }
 * propertyStore.create(property)
 * ```
 *
 * @param T The type of the property value
 * @param name The unique name of the property
 *
 * @see FixedValuesBuilder
 * @see Property
 *
 * @author Yonatan Karp-Rudin
 */
@PropertyDsl
class PropertyBuilder<T>(private val name: String) {
    /**
     * The value of the property.
     * Must be set before building the property.
     */
    var value: T? = null

    /**
     * Optional human-readable description of the property.
     */
    var description: String? = null

    /**
     * Set of allowed values. If not empty, the property value must be one of these values.
     * Can be set directly or using the [fixedValues] DSL function.
     */
    var fixedValues: Set<T> = emptySet()

    /**
     * Indicates whether this property is read-only.
     * Defaults to `false`.
     */
    var readOnly: Boolean = false

    /**
     * DSL function for configuring fixed values using a declarative syntax.
     *
     * Provides two ways to add values:
     * - Unary plus operator: `+"value"`
     * - Add method: `add(value)`
     *
     * Example with unary plus:
     * ```kotlin
     * fixedValues {
     *     +"TRACE"
     *     +"DEBUG"
     *     +"INFO"
     * }
     * ```
     *
     * Example with add method:
     * ```kotlin
     * fixedValues {
     *     add(1)
     *     add(3)
     *     add(5)
     * }
     * ```
     *
     * @param block Configuration block for adding fixed values
     * @see FixedValuesBuilder
     */
    fun fixedValues(block: FixedValuesBuilder<T>.() -> Unit) {
        this.fixedValues = FixedValuesBuilder<T>().apply(block).build()
    }

    /**
     * Builds a [Property] instance based on the configured value type.
     *
     * The concrete property type is determined at runtime by examining the value:
     * - String → PropertyString
     * - Int → PropertyInt
     * - Boolean → PropertyBoolean
     * - etc.
     *
     * @return Typed property instance
     * @throws IllegalStateException if value is not set
     * @throws IllegalArgumentException if value type is not supported
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
