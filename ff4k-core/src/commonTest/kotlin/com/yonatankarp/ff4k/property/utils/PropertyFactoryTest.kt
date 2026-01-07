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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyFactoryTest {

    // -------------------------
    // String
    // -------------------------

    @Test
    fun `creates string property with default parameters`() {
        // Given
        val name = "apiKey"
        val value = "secret123"

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyString>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates string property with readOnly true`() {
        // Given
        val name = "apiKey"
        val value = "secret123"

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyString>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates string property with fixed values`() {
        // Given
        val name = "env"
        val value = "prod"
        val fixedValues = setOf("dev", "staging", "prod")

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyString>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    @Test
    fun `creates string property with description`() {
        // Given
        val name = "apiKey"
        val value = "secret"
        val description = "API key for external service"

        // When
        val property = property(name, value, description = description)

        // Then
        assertIs<PropertyString>(property)
        assertEquals(description, property.description)
    }

    // -------------------------
    // Int
    // -------------------------

    @Test
    fun `creates int property with default parameters`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyInt>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates int property with readOnly true`() {
        // Given
        val name = "maxRetries"
        val value = 3

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyInt>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates int property with fixed values`() {
        // Given
        val name = "retryCount"
        val value = 3
        val fixedValues = setOf(1, 2, 3, 4)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyInt>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Boolean
    // -------------------------

    @Test
    fun `creates boolean property with default parameters`() {
        // Given
        val name = "enabled"
        val value = true

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyBoolean>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates boolean property with readOnly true`() {
        // Given
        val name = "enabled"
        val value = true

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyBoolean>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates boolean property with fixed values`() {
        // Given
        val name = "featureToggle"
        val value = true
        val fixedValues = setOf(true, false)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyBoolean>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Long
    // -------------------------

    @Test
    fun `creates long property with default parameters`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyLong>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates long property with readOnly true`() {
        // Given
        val name = "timestamp"
        val value = 1234567890L

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyLong>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates long property with fixed values`() {
        // Given
        val name = "allowedIds"
        val value = 10L
        val fixedValues = setOf(10L, 20L, 30L)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyLong>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Float
    // -------------------------

    @Test
    fun `creates float property with default parameters`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyFloat>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates float property with readOnly true`() {
        // Given
        val name = "temperature"
        val value = 98.6f

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyFloat>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates float property with fixed values`() {
        // Given
        val name = "ratio"
        val value = 1.0f
        val fixedValues = setOf(0.5f, 1.0f, 1.5f)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyFloat>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Double
    // -------------------------

    @Test
    fun `creates double property with default parameters`() {
        // Given
        val name = "pi"
        val value = 3.14159

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyDouble>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates double property with readOnly true`() {
        // Given
        val name = "pi"
        val value = 3.14159

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyDouble>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates double property with fixed values`() {
        // Given
        val name = "probability"
        val value = 0.5
        val fixedValues = setOf(0.0, 0.5, 1.0)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyDouble>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Short
    // -------------------------

    @Test
    fun `creates short property with default parameters`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyShort>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates short property with readOnly true`() {
        // Given
        val name = "port"
        val value: Short = 8080

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyShort>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates short property with fixed values`() {
        // Given
        val name = "allowedPorts"
        val value: Short = 8080
        val fixedValues: Set<Short> = setOf(8080, 8081, 9090)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyShort>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Byte
    // -------------------------

    @Test
    fun `creates byte property with default parameters`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyByte>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates byte property with readOnly true`() {
        // Given
        val name = "flag"
        val value: Byte = 127

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyByte>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates byte property with fixed values`() {
        // Given
        val name = "allowedFlags"
        val value: Byte = 1
        val fixedValues: Set<Byte> = setOf(0, 1, 2)

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyByte>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // BigInteger
    // -------------------------

    @Test
    fun `creates bigInteger property with default parameters`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890".toBigInteger()

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyBigInteger>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates bigInteger property with readOnly true`() {
        // Given
        val name = "largeNumber"
        val value = "12345678901234567890".toBigInteger()

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyBigInteger>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates bigInteger property with fixed values`() {
        // Given
        val name = "allowedLargeNumbers"
        val value = "10".toBigInteger()
        val fixedValues = setOf("10".toBigInteger(), "20".toBigInteger(), "30".toBigInteger())

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyBigInteger>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // BigDecimal
    // -------------------------

    @Test
    fun `creates bigDecimal property with default parameters`() {
        // Given
        val name = "preciseValue"
        val value = HIGH_PRECISION_DECIMAL.toBigDecimal()

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyBigDecimal>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates bigDecimal property with readOnly true`() {
        // Given
        val name = "preciseValue"
        val value = HIGH_PRECISION_DECIMAL.toBigDecimal()

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyBigDecimal>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates bigDecimal property with fixed values`() {
        // Given
        val name = "allowedPreciseValues"
        val value = "1.5".toBigDecimal()
        val fixedValues = setOf("1.0".toBigDecimal(), "1.5".toBigDecimal(), "2.0".toBigDecimal())

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyBigDecimal>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // Instant
    // -------------------------

    @Test
    fun `creates instant property with default parameters`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse(TIMESTAMP_ISO)

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyInstant>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates instant property with readOnly true`() {
        // Given
        val name = "createdAt"
        val value = Instant.parse(TIMESTAMP_ISO)

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyInstant>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates instant property with fixed values`() {
        // Given
        val name = "allowedInstants"
        val value = Instant.parse("2024-01-15T10:30:00Z")
        val fixedValues = setOf(
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-15T10:30:00Z"),
            Instant.parse("2024-02-01T00:00:00Z"),
        )

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyInstant>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // LocalDateTime
    // -------------------------

    @Test
    fun `creates localDateTime property with default parameters`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyLocalDateTime>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates localDateTime property with readOnly true`() {
        // Given
        val name = "scheduledAt"
        val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyLocalDateTime>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates localDateTime property with fixed values`() {
        // Given
        val name = "allowedScheduleTimes"
        val value = LocalDateTime.parse("2024-01-15T10:30:00")
        val fixedValues = setOf(
            LocalDateTime.parse("2024-01-01T00:00:00"),
            LocalDateTime.parse("2024-01-15T10:30:00"),
            LocalDateTime.parse("2024-02-01T00:00:00"),
        )

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyLocalDateTime>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // LocalDate
    // -------------------------

    @Test
    fun `creates localDate property with default parameters`() {
        // Given
        val name = "startDate"
        val value = LocalDate.parse(DATE_LOCAL)

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyLocalDate>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates localDate property with readOnly true`() {
        // Given
        val name = "startDate"
        val value = LocalDate.parse(DATE_LOCAL)

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyLocalDate>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates localDate property with fixed values`() {
        // Given
        val name = "billingDate"
        val value = LocalDate.parse("2024-01-15")
        val fixedValues = setOf(
            LocalDate.parse("2024-01-01"),
            LocalDate.parse("2024-01-15"),
            LocalDate.parse("2024-02-01"),
        )

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyLocalDate>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    // -------------------------
    // LogLevel
    // -------------------------

    @Test
    fun `creates logLevel property with default parameters`() {
        // Given
        val name = "level"
        val value = PropertyLogLevel.LogLevel.INFO

        // When
        val property = property(name, value)

        // Then
        assertIs<PropertyLogLevel>(property)
        assertDefaults(property, name, value)
    }

    @Test
    fun `creates logLevel property with readOnly true`() {
        // Given
        val name = "level"
        val value = PropertyLogLevel.LogLevel.DEBUG

        // When
        val property = property(name, value, readOnly = true)

        // Then
        assertIs<PropertyLogLevel>(property)
        assertTrue(property.readOnly)
    }

    @Test
    fun `creates logLevel property with fixed values`() {
        // Given
        val name = "level"
        val value = PropertyLogLevel.LogLevel.ERROR
        val fixedValues = setOf(
            PropertyLogLevel.LogLevel.INFO,
            PropertyLogLevel.LogLevel.WARN,
            PropertyLogLevel.LogLevel.ERROR,
        )

        // When
        val property = property(name, value, fixedValues = fixedValues)

        // Then
        assertIs<PropertyLogLevel>(property)
        assertEquals(fixedValues, property.fixedValues)
    }

    @Test
    fun `creates logLevel property with description`() {
        // Given
        val name = "level"
        val value = PropertyLogLevel.LogLevel.WARN
        val description = "Controls application logging verbosity"

        // When
        val property = property(name, value, description = description)

        // Then
        assertIs<PropertyLogLevel>(property)
        assertEquals(description, property.description)
    }

    // -------------------------
    // Unsupported type
    // -------------------------

    @Test
    fun `throws for unsupported property type`() {
        // Given
        data class Unsupported(val x: Int)

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            property("u", Unsupported(1))
        }
    }

    // -------------------------
    // Helpers
    // -------------------------

    private fun <T> assertDefaults(property: Property<T>, expectedName: String, expectedValue: T) {
        assertEquals(expectedName, property.name)
        assertEquals(expectedValue, property.value)
        assertNull(property.description)
        assertEquals(emptySet(), property.fixedValues)
        assertTrue(property.readOnly.not())
    }

    companion object {
        private const val TIMESTAMP_ISO = "2024-01-15T10:30:00Z"
        private const val TIMESTAMP_LOCAL = "2024-01-15T10:30:00"
        private const val DATE_LOCAL = "2024-01-15"
        private const val HIGH_PRECISION_DECIMAL = "123.456789012345"
    }
}
