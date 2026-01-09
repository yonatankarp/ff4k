package com.yonatankarp.ff4k.property.dsl

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
 * Creates a String property using DSL.
 */
fun stringProperty(
    name: String,
    block: PropertyBuilder<String>.() -> Unit,
): PropertyString = buildTypedProperty<String, PropertyString>(name, block)

/**
 * Creates an Int property using DSL.
 */
fun intProperty(
    name: String,
    block: PropertyBuilder<Int>.() -> Unit,
): PropertyInt = buildTypedProperty<Int, PropertyInt>(name, block)

/**
 * Creates a Long property using DSL.
 */
fun longProperty(
    name: String,
    block: PropertyBuilder<Long>.() -> Unit,
): PropertyLong = buildTypedProperty<Long, PropertyLong>(name, block)

/**
 * Creates a Short property using DSL.
 */
fun shortProperty(
    name: String,
    block: PropertyBuilder<Short>.() -> Unit,
): PropertyShort = buildTypedProperty<Short, PropertyShort>(name, block)

/**
 * Creates a Byte property using DSL.
 */
fun byteProperty(
    name: String,
    block: PropertyBuilder<Byte>.() -> Unit,
): PropertyByte = buildTypedProperty<Byte, PropertyByte>(name, block)

/**
 * Creates a Float property using DSL.
 */
fun floatProperty(
    name: String,
    block: PropertyBuilder<Float>.() -> Unit,
): PropertyFloat = buildTypedProperty<Float, PropertyFloat>(name, block)

/**
 * Creates a Double property using DSL.
 */
fun doubleProperty(
    name: String,
    block: PropertyBuilder<Double>.() -> Unit,
): PropertyDouble = buildTypedProperty<Double, PropertyDouble>(name, block)

/**
 * Creates a Boolean property using DSL.
 */
fun booleanProperty(
    name: String,
    block: PropertyBuilder<Boolean>.() -> Unit,
): PropertyBoolean = buildTypedProperty<Boolean, PropertyBoolean>(name, block)

/**
 * Creates a BigDecimal property using DSL.
 */
fun bigDecimalProperty(
    name: String,
    block: PropertyBuilder<BigDecimal>.() -> Unit,
): PropertyBigDecimal = buildTypedProperty<BigDecimal, PropertyBigDecimal>(name, block)

/**
 * Creates a BigInteger property using DSL.
 */
fun bigIntegerProperty(
    name: String,
    block: PropertyBuilder<BigInteger>.() -> Unit,
): PropertyBigInteger = buildTypedProperty<BigInteger, PropertyBigInteger>(name, block)

/**
 * Creates an Instant property using DSL.
 */
fun instantProperty(
    name: String,
    block: PropertyBuilder<Instant>.() -> Unit,
): PropertyInstant = buildTypedProperty<Instant, PropertyInstant>(name, block)

/**
 * Creates a LocalDate property using DSL.
 */
fun localDateProperty(
    name: String,
    block: PropertyBuilder<LocalDate>.() -> Unit,
): PropertyLocalDate = buildTypedProperty<LocalDate, PropertyLocalDate>(name, block)

/**
 * Creates a LocalDateTime property using DSL.
 */
fun localDateTimeProperty(
    name: String,
    block: PropertyBuilder<LocalDateTime>.() -> Unit,
): PropertyLocalDateTime = buildTypedProperty<LocalDateTime, PropertyLocalDateTime>(name, block)

/**
 * Creates a LogLevel property using DSL.
 */
fun logLevelProperty(
    name: String,
    block: PropertyBuilder<PropertyLogLevel.LogLevel>.() -> Unit,
): PropertyLogLevel = buildTypedProperty<PropertyLogLevel.LogLevel, PropertyLogLevel>(name, block)

/**
 * Helper function to build a typed property with type-safe casting.
 * Ensures the built property matches the expected type and provides clear error messages if it doesn't.
 */
private inline fun <T : Any, reified P : Any> buildTypedProperty(
    name: String,
    block: PropertyBuilder<T>.() -> Unit,
): P {
    val built = PropertyBuilder<T>(name).apply(block).build()
    return built as? P ?: error("Property '$name': expected ${P::class.simpleName}, got ${built::class.simpleName}")
}
