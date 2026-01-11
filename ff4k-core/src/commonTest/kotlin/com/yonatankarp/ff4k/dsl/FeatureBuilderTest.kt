package com.yonatankarp.ff4k.dsl
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FeatureBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
class FeatureBuilderTest {

    @Test
    fun `feature creates feature with minimal configuration`() {
        // When
        val feature = feature(FEATURE_UID) { }

        // Then
        assertEquals(FEATURE_UID, feature.uid)
        assertFalse(feature.isEnabled)
        assertNull(feature.description)
        assertNull(feature.group)
        assertTrue(feature.permissions.isEmpty())
        assertNull(feature.flippingStrategy)
        assertTrue(feature.customProperties.isEmpty())
    }

    @Test
    fun `feature creates feature with all fields set`() {
        // Given
        val strategy = TestStrategy()

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
        assertEquals(FEATURE_UID, feature.uid)
        assertTrue(feature.isEnabled)
        assertEquals(FEATURE_DESCRIPTION, feature.description)
        assertEquals(FEATURE_GROUP, feature.group)
        assertEquals(BASIC_PERMISSIONS, feature.permissions)
        assertEquals(strategy, feature.flippingStrategy)
    }

    @Test
    fun `enable sets isEnabled to true`() {
        // When
        val feature = feature(FEATURE_UID) {
            enable()
        }

        // Then
        assertTrue(feature.isEnabled)
    }

    @Test
    fun `disable sets isEnabled to false`() {
        // When
        val feature = feature(FEATURE_UID) {
            isEnabled = true
            disable()
        }

        // Then
        assertFalse(feature.isEnabled)
    }

    @Test
    fun `inGroup sets group name`() {
        // When
        val feature = feature(FEATURE_UID) {
            inGroup(FEATURE_GROUP)
        }

        // Then
        assertEquals(FEATURE_GROUP, feature.group)
    }

    @Test
    fun `strategy sets flipping strategy`() {
        // Given
        val strategy = TestStrategy()

        // When
        val feature = feature(FEATURE_UID) {
            strategy(strategy)
        }

        // Then
        assertEquals(strategy, feature.flippingStrategy)
    }

    @Test
    fun `permission adds single permission`() {
        // When
        val feature = feature(FEATURE_UID) {
            permission(PERMISSION_ADMIN)
        }

        // Then
        assertEquals(setOf(PERMISSION_ADMIN), feature.permissions)
    }

    @Test
    fun `permission adds multiple permissions`() {
        // When
        val feature = feature(FEATURE_UID) {
            permission(PERMISSION_ADMIN)
            permission(PERMISSION_USER)
            permission(PERMISSION_MODERATOR)
        }

        // Then
        assertEquals(THREE_PERMISSIONS, feature.permissions)
    }

    @Test
    fun `permissions vararg adds multiple permissions`() {
        // When
        val feature = feature(FEATURE_UID) {
            permissions(PERMISSION_ADMIN, PERMISSION_USER, PERMISSION_MODERATOR)
        }

        // Then
        assertEquals(THREE_PERMISSIONS, feature.permissions)
    }

    @Test
    fun `permissions DSL block adds multiple permissions`() {
        // When
        val feature = feature(FEATURE_UID) {
            permissions {
                +PERMISSION_ADMIN
                +PERMISSION_USER
                +PERMISSION_MODERATOR
            }
        }

        // Then
        assertEquals(THREE_PERMISSIONS, feature.permissions)
    }

    @Test
    fun `permissions can be added using multiple methods`() {
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
        assertEquals(ALL_PERMISSIONS, feature.permissions)
    }

    @Test
    fun `property adds existing property`() {
        // Given
        val prop = intProperty(PROPERTY_MAX_RETRIES) {
            value = MAX_RETRIES_VALUE
        }

        // When
        val feature = feature(FEATURE_UID) {
            property(prop)
        }

        // Then
        assertEquals(1, feature.customProperties.size)
        assertEquals(prop, feature.customProperties[PROPERTY_MAX_RETRIES])
    }

    @Test
    fun `property creates inline property with type inference`() {
        // When
        val feature = feature(FEATURE_UID) {
            property(PROPERTY_MAX_REQUESTS) {
                value = MAX_REQUESTS_VALUE
                description = MAX_REQUESTS_DESCRIPTION
            }
        }

        // Then
        assertEquals(1, feature.customProperties.size)
        val prop = feature.customProperties[PROPERTY_MAX_REQUESTS]
        assertNotNull(prop)
        assertEquals(PROPERTY_MAX_REQUESTS, prop.name)
        assertEquals(MAX_REQUESTS_VALUE, prop.value)
        assertEquals(MAX_REQUESTS_DESCRIPTION, prop.description)
    }

