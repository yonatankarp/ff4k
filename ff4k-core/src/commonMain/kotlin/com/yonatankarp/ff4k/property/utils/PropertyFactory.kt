package com.yonatankarp.ff4k.property.utils

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
 * Create the appropriate PropertyXxx instance for the supplied value.
 *
 * Example usage:
 *   val pInt      = property("count", 5)
 *   val pString   = property("name", "hello")
 *   val pClass    = property("klass", String::class)
 *   val pDateTime = property("now", LocalDateTime.now())
 *
 * @param name        Unique identifier for the property.
 * @param value       The initial value.
 * @param description Optional human‑readable description.
 * @param fixedValues Optional set of values that the property may assume.
 * @param readOnly    If true the property cannot be changed after creation.
 *
 * @return A Property subclass that matches the type of `value`.
 */
inline fun <reified T> property(
    name: String,
    value: T,
    description: String? = null,
    fixedValues: Set<T> = emptySet(),
    readOnly: Boolean = false,
): Property<T> = propertyOf(name, value, description, fixedValues, readOnly, T::class)

/**
 * Internal implementation backing the public inline [property] factory.
 *
 * This function exists to satisfy Kotlin's visibility rules for **public inline**
 * APIs: a public inline function may not directly access non-public declarations.
 * By delegating the actual dispatch logic to this `@PublishedApi internal`
 * function, we keep the public API surface minimal while still allowing the
 * inline wrapper to be safely inlined across module boundaries.
 *
 * The function selects the appropriate `PropertyXxx` implementation based on
 * the supplied [kclass] and constructs it using the provided metadata.
 *
 * If an unsupported type is supplied, an [IllegalArgumentException] is thrown.
 *
 * ### API guarantees
 * - The returned property will always have its [Property.name], [Property.value],
 *   [Property.description], [Property.fixedValues], and [Property.readOnly]
 *   fields populated exactly as provided.
 * - No type coercion is performed beyond safe casts required by the dispatch.
 * - This function is **not** part of the public API and may change without notice;
 *   consumers should use the public [property] function instead.
 *
 * @param name        Unique identifier for the property.
 * @param value       The property's initial value.
 * @param description Optional human-readable description.
 * @param fixedValues Optional set of allowed values.
 * @param readOnly    Whether the property is immutable after creation.
 * @param kclass      Runtime class of the value, supplied by the inline wrapper.
 *
 * @return A concrete [Property] implementation matching the supplied type.
 *
 * @throws IllegalArgumentException if [kclass] does not correspond to a supported
 *         property type.
 */
@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <T> propertyOf(
    name: String,
    value: T,
    description: String?,
    fixedValues: Set<T>,
    readOnly: Boolean,
    kclass: kotlin.reflect.KClass<*>,
): Property<T> = when (kclass) {
    String::class -> string(name, value as String, description, fixedValues as Set<String>, readOnly)
    Int::class -> int(name, value as Int, description, fixedValues as Set<Int>, readOnly)
    Boolean::class -> boolean(name, value as Boolean, description, fixedValues as Set<Boolean>, readOnly)
    Long::class -> long(name, value as Long, description, fixedValues as Set<Long>, readOnly)
    Float::class -> float(name, value as Float, description, fixedValues as Set<Float>, readOnly)
    Double::class -> double(name, value as Double, description, fixedValues as Set<Double>, readOnly)
    Short::class -> short(name, value as Short, description, fixedValues as Set<Short>, readOnly)
    Byte::class -> byte(name, value as Byte, description, fixedValues as Set<Byte>, readOnly)
    LocalDate::class -> localDate(name, value as LocalDate, description, fixedValues as Set<LocalDate>, readOnly)
    LocalDateTime::class -> localDateTime(name, value as LocalDateTime, description, fixedValues as Set<LocalDateTime>, readOnly)
    Instant::class -> instant(name, value as Instant, description, fixedValues as Set<Instant>, readOnly)
    PropertyLogLevel.LogLevel::class -> logLevel(name, value as PropertyLogLevel.LogLevel, description, fixedValues as Set<PropertyLogLevel.LogLevel>, readOnly)
    BigInteger::class -> bigInteger(name, value as BigInteger, description, fixedValues as Set<BigInteger>, readOnly)
    BigDecimal::class -> bigDecimal(name, value as BigDecimal, description, fixedValues as Set<BigDecimal>, readOnly)
    else -> throw IllegalArgumentException("Unsupported property type: $kclass")
} as Property<T>

