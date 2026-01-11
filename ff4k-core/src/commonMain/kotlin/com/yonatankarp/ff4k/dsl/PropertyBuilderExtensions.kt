package com.yonatankarp.ff4k.dsl

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
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
 * Type-specific DSL functions for creating properties.
 *
 * This file provides strongly-typed property creation functions that return
 * concrete property types (PropertyString, PropertyInt, etc.) rather than
 * generic Property<T>.
 *
 * ## Available Functions
 *
 * ### Primitives
 * - [stringProperty] - Creates PropertyString
 * - [intProperty] - Creates PropertyInt
 * - [longProperty] - Creates PropertyLong
 * - [shortProperty] - Creates PropertyShort
 * - [byteProperty] - Creates PropertyByte
 * - [floatProperty] - Creates PropertyFloat
 * - [doubleProperty] - Creates PropertyDouble
 * - [booleanProperty] - Creates PropertyBoolean
 *
 * ### Date/Time
 * - [instantProperty] - Creates PropertyInstant
 * - [localDateProperty] - Creates PropertyLocalDate
 * - [localDateTimeProperty] - Creates PropertyLocalDateTime
 *
 * ### Arbitrary Precision
 * - [bigDecimalProperty] - Creates PropertyBigDecimal
 * - [bigIntegerProperty] - Creates PropertyBigInteger
 *
 * ### Custom Types
 * - [logLevelProperty] - Creates PropertyLogLevel
 *
 * ## Usage
 *
 * These functions are primarily used for:
 * 1. Creating standalone properties for PropertyStore
 * 2. Creating properties to add to features (using the property(Property) method)
 *
 * For inline property creation within features, use the generic property() method instead.
 *
 * @author Yonatan Karp-Rudin
 */

/**
 * Creates a String property using DSL.
 *
 * Example:
 * ```kotlin
 * val apiKey = stringProperty("api.key") {
 *     value = "secret-key-123"
 *     description = "API authentication key"
 *     readOnly = true
 * }
 * ```
 *
 * With fixed values:
 * ```kotlin
 * val environment = stringProperty("environment") {
 *     value = "production"
 *     fixedValues {
 *         +"development"
 *         +"staging"
 *         +"production"
 *     }
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyString] instance
 */
fun stringProperty(
    name: String,
    block: PropertyBuilder<String>.() -> Unit,
): PropertyString = buildTypedProperty<String, PropertyString>(name, block)

/**
 * Creates an Int property using DSL.
 *
 * Example:
 * ```kotlin
 * val maxRetries = intProperty("max.retries") {
 *     value = 3
 *     description = "Maximum number of retry attempts"
 *     fixedValues {
 *         add(1)
 *         add(3)
 *         add(5)
 *         add(10)
 *     }
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyInt] instance
 */
fun intProperty(
    name: String,
    block: PropertyBuilder<Int>.() -> Unit,
): PropertyInt = buildTypedProperty<Int, PropertyInt>(name, block)

/**
 * Creates a Long property using DSL.
 *
 * Example:
 * ```kotlin
 * val maxFileSize = longProperty("max.file.size") {
 *     value = 1024L * 1024L * 100L  // 100 MB
 *     description = "Maximum file upload size in bytes"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyLong] instance
 */
fun longProperty(
    name: String,
    block: PropertyBuilder<Long>.() -> Unit,
): PropertyLong = buildTypedProperty<Long, PropertyLong>(name, block)

/**
 * Creates a Short property using DSL.
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyShort] instance
 */
fun shortProperty(
    name: String,
    block: PropertyBuilder<Short>.() -> Unit,
): PropertyShort = buildTypedProperty<Short, PropertyShort>(name, block)

/**
 * Creates a Byte property using DSL.
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyByte] instance
 */
fun byteProperty(
    name: String,
    block: PropertyBuilder<Byte>.() -> Unit,
): PropertyByte = buildTypedProperty<Byte, PropertyByte>(name, block)

/**
 * Creates a Float property using DSL.
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyFloat] instance
 */
fun floatProperty(
    name: String,
    block: PropertyBuilder<Float>.() -> Unit,
): PropertyFloat = buildTypedProperty<Float, PropertyFloat>(name, block)

/**
 * Creates a Double property using DSL.
 *
 * Example:
 * ```kotlin
 * val threshold = doubleProperty("success.threshold") {
 *     value = 0.95
 *     description = "Success rate threshold (0.0 to 1.0)"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyDouble] instance
 */
fun doubleProperty(
    name: String,
    block: PropertyBuilder<Double>.() -> Unit,
): PropertyDouble = buildTypedProperty<Double, PropertyDouble>(name, block)

