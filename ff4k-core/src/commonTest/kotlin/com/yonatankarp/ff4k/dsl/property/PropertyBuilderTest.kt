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
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Tests for PropertyBuilder DSL.
 */
internal class PropertyBuilderTest :
    FunSpec({

        test("builds String property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_STRING
            }

            // Then
            result.shouldBeInstanceOf<PropertyString> {
                it.name shouldBe PROPERTY_NAME
                it.value shouldBe VALUE_STRING
            }
        }

        test("builds Int property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_INT
            }

            // Then
            result.shouldBeInstanceOf<PropertyInt> {
                it.value shouldBe VALUE_INT
            }
        }

        test("builds Long property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_LONG
            }

            // Then
            result.shouldBeInstanceOf<PropertyLong> {
                it.value shouldBe VALUE_LONG
            }
        }

        test("builds Short property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_SHORT
            }

            // Then
            result.shouldBeInstanceOf<PropertyShort> {
                it.value shouldBe VALUE_SHORT
            }
        }

        test("builds Byte property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_BYTE
            }

            // Then
            result.shouldBeInstanceOf<PropertyByte> {
                it.value shouldBe VALUE_BYTE
            }
        }

        test("builds Float property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_FLOAT
            }

            // Then
            result.shouldBeInstanceOf<PropertyFloat> {
                it.value shouldBe VALUE_FLOAT
            }
        }

        test("builds Double property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_DOUBLE
            }

            // Then
            result.shouldBeInstanceOf<PropertyDouble> {
                it.value shouldBe VALUE_DOUBLE
            }
        }

        test("builds Boolean property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_BOOLEAN
            }

            // Then
            result.shouldBeInstanceOf<PropertyBoolean> {
                it.value shouldBe VALUE_BOOLEAN
            }
        }

        test("builds BigDecimal property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_BIG_DECIMAL
            }

            // Then
            result.shouldBeInstanceOf<PropertyBigDecimal> {
                it.value shouldBe VALUE_BIG_DECIMAL
            }
        }

        test("builds BigInteger property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_BIG_INTEGER
            }

            // Then
            result.shouldBeInstanceOf<PropertyBigInteger> {
                it.value shouldBe VALUE_BIG_INTEGER
            }
        }

        test("builds Instant property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_INSTANT
            }

            // Then
            result.shouldBeInstanceOf<PropertyInstant> {
                it.value shouldBe VALUE_INSTANT
            }
        }

        test("builds LocalDate property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_LOCAL_DATE
            }

            // Then
            result.shouldBeInstanceOf<PropertyLocalDate> {
                it.value shouldBe VALUE_LOCAL_DATE
            }
        }

        test("builds LocalDateTime property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_LOCAL_DATE_TIME
            }

            // Then
            result.shouldBeInstanceOf<PropertyLocalDateTime> {
                it.value shouldBe VALUE_LOCAL_DATE_TIME
            }
        }

        test("builds LogLevel property") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_LOG_LEVEL
            }

            // Then
            result.shouldBeInstanceOf<PropertyLogLevel> {
                it.value shouldBe VALUE_LOG_LEVEL
            }
        }

        test("sets description") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_STRING
                description = DESCRIPTION
            }

            // Then
            result.description shouldBe DESCRIPTION
        }

        test("description defaults to null") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_STRING
            }

            // Then
            result.description.shouldBeNull()
        }

        test("sets readOnly flag") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_STRING
                readOnly = true
            }

            // Then
            result.readOnly.shouldBeTrue()
        }

        test("readOnly defaults to false") {
            // When
            val result = property(PROPERTY_NAME) {
                value = VALUE_STRING
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
                value = VALUE_STRING
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

        private const val VALUE_STRING = "test-value"
        private const val VALUE_INT = 42
        private const val VALUE_LONG = 1000L
        private val VALUE_SHORT: Short = 100
        private val VALUE_BYTE: Byte = 10
        private const val VALUE_FLOAT = 3.14f
        private const val VALUE_DOUBLE = 3.14159
        private const val VALUE_BOOLEAN = true
        private val VALUE_BIG_DECIMAL = BigDecimal.parseString("123.456")
        private val VALUE_BIG_INTEGER = BigInteger.parseString("123456789")
        private val VALUE_INSTANT = Instant.parse("2024-01-15T10:30:00Z")
        private val VALUE_LOCAL_DATE = LocalDate.parse("2024-01-15")
        private val VALUE_LOCAL_DATE_TIME = LocalDateTime.parse("2024-01-15T10:30:00")
        private val VALUE_LOG_LEVEL = PropertyLogLevel.LogLevel.INFO
    }
}