/**
 * Create a PropertyString configured with the given name and value.
 *
 * @param name The property's identifier.
 * @param value The property's string value.
 * @param description Optional human-readable description.
 * @param fixedValues Allowed set of values for the property.
 * @param readOnly When true, the property is immutable.
 * @return A PropertyString configured with the provided name, value, description, allowed values, and read-only flag.
 */
internal fun string(
    name: String,
    value: String,
    description: String?,
    fixedValues: Set<String>,
    readOnly: Boolean,
): PropertyString = PropertyString(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Creates an integer property with the provided name and initial value.
 *
 * @param name The property's identifier.
 * @param value The property's integer value.
 * @param description Optional human-readable description.
 * @param fixedValues Allowed set of values for the property.
 * @param readOnly When true, the property is immutable.
 * @return A PropertyInt configured with the provided name, value, description, fixed values, and read-only flag.
 */
internal fun int(
    name: String,
    value: Int,
    description: String?,
    fixedValues: Set<Int>,
    readOnly: Boolean,
): PropertyInt = PropertyInt(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Constructs a PropertyBoolean with the provided name, value, and optional metadata.
 *
 * @param name The property's identifier.
 * @param value The property's boolean value.
 * @param description Optional human-readable description for the property.
 * @param fixedValues Allowed set of values for the property.
 * @param readOnly Whether the property is read-only.
 * @return A PropertyBoolean initialized with the supplied arguments.
 */
internal fun boolean(
    name: String,
    value: Boolean,
    description: String?,
    fixedValues: Set<Boolean>,
    readOnly: Boolean,
): PropertyBoolean = PropertyBoolean(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Creates a PropertyLong with the provided name, value, and optional metadata.
 *
 * @param name The property identifier.
 * @param value The long value of the property.
 * @param description Optional human-readable description.
 * @param fixedValues Optional set of allowed values for the property.
 * @param readOnly When true, marks the property as immutable.
 * @return A PropertyLong configured with the given parameters.
 */
internal fun long(
    name: String,
    value: Long,
    description: String?,
    fixedValues: Set<Long>,
    readOnly: Boolean,
): PropertyLong = PropertyLong(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyDouble with the provided name, value, and optional metadata.
 *
 * @param name The property's identifier.
 * @param value The property's double value.
 * @param description Optional human-readable description for the property.
 * @param fixedValues Optional set of allowed values for the property.
 * @param readOnly Whether the property is immutable.
 * @return The created PropertyDouble.
 */
internal fun double(
    name: String,
    value: Double,
    description: String?,
    fixedValues: Set<Double>,
    readOnly: Boolean,
): PropertyDouble = PropertyDouble(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Creates a PropertyFloat with the given name, value, and optional metadata.
 *
 * @param name The property's identifier.
 * @param value The initial float value.
 * @param description Optional human-readable description.
 * @param fixedValues Optional set of allowed float values for the property.
 * @param readOnly If `true`, the property is immutable after creation.
 * @return A configured [PropertyFloat] instance.
 */
internal fun float(
    name: String,
    value: Float,
    description: String?,
    fixedValues: Set<Float>,
    readOnly: Boolean,
): PropertyFloat = PropertyFloat(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyByte with the given name and byte value.
 *
 * @param name The property's name.
 * @param value The property's byte value.
 * @param description Optional human-readable description.
 * @param fixedValues Optional set of allowed byte values.
 * @param readOnly If `true`, the property is read-only and cannot be modified.
 * @return The created PropertyByte.
 */
internal fun byte(
    name: String,
    value: Byte,
    description: String?,
    fixedValues: Set<Byte>,
    readOnly: Boolean,
): PropertyByte = PropertyByte(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyShort with the specified name, value, and optional metadata.
 *
 * @param name The property's identifier.
 * @param value The property's short value.
 * @param description Optional user-facing description.
 * @param fixedValues Optional set of allowed values for the property.
 * @param readOnly If `true`, the property is immutable.
 * @return A PropertyShort initialized with the provided arguments.
 */
internal fun short(
    name: String,
    value: Short,
    description: String?,
    fixedValues: Set<Short>,
    readOnly: Boolean,
): PropertyShort = PropertyShort(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Creates a PropertyBigInteger with the supplied metadata and value.
 *
 * @param name The property identifier.
 * @param value The decimal string representation of the BigInteger value.
 * @param description Optional human-readable description of the property.
 * @param fixedValues Optional set of allowed decimal string values for the property.
 * @param readOnly `true` if the property must not be modified, `false` otherwise.
 * @return A PropertyBigInteger populated with the provided name, value, description, fixed values, and readOnly flag.
 */
internal fun bigInteger(
    name: String,
    value: BigInteger,
    description: String?,
    fixedValues: Set<BigInteger>,
    readOnly: Boolean,
): PropertyBigInteger = PropertyBigInteger(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyBigDecimal with the provided name, string decimal value, and optional metadata.
 *
 * @param name The property's identifier.
 * @param value The decimal value encoded as a string.
 * @param description Optional human-readable description.
 * @param fixedValues Optional set of allowed decimal values (each encoded as a string).
 * @param readOnly When true, the property cannot be modified.
 * @return A PropertyBigDecimal initialized with the supplied values.
 */
internal fun bigDecimal(
    name: String,
    value: BigDecimal,
    description: String?,
    fixedValues: Set<BigDecimal>,
    readOnly: Boolean,
): PropertyBigDecimal = PropertyBigDecimal(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a property representing an Instant value.
 *
 * @param name The property's identifier.
 * @param value The initial Instant value.
 * @param description Optional human-readable description.
 * @param fixedValues A set of permitted Instant values for the property.
 * @param readOnly `true` if the property cannot be modified, `false` otherwise.
 * @return A PropertyInstant populated with the provided name, value, description, fixedValues, and readOnly flag.
 */
internal fun instant(
    name: String,
    value: Instant,
    description: String?,
    fixedValues: Set<Instant>,
    readOnly: Boolean,
): PropertyInstant = PropertyInstant(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyLocalDate with the given name, value, optional description, allowed values, and read-only flag.
 *
 * @param name The property's identifier.
 * @param value The initial LocalDate value.
 * @param description Optional human-readable description.
 * @param fixedValues A set of permitted LocalDate values for the property.
 * @param readOnly `true` if the property cannot be modified, `false` otherwise.
 * @return The created PropertyLocalDate.
 */
internal fun localDate(
    name: String,
    value: LocalDate,
    description: String?,
    fixedValues: Set<LocalDate>,
    readOnly: Boolean,
): PropertyLocalDate = PropertyLocalDate(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyLocalDateTime with the given name, value, optional description, allowed values, and read-only flag.
 *
 * @param name The property's identifier.
 * @param value The initial LocalDateTime value.
 * @param description Optional human-readable description.
 * @param fixedValues A set of permitted LocalDateTime values for the property.
 * @param readOnly `true` if the property cannot be modified, `false` otherwise.
 * @return The created PropertyLocalDateTime.
 */
internal fun localDateTime(
    name: String,
    value: LocalDateTime,
    description: String?,
    fixedValues: Set<LocalDateTime>,
    readOnly: Boolean,
): PropertyLocalDateTime = PropertyLocalDateTime(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)

/**
 * Create a PropertyLogLevel with the given name, value, optional description, allowed values, and read-only flag.
 *
 * @param name The property's identifier.
 * @param value The initial PropertyLogLevel.LogLevel value.
 * @param description Optional human-readable description.
 * @param fixedValues A set of permitted PropertyLogLevel.LogLevel values for the property.
 * @param readOnly `true` if the property cannot be modified, `false` otherwise.
 * @return The created PropertyLogLevel.
 */
internal fun logLevel(
    name: String,
    value: PropertyLogLevel.LogLevel,
    description: String?,
    fixedValues: Set<PropertyLogLevel.LogLevel>,
    readOnly: Boolean,
): PropertyLogLevel = PropertyLogLevel(
    name = name,
    value = value,
    description = description,
    fixedValues = fixedValues,
    readOnly = readOnly,
)