/**
 * Creates a Boolean property using DSL.
 *
 * Example:
 * ```kotlin
 * val featureEnabled = booleanProperty("feature.enabled") {
 *     value = true
 *     description = "Enable new feature"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyBoolean] instance
 */
fun booleanProperty(
    name: String,
    block: PropertyBuilder<Boolean>.() -> Unit,
): PropertyBoolean = buildTypedProperty<Boolean, PropertyBoolean>(name, block)

/**
 * Creates a BigDecimal property using DSL for arbitrary-precision decimal numbers.
 *
 * Example:
 * ```kotlin
 * val price = bigDecimalProperty("product.price") {
 *     value = BigDecimal.parseString("19.99")
 *     description = "Product price"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyBigDecimal] instance
 */
fun bigDecimalProperty(
    name: String,
    block: PropertyBuilder<BigDecimal>.() -> Unit,
): PropertyBigDecimal = buildTypedProperty<BigDecimal, PropertyBigDecimal>(name, block)

/**
 * Creates a BigInteger property using DSL for arbitrary-precision integers.
 *
 * Example:
 * ```kotlin
 * val largeNumber = bigIntegerProperty("large.id") {
 *     value = BigInteger.parseString("123456789012345678901234567890")
 *     description = "Very large identifier"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyBigInteger] instance
 */
fun bigIntegerProperty(
    name: String,
    block: PropertyBuilder<BigInteger>.() -> Unit,
): PropertyBigInteger = buildTypedProperty<BigInteger, PropertyBigInteger>(name, block)

/**
 * Creates an Instant property using DSL for UTC timestamps.
 *
 * Example:
 * ```kotlin
 * val createdAt = instantProperty("record.created") {
 *     value = Instant.parse("2023-01-01T00:00:00Z")
 *     description = "Record creation timestamp"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyInstant] instance
 */
fun instantProperty(
    name: String,
    block: PropertyBuilder<Instant>.() -> Unit,
): PropertyInstant = buildTypedProperty<Instant, PropertyInstant>(name, block)

/**
 * Creates a LocalDate property using DSL for date values without time.
 *
 * Example:
 * ```kotlin
 * val birthDate = localDateProperty("user.birth.date") {
 *     value = LocalDate.parse("1990-01-15")
 *     description = "User birth date"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyLocalDate] instance
 */
fun localDateProperty(
    name: String,
    block: PropertyBuilder<LocalDate>.() -> Unit,
): PropertyLocalDate = buildTypedProperty<LocalDate, PropertyLocalDate>(name, block)

/**
 * Creates a LocalDateTime property using DSL for date-time values without timezone.
 *
 * Example:
 * ```kotlin
 * val eventTime = localDateTimeProperty("event.scheduled") {
 *     value = LocalDateTime.parse("2023-12-25T18:00:00")
 *     description = "Event scheduled time"
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyLocalDateTime] instance
 */
fun localDateTimeProperty(
    name: String,
    block: PropertyBuilder<LocalDateTime>.() -> Unit,
): PropertyLocalDateTime = buildTypedProperty<LocalDateTime, PropertyLocalDateTime>(name, block)

/**
 * Creates a LogLevel property using DSL.
 *
 * Example:
 * ```kotlin
 * val logLevel = logLevelProperty("application.log.level") {
 *     value = PropertyLogLevel.LogLevel.INFO
 *     description = "Application logging level"
 *     fixedValues {
 *         +PropertyLogLevel.LogLevel.DEBUG
 *         +PropertyLogLevel.LogLevel.INFO
 *         +PropertyLogLevel.LogLevel.WARN
 *         +PropertyLogLevel.LogLevel.ERROR
 *     }
 * }
 * ```
 *
 * @param name Unique identifier for the property
 * @param block Configuration block
 * @return Configured [PropertyLogLevel] instance
 */
fun logLevelProperty(
    name: String,
    block: PropertyBuilder<PropertyLogLevel.LogLevel>.() -> Unit,
): PropertyLogLevel = buildTypedProperty<PropertyLogLevel.LogLevel, PropertyLogLevel>(name, block)

/**
 * Helper function to build a typed property with type-safe casting.
 * Ensures the built property matches the expected type and provides clear error messages if it doesn't.
 */
internal inline fun <T : Any, reified P : Any> buildTypedProperty(
    name: String,
    block: PropertyBuilder<T>.() -> Unit,
): P {
    val built = PropertyBuilder<T>(name).apply(block).build()
    return built as? P ?: error("Property '$name': expected ${P::class.simpleName}, got ${built::class.simpleName}")
}
