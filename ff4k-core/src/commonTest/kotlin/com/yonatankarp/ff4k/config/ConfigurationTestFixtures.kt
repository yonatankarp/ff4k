package com.yonatankarp.ff4k.config

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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

/**
 * Shared test fixtures for configuration parser tests.
 */
internal object ConfigurationTestFixtures {

    /**
     * A map containing all supported property types for comprehensive testing.
     * This matches the content of `ff4k_configuration_polymorphic.json`.
     */
    val allTestProperties: Map<String, Property<*>> = mapOf(
        "retryLimit" to PropertyInt(
            "retryLimit",
            5,
            description = "Maximum number of retries",
        ),
        "welcomeMessage" to PropertyString(
            "welcomeMessage",
            "Hello, user!",
            description = "Greeting message for users",
        ),
        "piValue" to PropertyDouble(
            "piValue",
            3.14159,
            description = "Pi constant value",
        ),
        "sessionTimeout" to PropertyFloat(
            "sessionTimeout",
            30.0f,
            description = "Session timeout in minutes",
        ),
        "maxUsers" to PropertyLong(
            "maxUsers",
            1_000L,
            description = "Maximum concurrent users allowed",
        ),
        "isBetaEnabled" to PropertyBoolean(
            "isBetaEnabled",
            true,
            description = "Flag to enable beta features",
        ),
        "defaultPriority" to PropertyShort(
            "defaultPriority",
            1.toShort(),
            description = "Default task priority",
        ),
        "defaultByteValue" to PropertyByte(
            "defaultByteValue",
            31.toByte(),
            description = "Default byte configuration",
        ),
        "bigOrderNumber" to PropertyBigInteger(
            "bigOrderNumber",
            BigInteger.parseString("1234567890123456789"),
            description = "Large order identifier",
        ),
        "taxRate" to PropertyBigDecimal(
            "taxRate",
            BigDecimal.parseString("1.9E-1"),
            description = "Tax rate as BigDecimal",
        ),
        "launchDate" to PropertyLocalDate(
            "launchDate",
            LocalDate.parse("2026-01-23"),
            description = "Official launch date",
        ),
        "lastLogin" to PropertyLocalDateTime(
            "lastLogin",
            LocalDateTime.parse("2026-01-23T21:00"),
            description = "User last login timestamp",
        ),
        "eventTimestamp" to PropertyInstant(
            "eventTimestamp",
            Instant.parse("2026-01-23T21:00:00Z"),
            description = "Event occurrence timestamp",
        ),
        "logLevel" to PropertyLogLevel(
            "logLevel",
            PropertyLogLevel.LogLevel.INFO,
            description = "Logging level for the system",
        ),
    )

    /**
     * JSON content for basic configuration testing.
     * Matches `ff4k_configuration.json`.
     */
    // language=json
    val BASIC_CONFIG_JSON = """
        {
          "settings" : {
            "autoCreate": true
          },
          "features" : {
            "dark-mode" : {
              "uid" : "dark-mode",
              "isEnabled" : true,
              "description" : "Whether or not the user is in dark-mode"
            }
          },
          "properties" : {
            "max-retries" : {
              "type" : "int",
              "name" : "max-retries",
              "value" : 3
            }
          }
        }
    """.trimIndent()

    /**
     * JSON content for testing all polymorphic property types.
     * Matches `ff4k_configuration_polymorphic.json`.
     */
    // language=json
    val POLYMORPHIC_CONFIG_JSON = """
        {
          "settings" : { },
          "features": { },
          "properties" : {
            "retryLimit" : {
              "type" : "int",
              "name" : "retryLimit",
              "value" : 5,
              "description" : "Maximum number of retries"
            },
            "welcomeMessage" : {
              "type" : "string",
              "name" : "welcomeMessage",
              "value" : "Hello, user!",
              "description" : "Greeting message for users"
            },
            "piValue" : {
              "type" : "double",
              "name" : "piValue",
              "value" : 3.14159,
              "description" : "Pi constant value"
            },
            "sessionTimeout" : {
              "type" : "float",
              "name" : "sessionTimeout",
              "value" : 30.0,
              "description" : "Session timeout in minutes"
            },
            "maxUsers" : {
              "type" : "long",
              "name" : "maxUsers",
              "value" : 1000,
              "description" : "Maximum concurrent users allowed"
            },
            "isBetaEnabled" : {
              "type" : "boolean",
              "name" : "isBetaEnabled",
              "value" : true,
              "description" : "Flag to enable beta features"
            },
            "defaultPriority" : {
              "type" : "short",
              "name" : "defaultPriority",
              "value" : 1,
              "description" : "Default task priority"
            },
            "defaultByteValue" : {
              "type" : "byte",
              "name" : "defaultByteValue",
              "value" : 31,
              "description" : "Default byte configuration"
            },
            "bigOrderNumber" : {
              "type" : "bigInteger",
              "name" : "bigOrderNumber",
              "value" : "1234567890123456789",
              "description" : "Large order identifier"
            },
            "taxRate" : {
              "type" : "bigDecimal",
              "name" : "taxRate",
              "value" : "1.9E-1",
              "description" : "Tax rate as BigDecimal"
            },
            "launchDate" : {
              "type" : "localDate",
              "name" : "launchDate",
              "value" : "2026-01-23",
              "description" : "Official launch date"
            },
            "lastLogin" : {
              "type" : "localDateTime",
              "name" : "lastLogin",
              "value" : "2026-01-23T21:00",
              "description" : "User last login timestamp"
            },
            "eventTimestamp" : {
              "type" : "instant",
              "name" : "eventTimestamp",
              "value" : "2026-01-23T21:00:00Z",
              "description" : "Event occurrence timestamp"
            },
            "logLevel" : {
              "type" : "logLevel",
              "name" : "logLevel",
              "value" : "INFO",
              "description" : "Logging level for the system"
            }
          }
        }
    """.trimIndent()

    /**
     * JSON content for testing default values when configuration is empty.
     * Matches `ff4k_configuration_with_default_values.json`.
     */
    // language=json
    val DEFAULT_VALUES_CONFIG_JSON = """
        {
        }
    """.trimIndent()
}
