package com.yonatankarp.ff4k.property.utils

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.toBigInteger
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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Instant

internal class PropertyFactoryTest :
    FunSpec({

        registerTests(
            typeName = "String",
            defaultValue = "default",
            fixedValues = setOf("a", "b", "default"),
            expectedClass = PropertyString::class,
        )

        registerTests(
            typeName = "Int",
            defaultValue = 42,
            fixedValues = setOf(1, 42, 100),
            expectedClass = PropertyInt::class,
        )

        registerTests(
            typeName = "Boolean",
            defaultValue = true,
            fixedValues = setOf(true, false),
            expectedClass = PropertyBoolean::class,
        )

        registerTests(
            typeName = "Long",
            defaultValue = 1234567890L,
            fixedValues = setOf(10L, 1234567890L),
            expectedClass = PropertyLong::class,
        )

        registerTests(
            typeName = "Float",
            defaultValue = 12.34f,
            fixedValues = setOf(1.1f, 12.34f),
            expectedClass = PropertyFloat::class,
        )

        registerTests(
            typeName = "Double",
            defaultValue = 3.14159,
            fixedValues = setOf(1.0, 3.14159),
            expectedClass = PropertyDouble::class,
        )

        registerTests(
            typeName = "Short",
            defaultValue = 10.toShort(),
            fixedValues = setOf(1.toShort(), 10.toShort()),
            expectedClass = PropertyShort::class,
        )

        registerTests(
            typeName = "Byte",
            defaultValue = 1.toByte(),
            fixedValues = setOf(0.toByte(), 1.toByte()),
            expectedClass = PropertyByte::class,
        )

        registerTests(
            typeName = "BigInteger",
            defaultValue = "12345678901234567890".toBigInteger(),
            fixedValues = setOf("1".toBigInteger(), "12345678901234567890".toBigInteger()),
            expectedClass = PropertyBigInteger::class,
        )

        registerTests(
            typeName = "BigDecimal",
            defaultValue = "123.456789".toBigDecimal(),
            fixedValues = setOf("1.0".toBigDecimal(), "123.456789".toBigDecimal()),
            expectedClass = PropertyBigDecimal::class,
        )

        registerTests(
            typeName = "Instant",
            defaultValue = Instant.parse("2024-01-01T10:00:00Z"),
            fixedValues = setOf(
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-02T10:00:00Z"),
            ),
            expectedClass = PropertyInstant::class,
        )

        registerTests(
            typeName = "LocalDate",
            defaultValue = LocalDate.parse("2024-01-01"),
            fixedValues = setOf(
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-01-02"),
            ),
            expectedClass = PropertyLocalDate::class,
        )

        registerTests(
            typeName = "LocalDateTime",
            defaultValue = LocalDateTime.parse("2024-01-01T10:00:00"),
            fixedValues = setOf(
                LocalDateTime.parse("2024-01-01T10:00:00"),
                LocalDateTime.parse("2024-01-02T10:00:00"),
            ),
            expectedClass = PropertyLocalDateTime::class,
        )

        registerTests(
            typeName = "LogLevel",
            defaultValue = PropertyLogLevel.LogLevel.INFO,
            fixedValues = setOf(
                PropertyLogLevel.LogLevel.INFO,
                PropertyLogLevel.LogLevel.ERROR,
            ),
            expectedClass = PropertyLogLevel::class,
        )

        test("throws for unsupported property type") {
            data class Unsupported(val x: Int)
            shouldThrow<IllegalArgumentException> {
                property("u", Unsupported(1))
            }
        }
    })

/**
 * Helper to verify common property defaults.
 */
private fun <T> assertDefaults(
    property: Property<T>,
    expectedName: String,
    expectedValue: T,
) {
    property.name shouldBe expectedName
    property.value shouldBe expectedValue
    property.description.shouldBeNull()
    property.fixedValues.shouldBeEmpty()
    property.readOnly.shouldBeFalse()
}

/**
 * Generates a suite of tests for a specific property type.
 */
private inline fun <reified T : Any> FunSpec.registerTests(
    typeName: String,
    defaultValue: T,
    fixedValues: Set<T>,
    expectedClass: KClass<*>,
) {
    context("Type: $typeName") {
        test("creates property with default parameters") {
            val name = "prop"
            val property = property(name, defaultValue)

            property::class shouldBe expectedClass
            assertDefaults(property, name, defaultValue)
        }

        test("creates property with readOnly true") {
            val name = "prop"
            val property = property(name, defaultValue, readOnly = true)

            property::class shouldBe expectedClass
            property.readOnly.shouldBeTrue()
        }

        test("creates property with fixed values") {
            val name = "prop"
            val property = property(name, defaultValue, fixedValues = fixedValues)

            property::class shouldBe expectedClass
            property.fixedValues shouldBe fixedValues
        }

        test("creates property with description") {
            val name = "prop"
            val description = "A description"
            val property = property(name, defaultValue, description = description)

            property::class shouldBe expectedClass
            property.description shouldBe description
        }
    }
}