    @Test
    fun `property creates multiple properties`() {
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
        assertEquals(3, feature.customProperties.size)
        assertEquals(MAX_REQUESTS_VALUE, feature.customProperties[PROPERTY_MAX_REQUESTS]?.value)
        assertEquals(TIMEOUT_VALUE, feature.customProperties[PROPERTY_TIMEOUT_SECONDS]?.value)
        assertEquals(API_KEY_VALUE, feature.customProperties[PROPERTY_API_KEY]?.value)
    }

    @Test
    fun `property replaces existing property with same name`() {
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
        assertEquals(1, feature.customProperties.size)
        assertEquals(newValue, feature.customProperties[PROPERTY_CONFIG]?.value)
    }

    @Test
    fun `property supports different types with type inference`() {
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
        assertEquals(5, feature.customProperties.size)
        assertEquals(STRING_VALUE, feature.customProperties["string-prop"]?.value)
        assertEquals(INT_VALUE, feature.customProperties["int-prop"]?.value)
        assertEquals(LONG_VALUE, feature.customProperties["long-prop"]?.value)
        assertEquals(DOUBLE_VALUE, feature.customProperties["double-prop"]?.value)
        assertEquals(BOOLEAN_VALUE, feature.customProperties["boolean-prop"]?.value)
    }

    @Test
    fun `property with fixedValues works correctly`() {
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
        val prop = feature.customProperties[PROPERTY_LOG_LEVEL]
        assertNotNull(prop)
        assertEquals(LOG_LEVELS, prop.fixedValues)
    }

    @Test
    fun `property with readOnly works correctly`() {
        // When
        val feature = feature(FEATURE_UID) {
            property(PROPERTY_CONFIG) {
                value = CONFIG_VALUE
                readOnly = true
            }
        }

        // Then
        val prop = feature.customProperties[PROPERTY_CONFIG]
        assertNotNull(prop)
        assertTrue(prop.readOnly)
    }

    @Test
    fun `validation fails when property value not set`() {
        // When / Then
        assertFailsWith<IllegalStateException> {
            feature(FEATURE_UID) {
                property(PROPERTY_CONFIG) {
                    description = "Configuration"
                }
            }
        }
    }

    @Test
    fun `validation fails when property value not in fixedValues`() {
        // Given
        val invalidValue = "INVALID"

        // When / Then
        assertFailsWith<IllegalArgumentException> {
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

    @Test
    fun `complex nested scenario with all features`() {
        // Given
        val strategy = TestStrategy()
        val existingProp = stringProperty(PROPERTY_EXTERNAL_CONFIG) {
            value = EXTERNAL_CONFIG_VALUE
        }

        // When
        val feature = feature(FEATURE_UID) {
            enable()
            description = FEATURE_DESCRIPTION
            inGroup(FEATURE_GROUP)
            strategy(strategy)

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
        assertEquals(FEATURE_UID, feature.uid)
        assertTrue(feature.isEnabled)
        assertEquals(FEATURE_DESCRIPTION, feature.description)
        assertEquals(FEATURE_GROUP, feature.group)
        assertEquals(ADMIN_PERMISSIONS, feature.permissions)
        assertEquals(strategy, feature.flippingStrategy)
        assertEquals(5, feature.customProperties.size)

        // Verify properties
        assertEquals(existingProp, feature.customProperties[PROPERTY_EXTERNAL_CONFIG])
        assertEquals(MAX_RETRIES_VALUE, feature.customProperties[PROPERTY_MAX_RETRIES]?.value)
        assertEquals(API_ENDPOINT_VALUE, feature.customProperties[PROPERTY_API_ENDPOINT]?.value)
        assertEquals(TIMEOUT_MS_VALUE, feature.customProperties[PROPERTY_TIMEOUT_MS]?.value)
        assertEquals(FEATURE_ENABLED_VALUE, feature.customProperties[PROPERTY_FEATURE_ENABLED]?.value)

        // Verify property details
        val retriesProp = feature.customProperties[PROPERTY_MAX_RETRIES]
        assertNotNull(retriesProp)
        assertEquals(MAX_RETRIES_DESCRIPTION, retriesProp.description)
        assertEquals(RETRIES_FIXED_VALUES, retriesProp.fixedValues)

        val apiProp = feature.customProperties[PROPERTY_API_ENDPOINT]
        assertNotNull(apiProp)
        assertTrue(apiProp.readOnly)
    }

    private class TestStrategy : FlippingStrategy {
        override val initParams = emptyMap<String, String>()
    }

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
