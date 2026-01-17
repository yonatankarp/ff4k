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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PropertyBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBuilderTest {

    @Test
    fun `builds String property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
        }

        // Then
        assertIs<PropertyString>(result)
        assertEquals(PROPERTY_NAME, result.name)
        assertEquals(VALUE_STRING, result.value)
    }

    @Test
    fun `builds Int property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_INT
        }

        // Then
        assertIs<PropertyInt>(result)
        assertEquals(VALUE_INT, result.value)
    }

    @Test
    fun `builds Long property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_LONG
        }

        // Then
        assertIs<PropertyLong>(result)
        assertEquals(VALUE_LONG, result.value)
    }

    @Test
    fun `builds Short property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_SHORT
        }

        // Then
        assertIs<PropertyShort>(result)
        assertEquals(VALUE_SHORT, result.value)
    }

    @Test
    fun `builds Byte property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_BYTE
        }

        // Then
        assertIs<PropertyByte>(result)
        assertEquals(VALUE_BYTE, result.value)
    }

    @Test
    fun `builds Float property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_FLOAT
        }

        // Then
        assertIs<PropertyFloat>(result)
        assertEquals(VALUE_FLOAT, result.value)
    }

    @Test
    fun `builds Double property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_DOUBLE
        }

        // Then
        assertIs<PropertyDouble>(result)
        assertEquals(VALUE_DOUBLE, result.value)
    }

    @Test
    fun `builds Boolean property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_BOOLEAN
        }

        // Then
        assertIs<PropertyBoolean>(result)
        assertEquals(VALUE_BOOLEAN, result.value)
    }

    @Test
    fun `builds BigDecimal property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_BIG_DECIMAL
        }

        // Then
        assertIs<PropertyBigDecimal>(result)
        assertEquals(VALUE_BIG_DECIMAL, result.value)
    }

    @Test
    fun `builds BigInteger property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_BIG_INTEGER
        }

        // Then
        assertIs<PropertyBigInteger>(result)
        assertEquals(VALUE_BIG_INTEGER, result.value)
    }

    @Test
    fun `builds Instant property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_INSTANT
        }

        // Then
        assertIs<PropertyInstant>(result)
        assertEquals(VALUE_INSTANT, result.value)
    }

    @Test
    fun `builds LocalDate property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_LOCAL_DATE
        }

        // Then
        assertIs<PropertyLocalDate>(result)
        assertEquals(VALUE_LOCAL_DATE, result.value)
    }

    @Test
    fun `builds LocalDateTime property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_LOCAL_DATE_TIME
        }

        // Then
        assertIs<PropertyLocalDateTime>(result)
        assertEquals(VALUE_LOCAL_DATE_TIME, result.value)
    }

    @Test
    fun `builds LogLevel property`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_LOG_LEVEL
        }

        // Then
        assertIs<PropertyLogLevel>(result)
        assertEquals(VALUE_LOG_LEVEL, result.value)
    }

    @Test
    fun `sets description`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
            description = DESCRIPTION
        }

        // Then
        assertEquals(DESCRIPTION, result.description)
    }

    @Test
    fun `description defaults to null`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
        }

        // Then
        assertNull(result.description)
    }

    @Test
    fun `sets readOnly flag`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
            readOnly = true
        }

        // Then
        assertTrue(result.readOnly)
    }

    @Test
    fun `readOnly defaults to false`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
        }

        // Then
        assertFalse(result.readOnly)
    }

    @Test
    fun `sets fixedValues using DSL block`() {
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
        assertEquals(setOf("option1", "option2", "option3"), result.fixedValues)
    }

    @Test
    fun `sets fixedValues directly`() {
        // Given
        val fixed = setOf("a", "b", "c")

        // When
        val result = property(PROPERTY_NAME) {
            value = "a"
            fixedValues = fixed
        }

        // Then
        assertEquals(fixed, result.fixedValues)
    }

    @Test
    fun `fixedValues defaults to empty set`() {
        // When
        val result = property(PROPERTY_NAME) {
            value = VALUE_STRING
        }

        // Then
        assertTrue(result.fixedValues.isEmpty())
    }

    @Test
    fun `throws IllegalStateException when value not set`() {
        // When / Then
        assertFailsWith<IllegalStateException> {
            property<String>(PROPERTY_NAME) { }
        }
    }

    @Test
    fun `throws IllegalArgumentException for unsupported type`() {
        // Given
        data class UnsupportedType(val data: String)

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            property(PROPERTY_NAME) {
                value = UnsupportedType("test")
            }
        }
    }

    @Test
    fun `builds property with all options`() {
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
        assertEquals(PROPERTY_NAME, result.name)
        assertEquals(3, result.value)
        assertEquals(DESCRIPTION, result.description)
        assertTrue(result.readOnly)
        assertEquals(setOf(1, 2, 3), result.fixedValues)
    }

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
