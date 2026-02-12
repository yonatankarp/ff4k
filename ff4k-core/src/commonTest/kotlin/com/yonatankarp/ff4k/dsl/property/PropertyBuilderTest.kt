package com.yonatankarp.ff4k.dsl.property

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.yonatankarp.ff4k.dsl.internal.property
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

/**
 * Tests for PropertyBuilder DSL.
 */
internal class PropertyBuilderTest :
    FunSpec({
        registerTests(
            typeName = "String",
            defaultValue = "test-value",
            expectedClass = PropertyString::class,
        )

        registerTests(
            typeName = "Int",
            defaultValue = 42,
            expectedClass = PropertyInt::class,
        )

        registerTests(
            typeName = "Long",
            defaultValue = 1000L,
            expectedClass = PropertyLong::class,
        )

        registerTests(
            typeName = "Short",
            defaultValue = 100.toShort(),
            expectedClass = PropertyShort::class,
        )

        registerTests(
            typeName = "Byte",
            defaultValue = 10.toByte(),
            expectedClass = PropertyByte::class,
        )

        registerTests(
            typeName = "Float",
            defaultValue = 3.14f,
            expectedClass = PropertyFloat::class,
        )

        registerTests(
            typeName = "Double",
            defaultValue = 3.14159,
            expectedClass = PropertyDouble::class,
        )

        registerTests(
            typeName = "Boolean",
            defaultValue = true,
            expectedClass = PropertyBoolean::class,
        )

        registerTests(
            typeName = "BigDecimal",
            defaultValue = BigDecimal.parseString("123.456"),
            expectedClass = PropertyBigDecimal::class,
        )

        registerTests(
            typeName = "BigInteger",
            defaultValue = BigInteger.parseString("123456789"),
            expectedClass = PropertyBigInteger::class,
        )

        registerTests(
            typeName = "Instant",
            defaultValue = Instant.parse("2024-01-15T10:30:00Z"),
            expectedClass = PropertyInstant::class,
        )

        registerTests(
            typeName = "LocalDate",
            defaultValue = LocalDate.parse("2024-01-15"),
            expectedClass = PropertyLocalDate::class,
        )

        registerTests(
            typeName = "LocalDateTime",
            defaultValue = LocalDateTime.parse("2024-01-15T10:30:00"),
            expectedClass = PropertyLocalDateTime::class,
        )

        registerTests(
            typeName = "LogLevel",
            defaultValue = PropertyLogLevel.LogLevel.INFO,
            expectedClass = PropertyLogLevel::class,
        )

        test("sets description") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "test-value"
                description = DESCRIPTION
            }

            // Then
            result.description shouldBe DESCRIPTION
        }

        test("description defaults to null") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "test-value"
            }

            // Then
            result.description.shouldBeNull()
        }

        test("sets readOnly flag") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "test-value"
                readOnly = true
            }

            // Then
            result.readOnly.shouldBeTrue()
        }

        test("readOnly defaults to false") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "test-value"
            }

            // Then
            result.readOnly.shouldBeFalse()
        }

        test("sets fixedValues using DSL block") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "option2"
                fixedValues {
                    +"option1"
                    +"option2"
                    +"option3"
                }
            }

            // Then
            result.fixedValues shouldBe setOf("option1", "option2", "option3")
        }

        test("sets fixedValues directly") {
            // Given
            val fixed = setOf("a", "b", "c")

            // When
            val result = property(PROPERTY_NAME) {
                value = "a"
                fixedValues = fixed
            }

            // Then
            result.fixedValues shouldBe fixed
        }

        test("fixedValues defaults to empty set") {
            // When
            val result = property(PROPERTY_NAME) {
                value = "test-value"
            }

            // Then
            result.fixedValues.shouldBeEmpty()
        }

        test("throws IllegalStateException when value not set") {
            // When / Then
            shouldThrow<IllegalStateException> {
                property<String>(PROPERTY_NAME) { }
            }
        }

        test("throws IllegalArgumentException for unsupported type") {
            // Given
            data class UnsupportedType(val data: String)

            // When / Then
            shouldThrow<IllegalArgumentException> {
                property(PROPERTY_NAME) {
                    value = UnsupportedType("test")
                }
            }
        }

        test("builds property with all options") {
            // When
            val result = property(PROPERTY_NAME) {
                value = 3
                description = DESCRIPTION
                readOnly = true
                fixedValues {
                    add(1)
                    add(2)
                    add(3)
                }
            }

            // Then
            result.name shouldBe PROPERTY_NAME
            result.value shouldBe 3
            result.description shouldBe DESCRIPTION
            result.readOnly.shouldBeTrue()
            result.fixedValues shouldBe setOf(1, 2, 3)
        }
    }) {
    private companion object {
        private const val PROPERTY_NAME = "test-property"
        private const val DESCRIPTION = "Test property description"
    }
}

/**
 * Registers a builder test for a specific type.
 */
private inline fun <reified T : Any> FunSpec.registerTests(
    typeName: String,
    defaultValue: T,
    expectedClass: KClass<*>,
) {
    test("builds $typeName property") {
        // When
        val result = property("test-property") {
            value = defaultValue
        }

        // Then
        result::class shouldBe expectedClass
        result.value shouldBe defaultValue
        result.name shouldBe "test-property"
    }
}
