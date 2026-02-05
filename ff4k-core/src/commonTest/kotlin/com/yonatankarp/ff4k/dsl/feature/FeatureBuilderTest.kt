package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests for FeatureBuilder DSL.
 */
internal class FeatureBuilderTest :
    FunSpec({

        test("feature creates feature with minimal configuration") {
            // When
            val feature = feature(FEATURE_UID) { }

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled.shouldBeFalse()
            feature.description.shouldBeNull()
            feature.group.shouldBeNull()
            feature.permissions.shouldBeEmpty()
            feature.flippingStrategy.shouldBeNull()
            feature.customProperties.shouldBeEmpty()
        }

        test("feature creates feature with all fields set") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy

            // When
            val feature = feature(FEATURE_UID) {
                isEnabled = true
                description = FEATURE_DESCRIPTION
                group = FEATURE_GROUP
                permission(PERMISSION_ADMIN)
                permission(PERMISSION_USER)
                flippingStrategy = strategy
            }

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled.shouldBeTrue()
            feature.description shouldBe FEATURE_DESCRIPTION
            feature.group shouldBe FEATURE_GROUP
            feature.permissions shouldBe BASIC_PERMISSIONS
            feature.flippingStrategy shouldBe strategy
        }

        test("isEnabled property sets enabled state") {
            // When
            val feature = feature(FEATURE_UID) {
                isEnabled = true
            }

            // Then
            feature.isEnabled.shouldBeTrue()
        }

        test("group property sets group name") {
            // When
            val feature = feature(FEATURE_UID) {
                group = FEATURE_GROUP
            }

            // Then
            feature.group shouldBe FEATURE_GROUP
        }

        test("flippingStrategy property sets strategy") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy

            // When
            val feature = feature(FEATURE_UID) {
                flippingStrategy = strategy
            }

            // Then
            feature.flippingStrategy shouldBe strategy
        }

        test("permission adds single permission") {
            // When
            val feature = feature(FEATURE_UID) {
                permission(PERMISSION_ADMIN)
            }

            // Then
            feature.permissions shouldBe setOf(PERMISSION_ADMIN)
        }

        test("permission adds multiple permissions") {
            // When
            val feature = feature(FEATURE_UID) {
                permission(PERMISSION_ADMIN)
                permission(PERMISSION_USER)
                permission(PERMISSION_MODERATOR)
            }

            // Then
            feature.permissions shouldBe THREE_PERMISSIONS
        }

        test("permissions vararg adds multiple permissions") {
            // When
            val feature = feature(FEATURE_UID) {
                permissions(PERMISSION_ADMIN, PERMISSION_USER, PERMISSION_MODERATOR)
            }

            // Then
            feature.permissions shouldBe THREE_PERMISSIONS
        }

        test("permissions DSL block adds multiple permissions") {
            // When
            val feature = feature(FEATURE_UID) {
                permissions {
                    +PERMISSION_ADMIN
                    +PERMISSION_USER
                    +PERMISSION_MODERATOR
                }
            }

            // Then
            feature.permissions shouldBe THREE_PERMISSIONS
        }

        test("permissions can be added using multiple methods") {
            // When
            val feature = feature(FEATURE_UID) {
                permission(PERMISSION_ADMIN)
                permissions(PERMISSION_USER, PERMISSION_MODERATOR)
                permissions {
                    +PERMISSION_OWNER
                    +PERMISSION_EDITOR
                }
            }

            // Then
            feature.permissions shouldBe ALL_PERMISSIONS
        }

        test("property adds existing property") {
            // Given
            val prop =
                PropertyInt(name = PROPERTY_MAX_RETRIES, value = MAX_RETRIES_VALUE)

            // When
            val feature = feature(FEATURE_UID) {
                property(prop)
            }

            // Then
            feature.customProperties.size shouldBe 1
            feature.customProperties[PROPERTY_MAX_RETRIES] shouldBe prop
        }

        test("property creates inline property with type inference") {
            // When
            val feature = feature(FEATURE_UID) {
                property(PROPERTY_MAX_REQUESTS) {
                    value = MAX_REQUESTS_VALUE
                    description = MAX_REQUESTS_DESCRIPTION
                }
            }

            // Then
            feature.customProperties.size shouldBe 1
            feature.customProperties[PROPERTY_MAX_REQUESTS].shouldNotBeNull {
                name shouldBe PROPERTY_MAX_REQUESTS
                value shouldBe MAX_REQUESTS_VALUE
                description shouldBe MAX_REQUESTS_DESCRIPTION
            }
        }

        test("property creates multiple properties") {
            // When
            val feature = feature(FEATURE_UID) {
                property(PROPERTY_MAX_REQUESTS) {
                    value = MAX_REQUESTS_VALUE
                }
                property(PROPERTY_TIMEOUT_SECONDS) {
                    value = TIMEOUT_VALUE
                }
                property(PROPERTY_API_KEY) {
                    value = API_KEY_VALUE
                }
            }

            // Then
            feature.customProperties.size shouldBe 3
            feature.customProperties[PROPERTY_MAX_REQUESTS]?.value shouldBe MAX_REQUESTS_VALUE
            feature.customProperties[PROPERTY_TIMEOUT_SECONDS]?.value shouldBe TIMEOUT_VALUE
            feature.customProperties[PROPERTY_API_KEY]?.value shouldBe API_KEY_VALUE
        }

        test("property replaces existing property with same name") {
            // Given
            val oldValue = "old-value"
            val newValue = "new-value"

            // When
            val feature = feature(FEATURE_UID) {
                property(PROPERTY_CONFIG) {
                    value = oldValue
                }
                property(PROPERTY_CONFIG) {
                    value = newValue
                }
            }

            // Then
            feature.customProperties.size shouldBe 1
            feature.customProperties[PROPERTY_CONFIG]?.value shouldBe newValue
        }

        test("property supports different types with type inference") {
            // When
            val feature = feature(FEATURE_UID) {
                property("string-prop") {
                    value = STRING_VALUE
                }
                property("int-prop") {
                    value = INT_VALUE
                }
                property("long-prop") {
                    value = LONG_VALUE
                }
                property("double-prop") {
                    value = DOUBLE_VALUE
                }
                property("boolean-prop") {
                    value = BOOLEAN_VALUE
                }
            }

            // Then
            feature.customProperties.size shouldBe 5
            feature.customProperties["string-prop"]?.value shouldBe STRING_VALUE
            feature.customProperties["int-prop"]?.value shouldBe INT_VALUE
            feature.customProperties["long-prop"]?.value shouldBe LONG_VALUE
            feature.customProperties["double-prop"]?.value shouldBe DOUBLE_VALUE
            feature.customProperties["boolean-prop"]?.value shouldBe BOOLEAN_VALUE
        }

        test("property with fixedValues works correctly") {
            // When
            val feature = feature(FEATURE_UID) {
                property(PROPERTY_LOG_LEVEL) {
                    value = LOG_LEVEL_INFO
                    fixedValues {
                        +LOG_LEVEL_DEBUG
                        +LOG_LEVEL_INFO
                        +LOG_LEVEL_WARN
                        +LOG_LEVEL_ERROR
                    }
                }
            }

            // Then
            feature.customProperties[PROPERTY_LOG_LEVEL].shouldNotBeNull {
                fixedValues shouldBe LOG_LEVELS
            }
        }

        test("property with readOnly works correctly") {
            // When
            val feature = feature(FEATURE_UID) {
                property(PROPERTY_CONFIG) {
                    value = CONFIG_VALUE
                    readOnly = true
                }
            }

            // Then
            feature.customProperties[PROPERTY_CONFIG].shouldNotBeNull {
                readOnly.shouldBeTrue()
            }
        }

        test("validation fails when property value not in fixedValues") {
            // Given
            val invalidValue = "INVALID"

            // When / Then
            shouldThrow<IllegalArgumentException> {
                feature(FEATURE_UID) {
                    property(PROPERTY_LOG_LEVEL) {
                        value = invalidValue
                        fixedValues {
                            +LOG_LEVEL_INFO
                            +LOG_LEVEL_WARN
                            +LOG_LEVEL_ERROR
                        }
                    }
                }
            }
        }

        test("complex nested scenario with all features") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy
            val existingProp = PropertyString(
                name = PROPERTY_EXTERNAL_CONFIG,
                value = EXTERNAL_CONFIG_VALUE,
            )

            // When
            val feature = feature(FEATURE_UID) {
                isEnabled = true
                description = FEATURE_DESCRIPTION
                group = FEATURE_GROUP
                flippingStrategy = strategy

                permissions {
                    +PERMISSION_ADMIN
                    +PERMISSION_SUPER_ADMIN
                }
                permission(PERMISSION_OWNER)

                property(existingProp)

                property(PROPERTY_MAX_RETRIES) {
                    value = MAX_RETRIES_VALUE
                    description = MAX_RETRIES_DESCRIPTION
                    fixedValues {
                        RETRIES_FIXED_VALUES.forEach { add(it) }
                    }
                }

                property(PROPERTY_API_ENDPOINT) {
                    value = API_ENDPOINT_VALUE
                    description = API_ENDPOINT_DESCRIPTION
                    readOnly = true
                }

                property(PROPERTY_TIMEOUT_MS) {
                    value = TIMEOUT_MS_VALUE
                }

                property(PROPERTY_FEATURE_ENABLED) {
                    value = FEATURE_ENABLED_VALUE
                }
            }

            // Then
            feature.uid shouldBe FEATURE_UID
            feature.isEnabled.shouldBeTrue()
            feature.description shouldBe FEATURE_DESCRIPTION
            feature.group shouldBe FEATURE_GROUP
            feature.permissions shouldBe ADMIN_PERMISSIONS
            feature.flippingStrategy shouldBe strategy
            feature.customProperties.size shouldBe 5

            // Verify properties
            feature.customProperties[PROPERTY_EXTERNAL_CONFIG] shouldBe existingProp
            feature.customProperties[PROPERTY_MAX_RETRIES]?.value shouldBe MAX_RETRIES_VALUE
            feature.customProperties[PROPERTY_API_ENDPOINT]?.value shouldBe API_ENDPOINT_VALUE
            feature.customProperties[PROPERTY_TIMEOUT_MS]?.value shouldBe TIMEOUT_MS_VALUE
            feature.customProperties[PROPERTY_FEATURE_ENABLED]?.value shouldBe FEATURE_ENABLED_VALUE

            // Verify property details
            feature.customProperties[PROPERTY_MAX_RETRIES].shouldNotBeNull {
                description shouldBe MAX_RETRIES_DESCRIPTION
                fixedValues shouldBe RETRIES_FIXED_VALUES
            }

            feature.customProperties[PROPERTY_API_ENDPOINT].shouldNotBeNull {
                readOnly.shouldBeTrue()
            }
        }
    }) {

    private companion object {
        // Feature constants
        private const val FEATURE_UID = "test-feature"
        private const val FEATURE_DESCRIPTION = "Test feature description"
        private const val FEATURE_GROUP = "test-group"

        // Permission constants
        private const val PERMISSION_ADMIN = "ROLE_ADMIN"
        private const val PERMISSION_USER = "ROLE_USER"
        private const val PERMISSION_MODERATOR = "ROLE_MODERATOR"
        private const val PERMISSION_OWNER = "ROLE_OWNER"
        private const val PERMISSION_EDITOR = "ROLE_EDITOR"
        private const val PERMISSION_SUPER_ADMIN = "ROLE_SUPER_ADMIN"

        // Permission sets
        private val BASIC_PERMISSIONS = setOf(PERMISSION_ADMIN, PERMISSION_USER)
        private val THREE_PERMISSIONS = setOf(PERMISSION_ADMIN, PERMISSION_USER, PERMISSION_MODERATOR)
        private val ADMIN_PERMISSIONS = setOf(PERMISSION_ADMIN, PERMISSION_SUPER_ADMIN, PERMISSION_OWNER)
        private val ALL_PERMISSIONS = setOf(
            PERMISSION_ADMIN,
            PERMISSION_USER,
            PERMISSION_MODERATOR,
            PERMISSION_OWNER,
            PERMISSION_EDITOR,
        )

        // Property name constants
        private const val PROPERTY_MAX_RETRIES = "max-retries"
        private const val PROPERTY_MAX_REQUESTS = "max-requests"
        private const val PROPERTY_TIMEOUT_SECONDS = "timeout-seconds"
        private const val PROPERTY_TIMEOUT_MS = "timeout-ms"
        private const val PROPERTY_API_KEY = "api-key"
        private const val PROPERTY_API_ENDPOINT = "api-endpoint"
        private const val PROPERTY_CONFIG = "config"
        private const val PROPERTY_LOG_LEVEL = "log-level"
        private const val PROPERTY_EXTERNAL_CONFIG = "external-config"
        private const val PROPERTY_FEATURE_ENABLED = "feature-enabled"

        // Property value constants
        private const val MAX_RETRIES_VALUE = 3
        private const val MAX_RETRIES_DESCRIPTION = "Maximum retry attempts"
        private const val MAX_REQUESTS_VALUE = 1000
        private const val MAX_REQUESTS_DESCRIPTION = "Maximum requests per hour"
        private const val TIMEOUT_VALUE = 60
        private const val TIMEOUT_MS_VALUE = 30000L
        private const val API_KEY_VALUE = "secret"
        private const val API_ENDPOINT_VALUE = "https://api.example.com"
        private const val API_ENDPOINT_DESCRIPTION = "API endpoint URL"
        private const val CONFIG_VALUE = "immutable"
        private const val EXTERNAL_CONFIG_VALUE = "external"
        private const val FEATURE_ENABLED_VALUE = true

        // Log level constants
        private const val LOG_LEVEL_DEBUG = "DEBUG"
        private const val LOG_LEVEL_INFO = "INFO"
        private const val LOG_LEVEL_WARN = "WARN"
        private const val LOG_LEVEL_ERROR = "ERROR"
        private val LOG_LEVELS = setOf(LOG_LEVEL_DEBUG, LOG_LEVEL_INFO, LOG_LEVEL_WARN, LOG_LEVEL_ERROR)

        // Fixed values
        private val RETRIES_FIXED_VALUES = setOf(1, 3, 5)

        // Type test values
        private const val STRING_VALUE = "text"
        private const val INT_VALUE = 42
        private const val LONG_VALUE = 1000L
        private const val DOUBLE_VALUE = 3.14
        private const val BOOLEAN_VALUE = true
    }
}
