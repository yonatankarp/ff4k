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
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

internal class PropertyFactoryTest :
    FunSpec({

        // -------------------------
        // Helpers
        // -------------------------

        fun <T> assertDefaults(property: Property<T>, expectedName: String, expectedValue: T) {
            property.name shouldBe expectedName
            property.value shouldBe expectedValue
            property.description.shouldBeNull()
            property.fixedValues.shouldBeEmpty()
            property.readOnly.shouldBeFalse()
        }

        // -------------------------
        // String
        // -------------------------

        test("creates string property with default parameters") {
            // Given
            val name = "apiKey"
            val value = "secret123"

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyString>()
            assertDefaults(property, name, value)
        }

        test("creates string property with readOnly true") {
            // Given
            val name = "apiKey"
            val value = "secret123"

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyString>()
            property.readOnly.shouldBeTrue()
        }

        test("creates string property with fixed values") {
            // Given
            val name = "env"
            val value = "prod"
            val fixedValues = setOf("dev", "staging", "prod")

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyString>()
            property.fixedValues shouldBe fixedValues
        }

        test("creates string property with description") {
            // Given
            val name = "apiKey"
            val value = "secret"
            val description = "API key for external service"

            // When
            val property = property(name, value, description = description)

            // Then
            property.shouldBeInstanceOf<PropertyString>()
            property.description shouldBe description
        }

        // -------------------------
        // Int
        // -------------------------

        test("creates int property with default parameters") {
            // Given
            val name = "maxRetries"
            val value = 3

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyInt>()
            assertDefaults(property, name, value)
        }

        test("creates int property with readOnly true") {
            // Given
            val name = "maxRetries"
            val value = 3

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyInt>()
            property.readOnly.shouldBeTrue()
        }

        test("creates int property with fixed values") {
            // Given
            val name = "retryCount"
            val value = 3
            val fixedValues = setOf(1, 2, 3, 4)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyInt>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Boolean
        // -------------------------

        test("creates boolean property with default parameters") {
            // Given
            val name = "enabled"
            val value = true

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyBoolean>()
            assertDefaults(property, name, value)
        }

        test("creates boolean property with readOnly true") {
            // Given
            val name = "enabled"
            val value = true

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyBoolean>()
            property.readOnly.shouldBeTrue()
        }

        test("creates boolean property with fixed values") {
            // Given
            val name = "featureToggle"
            val value = true
            val fixedValues = setOf(true, false)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyBoolean>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Long
        // -------------------------

        test("creates long property with default parameters") {
            // Given
            val name = "timestamp"
            val value = 1234567890L

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyLong>()
            assertDefaults(property, name, value)
        }

        test("creates long property with readOnly true") {
            // Given
            val name = "timestamp"
            val value = 1234567890L

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyLong>()
            property.readOnly.shouldBeTrue()
        }

        test("creates long property with fixed values") {
            // Given
            val name = "allowedIds"
            val value = 10L
            val fixedValues = setOf(10L, 20L, 30L)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyLong>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Float
        // -------------------------

        test("creates float property with default parameters") {
            // Given
            val name = "temperature"
            val value = 98.6f

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyFloat>()
            assertDefaults(property, name, value)
        }

        test("creates float property with readOnly true") {
            // Given
            val name = "temperature"
            val value = 98.6f

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyFloat>()
            property.readOnly.shouldBeTrue()
        }

        test("creates float property with fixed values") {
            // Given
            val name = "ratio"
            val value = 1.0f
            val fixedValues = setOf(0.5f, 1.0f, 1.5f)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyFloat>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Double
        // -------------------------

        test("creates double property with default parameters") {
            // Given
            val name = "pi"
            val value = 3.14159

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyDouble>()
            assertDefaults(property, name, value)
        }

        test("creates double property with readOnly true") {
            // Given
            val name = "pi"
            val value = 3.14159

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyDouble>()
            property.readOnly.shouldBeTrue()
        }

        test("creates double property with fixed values") {
            // Given
            val name = "probability"
            val value = 0.5
            val fixedValues = setOf(0.0, 0.5, 1.0)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyDouble>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Short
        // -------------------------

        test("creates short property with default parameters") {
            // Given
            val name = "port"
            val value: Short = 8080

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyShort>()
            assertDefaults(property, name, value)
        }

        test("creates short property with readOnly true") {
            // Given
            val name = "port"
            val value: Short = 8080

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyShort>()
            property.readOnly.shouldBeTrue()
        }

        test("creates short property with fixed values") {
            // Given
            val name = "allowedPorts"
            val value: Short = 8080
            val fixedValues: Set<Short> = setOf(8080, 8081, 9090)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyShort>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Byte
        // -------------------------

        test("creates byte property with default parameters") {
            // Given
            val name = "flag"
            val value: Byte = 127

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyByte>()
            assertDefaults(property, name, value)
        }

        test("creates byte property with readOnly true") {
            // Given
            val name = "flag"
            val value: Byte = 127

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyByte>()
            property.readOnly.shouldBeTrue()
        }

        test("creates byte property with fixed values") {
            // Given
            val name = "allowedFlags"
            val value: Byte = 1
            val fixedValues: Set<Byte> = setOf(0, 1, 2)

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyByte>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // BigInteger
        // -------------------------

        test("creates bigInteger property with default parameters") {
            // Given
            val name = "largeNumber"
            val value = "12345678901234567890".toBigInteger()

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyBigInteger>()
            assertDefaults(property, name, value)
        }

        test("creates bigInteger property with readOnly true") {
            // Given
            val name = "largeNumber"
            val value = "12345678901234567890".toBigInteger()

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyBigInteger>()
            property.readOnly.shouldBeTrue()
        }

        test("creates bigInteger property with fixed values") {
            // Given
            val name = "allowedLargeNumbers"
            val value = "10".toBigInteger()
            val fixedValues = setOf("10".toBigInteger(), "20".toBigInteger(), "30".toBigInteger())

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyBigInteger>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // BigDecimal
        // -------------------------

        test("creates bigDecimal property with default parameters") {
            // Given
            val name = "preciseValue"
            val value = HIGH_PRECISION_DECIMAL.toBigDecimal()

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyBigDecimal>()
            assertDefaults(property, name, value)
        }

        test("creates bigDecimal property with readOnly true") {
            // Given
            val name = "preciseValue"
            val value = HIGH_PRECISION_DECIMAL.toBigDecimal()

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyBigDecimal>()
            property.readOnly.shouldBeTrue()
        }

        test("creates bigDecimal property with fixed values") {
            // Given
            val name = "allowedPreciseValues"
            val value = "1.5".toBigDecimal()
            val fixedValues = setOf("1.0".toBigDecimal(), "1.5".toBigDecimal(), "2.0".toBigDecimal())

            // When
            val property = property(name, value, fixedValues = fixedValues)

            // Then
            property.shouldBeInstanceOf<PropertyBigDecimal>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // Instant
        // -------------------------

        test("creates instant property with default parameters") {
            // Given
            val name = "createdAt"
            val value = Instant.parse(TIMESTAMP_ISO)

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyInstant>()
            assertDefaults(property, name, value)
        }

        test("creates instant property with readOnly true") {
            // Given
            val name = "createdAt"
            val value = Instant.parse(TIMESTAMP_ISO)

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyInstant>()
            property.readOnly.shouldBeTrue()
        }

        test("creates instant property with fixed values") {
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
            property.shouldBeInstanceOf<PropertyInstant>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // LocalDateTime
        // -------------------------

        test("creates localDateTime property with default parameters") {
            // Given
            val name = "scheduledAt"
            val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyLocalDateTime>()
            assertDefaults(property, name, value)
        }

        test("creates localDateTime property with readOnly true") {
            // Given
            val name = "scheduledAt"
            val value = LocalDateTime.parse(TIMESTAMP_LOCAL)

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyLocalDateTime>()
            property.readOnly.shouldBeTrue()
        }

        test("creates localDateTime property with fixed values") {
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
            property.shouldBeInstanceOf<PropertyLocalDateTime>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // LocalDate
        // -------------------------

        test("creates localDate property with default parameters") {
            // Given
            val name = "startDate"
            val value = LocalDate.parse(DATE_LOCAL)

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyLocalDate>()
            assertDefaults(property, name, value)
        }

        test("creates localDate property with readOnly true") {
            // Given
            val name = "startDate"
            val value = LocalDate.parse(DATE_LOCAL)

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyLocalDate>()
            property.readOnly.shouldBeTrue()
        }

        test("creates localDate property with fixed values") {
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
            property.shouldBeInstanceOf<PropertyLocalDate>()
            property.fixedValues shouldBe fixedValues
        }

        // -------------------------
        // LogLevel
        // -------------------------

        test("creates logLevel property with default parameters") {
            // Given
            val name = "level"
            val value = PropertyLogLevel.LogLevel.INFO

            // When
            val property = property(name, value)

            // Then
            property.shouldBeInstanceOf<PropertyLogLevel>()
            assertDefaults(property, name, value)
        }

        test("creates logLevel property with readOnly true") {
            // Given
            val name = "level"
            val value = PropertyLogLevel.LogLevel.DEBUG

            // When
            val property = property(name, value, readOnly = true)

            // Then
            property.shouldBeInstanceOf<PropertyLogLevel>()
            property.readOnly.shouldBeTrue()
        }

        test("creates logLevel property with fixed values") {
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
            property.shouldBeInstanceOf<PropertyLogLevel>()
            property.fixedValues shouldBe fixedValues
        }

        test("creates logLevel property with description") {
            // Given
            val name = "level"
            val value = PropertyLogLevel.LogLevel.WARN
            val description = "Controls application logging verbosity"

            // When
            val property = property(name, value, description = description)

            // Then
            property.shouldBeInstanceOf<PropertyLogLevel>()
            property.description shouldBe description
        }

        // -------------------------
        // Unsupported type
        // -------------------------

        test("throws for unsupported property type") {
            // Given
            data class Unsupported(val x: Int)

            // When / Then
            shouldThrow<IllegalArgumentException> {
                property("u", Unsupported(1))
            }
        }
    }) {
    companion object {
        private const val TIMESTAMP_ISO = "2024-01-15T10:30:00Z"
        private const val TIMESTAMP_LOCAL = "2024-01-15T10:30:00"
        private const val DATE_LOCAL = "2024-01-15"
        private const val HIGH_PRECISION_DECIMAL = "123.456789012345"
    }
}
