package com.yonatankarp.ff4k.property.dsl

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.yonatankarp.ff4k.property.PropertyLogLevel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PropertyBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBuilderTest {

    @Test
    fun `stringProperty creates PropertyString with minimal configuration`() {
        // When
        val property = stringProperty("database.url") {
            value = "jdbc:postgresql://localhost/mydb"
        }

        // Then
        assertEquals("database.url", property.name)
        assertEquals("jdbc:postgresql://localhost/mydb", property.value)
        assertNull(property.description)
        assertTrue(property.fixedValues.isEmpty())
        assertFalse(property.readOnly)
    }

    @Test
    fun `stringProperty creates PropertyString with all fields set`() {
        // When
        val property = stringProperty("database.url") {
            value = "jdbc:postgresql://localhost/mydb"
            description = "Database connection URL"
            fixedValues = setOf("jdbc:postgresql://localhost/mydb", "jdbc:mysql://localhost/mydb")
            readOnly = true
        }

        // Then
        assertEquals("database.url", property.name)
        assertEquals("jdbc:postgresql://localhost/mydb", property.value)
        assertEquals("Database connection URL", property.description)
        assertEquals(setOf("jdbc:postgresql://localhost/mydb", "jdbc:mysql://localhost/mydb"), property.fixedValues)
        assertTrue(property.readOnly)
    }

    @Test
    fun `stringProperty with fixedValues builder DSL`() {
        // When
        val property = stringProperty("log.level") {
            value = "INFO"
            description = "Logging level"
            fixedValues {
                +"TRACE"
                +"DEBUG"
                +"INFO"
                +"WARN"
                +"ERROR"
            }
        }

        // Then
        assertEquals("log.level", property.name)
        assertEquals("INFO", property.value)
        assertEquals(setOf("TRACE", "DEBUG", "INFO", "WARN", "ERROR"), property.fixedValues)
    }

    @Test
    fun `stringProperty throws exception when value not set`() {
        // When / Then
        assertFailsWith<IllegalStateException> {
            stringProperty("database.url") {
                description = "Database connection URL"
            }
        }
    }

    @Test
    fun `stringProperty throws exception when value not in fixedValues`() {
        // When / Then
        assertFailsWith<IllegalArgumentException> {
            stringProperty("log.level") {
                value = "INVALID"
                fixedValues {
                    +"INFO"
                    +"WARN"
                    +"ERROR"
                }
            }
        }
    }

    @Test
    fun `intProperty creates PropertyInt`() {
        // When
        val property = intProperty("max.connections") {
            value = 100
            description = "Maximum database connections"
            readOnly = true
        }

        // Then
        assertEquals("max.connections", property.name)
        assertEquals(100, property.value)
        assertEquals("Maximum database connections", property.description)
        assertTrue(property.readOnly)
    }

    @Test
    fun `intProperty with fixedValues using add method`() {
        // When
        val property = intProperty("thread.pool.size") {
            value = 10
            fixedValues {
                add(5)
                add(10)
                add(20)
                add(50)
            }
        }

        // Then
        assertEquals(setOf(5, 10, 20, 50), property.fixedValues)
    }

    @Test
    fun `longProperty creates PropertyLong`() {
        // When
        val property = longProperty("max.file.size") {
            value = 1024L * 1024L * 100L
        }

        // Then
        assertEquals("max.file.size", property.name)
        assertEquals(1024L * 1024L * 100L, property.value)
    }

    @Test
    fun `shortProperty creates PropertyShort`() {
        // When
        val property = shortProperty("port") {
            value = 8080.toShort()
        }

        // Then
        assertEquals("port", property.name)
        assertEquals(8080.toShort(), property.value)
    }

    @Test
    fun `byteProperty creates PropertyByte`() {
        // When
        val property = byteProperty("flag") {
            value = 1.toByte()
        }

        // Then
        assertEquals("flag", property.name)
        assertEquals(1.toByte(), property.value)
    }

    @Test
    fun `floatProperty creates PropertyFloat`() {
        // When
        val property = floatProperty("rate") {
            value = 0.05f
            description = "Interest rate"
        }

        // Then
        assertEquals("rate", property.name)
        assertEquals(0.05f, property.value)
        assertEquals("Interest rate", property.description)
    }

    @Test
    fun `doubleProperty creates PropertyDouble`() {
        // When
        val property = doubleProperty("precision.value") {
            value = 3.14159265359
        }

        // Then
        assertEquals("precision.value", property.name)
        assertEquals(3.14159265359, property.value)
    }

    @Test
    fun `booleanProperty creates PropertyBoolean`() {
        // When
        val property = booleanProperty("feature.enabled") {
            value = true
            description = "Enable new feature"
        }

        // Then
        assertEquals("feature.enabled", property.name)
        assertTrue(property.value)
        assertEquals("Enable new feature", property.description)
    }

    @Test
    fun `bigDecimalProperty creates PropertyBigDecimal`() {
        // When
        val property = bigDecimalProperty("amount") {
            value = BigDecimal.parseString("123.456")
        }

        // Then
        assertEquals("amount", property.name)
        assertEquals(BigDecimal.parseString("123.456"), property.value)
    }

    @Test
    fun `bigIntegerProperty creates PropertyBigInteger`() {
        // When
        val property = bigIntegerProperty("large.number") {
            value = BigInteger.parseString("123456789012345678901234567890")
        }

        // Then
        assertEquals("large.number", property.name)
        assertEquals(BigInteger.parseString("123456789012345678901234567890"), property.value)
    }

    @Test
    fun `instantProperty creates PropertyInstant`() {
        // Given
        val now = Instant.parse("2023-01-01T00:00:00Z")

        // When
        val property = instantProperty("created.at") {
            value = now
        }

        // Then
        assertEquals("created.at", property.name)
        assertEquals(now, property.value)
    }

    @Test
    fun `localDateProperty creates PropertyLocalDate`() {
        // Given
        val date = LocalDate.parse("2023-01-01")

        // When
        val property = localDateProperty("birth.date") {
            value = date
        }

        // Then
        assertEquals("birth.date", property.name)
        assertEquals(date, property.value)
    }

    @Test
    fun `localDateTimeProperty creates PropertyLocalDateTime`() {
        // Given
        val dateTime = LocalDateTime.parse("2023-01-01T12:00:00")

        // When
        val property = localDateTimeProperty("event.time") {
            value = dateTime
        }

        // Then
        assertEquals("event.time", property.name)
        assertEquals(dateTime, property.value)
    }

    @Test
    fun `logLevelProperty creates PropertyLogLevel`() {
        // When
        val property = logLevelProperty("application.log.level") {
            value = PropertyLogLevel.LogLevel.INFO
            fixedValues {
                +PropertyLogLevel.LogLevel.DEBUG
                +PropertyLogLevel.LogLevel.INFO
                +PropertyLogLevel.LogLevel.WARN
                +PropertyLogLevel.LogLevel.ERROR
            }
        }

        // Then
        assertEquals("application.log.level", property.name)
        assertEquals(PropertyLogLevel.LogLevel.INFO, property.value)
        assertTrue(property.fixedValues.contains(PropertyLogLevel.LogLevel.INFO))
    }

    @Test
    fun `readOnly flag works correctly`() {
        // When
        val property = stringProperty("config.value") {
            value = "immutable"
            readOnly = true
        }

        // Then
        assertTrue(property.readOnly)
    }

    @Test
    fun `hasFixedValues returns true when fixed values set`() {
        // When
        val property = stringProperty("environment") {
            value = "dev"
            fixedValues {
                +"dev"
                +"staging"
                +"prod"
            }
        }

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // When
        val property = stringProperty("any.value") {
            value = "something"
        }

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `fixedValues DSL can be called multiple times with last one winning`() {
        // When
        val property = stringProperty("config") {
            value = "option2"
            fixedValues {
                +"option1"
            }
            fixedValues {
                +"option2"
                +"option3"
            }
        }

        // Then
        assertEquals(setOf("option2", "option3"), property.fixedValues)
    }

    @Test
    fun `logLevelProperty validates value against fixedValues`() {
        // When / Then
        assertFailsWith<IllegalArgumentException> {
            logLevelProperty("log.level") {
                value = PropertyLogLevel.LogLevel.TRACE
                fixedValues {
                    +PropertyLogLevel.LogLevel.INFO
                    +PropertyLogLevel.LogLevel.WARN
                    +PropertyLogLevel.LogLevel.ERROR
                }
            }
        }
    }

    @Test
    fun `numeric types work with add method`() {
        // When
        val intProp = intProperty("int.value") {
            value = 10
            fixedValues {
                add(10)
                add(20)
            }
        }

        val longProp = longProperty("long.value") {
            value = 100L
            fixedValues {
                add(100L)
                add(200L)
            }
        }

        val doubleProp = doubleProperty("double.value") {
            value = 1.5
            fixedValues {
                add(1.5)
                add(2.5)
            }
        }

        // Then
        assertEquals(setOf(10, 20), intProp.fixedValues)
        assertEquals(setOf(100L, 200L), longProp.fixedValues)
        assertEquals(setOf(1.5, 2.5), doubleProp.fixedValues)
    }
}
