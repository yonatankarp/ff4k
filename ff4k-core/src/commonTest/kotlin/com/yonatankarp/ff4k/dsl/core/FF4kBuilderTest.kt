package com.yonatankarp.ff4k.dsl.core

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.store.InMemoryPropertyStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for FF4kBuilder and ff4k() DSL entry point.
 *
 * @author Yonatan Karp-Rudin
 */
class FF4kBuilderTest {

    @Test
    fun `ff4k creates empty instance when no configuration provided`() = runTest {
        // When
        val ff4k = ff4k { }

        // Then
        assertTrue(ff4k.features().isEmpty())
        assertTrue(ff4k.properties().isEmpty())
    }

    @Test
    fun `ff4k registers pre-built feature using feature method`() = runTest {
        // Given
        val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

        // When
        val ff4k = ff4k {
            feature(feature)
        }

        // Then
        assertTrue(ff4k.hasFeature(FEATURE_DARK_MODE))
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
    }

    @Test
    fun `ff4k registers pre-built property using property method`() = runTest {
        // Given
        val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

        // When
        val ff4k = ff4k {
            property(property)
        }

        // Then
        assertTrue(ff4k.hasProperty(PROPERTY_API_URL))
        assertEquals(VALUE_API_URL, ff4k.property<String>(PROPERTY_API_URL)?.value)
    }

    @Test
    fun `ff4k registers multiple features using features block`() = runTest {
        // When
        val ff4k = ff4k {
            features {
                feature(FEATURE_DARK_MODE) {
                    isEnabled = true
                    description = DESCRIPTION_DARK_MODE
                }
                feature(FEATURE_BETA) {
                    isEnabled = false
                    group = GROUP_EXPERIMENTAL
                }
            }
        }

        // Then
        assertEquals(2, ff4k.features().size)
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
        assertFalse(ff4k.check(FEATURE_BETA))
        assertEquals(DESCRIPTION_DARK_MODE, ff4k.feature(FEATURE_DARK_MODE).description)
        assertEquals(GROUP_EXPERIMENTAL, ff4k.feature(FEATURE_BETA).group)
    }

    @Test
    fun `ff4k registers multiple properties using properties block`() = runTest {
        // When
        val ff4k = ff4k {
            properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                    description = DESCRIPTION_MAX_RETRIES
                }
                property(PROPERTY_TIMEOUT_MS) {
                    value = VALUE_TIMEOUT_MS
                }
            }
        }

        // Then
        assertEquals(2, ff4k.properties().size)
        assertEquals(VALUE_MAX_RETRIES, ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.value)
        assertEquals(VALUE_TIMEOUT_MS, ff4k.property<Long>(PROPERTY_TIMEOUT_MS)?.value)
        assertEquals(DESCRIPTION_MAX_RETRIES, ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.description)
    }

    @Test
    fun `ff4k combines pre-built and DSL-defined features`() = runTest {
        // Given
        val preBuiltFeature = Feature(FEATURE_PREMIUM, isEnabled = true)

        // When
        val ff4k = ff4k {
            feature(preBuiltFeature)
            features {
                feature(FEATURE_DARK_MODE) {
                    isEnabled = true
                }
            }
        }

        // Then
        assertEquals(2, ff4k.features().size)
        assertTrue(ff4k.hasFeature(FEATURE_PREMIUM))
        assertTrue(ff4k.hasFeature(FEATURE_DARK_MODE))
    }

    @Test
    fun `ff4k combines pre-built and DSL-defined properties`() = runTest {
        // Given
        val preBuiltProperty = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

        // When
        val ff4k = ff4k {
            property(preBuiltProperty)
            properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
            }
        }

        // Then
        assertEquals(2, ff4k.properties().size)
        assertTrue(ff4k.hasProperty(PROPERTY_API_URL))
        assertTrue(ff4k.hasProperty(PROPERTY_MAX_RETRIES))
    }

    @Test
    fun `ff4k accepts custom feature store`() = runTest {
        // Given
        val customStore = InMemoryFeatureStore()

        // When
        val ff4k = ff4k(featureStore = customStore) {
            features {
                feature(FEATURE_DARK_MODE) {
                    isEnabled = true
                }
            }
        }

        // Then
        assertTrue(ff4k.hasFeature(FEATURE_DARK_MODE))
        assertTrue(FEATURE_DARK_MODE in customStore)
    }

    @Test
    fun `ff4k accepts custom property store`() = runTest {
        // Given
        val customStore = InMemoryPropertyStore()

        // When
        val ff4k = ff4k(propertyStore = customStore) {
            properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                }
            }
        }

        // Then
        assertTrue(ff4k.hasProperty(PROPERTY_MAX_RETRIES))
        assertTrue(PROPERTY_MAX_RETRIES in customStore)
    }

    @Test
    fun `ff4k respects autoCreate parameter`() = runTest {
        // When
        val ff4k = ff4k(autoCreate = true) { }

        // Then
        assertFalse(ff4k.check(FEATURE_NON_EXISTENT))
        assertTrue(ff4k.hasFeature(FEATURE_NON_EXISTENT))
    }

    @Test
    fun `ff4k creates complete configuration with all options`() = runTest {
        // Given
        val strategy = TestStrategy()
        val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)
        val preBuiltProperty = PropertyInt(PROPERTY_PORT, VALUE_PORT)

        // When
        val ff4k = ff4k {
            feature(preBuiltFeature)
            property(preBuiltProperty)

            features {
                feature(FEATURE_DARK_MODE) {
                    isEnabled = true
                    description = DESCRIPTION_DARK_MODE
                    group = GROUP_UI
                    flippingStrategy = strategy
                    permissions(PERMISSION_ADMIN, PERMISSION_USER)
                    property(PROPERTY_THEME) {
                        value = VALUE_THEME
                    }
                }
                feature(FEATURE_BETA) {
                    isEnabled = false
                    group = GROUP_EXPERIMENTAL
                }
            }

            properties {
                property(PROPERTY_MAX_RETRIES) {
                    value = VALUE_MAX_RETRIES
                    description = DESCRIPTION_MAX_RETRIES
                    readOnly = true
                }
                property(PROPERTY_API_URL) {
                    value = VALUE_API_URL
                }
            }
        }

        // Then
        assertEquals(3, ff4k.features().size)
        assertTrue(ff4k.hasFeature(FEATURE_LEGACY))
        assertTrue(ff4k.hasFeature(FEATURE_DARK_MODE))
        assertTrue(ff4k.hasFeature(FEATURE_BETA))

        val darkModeFeature = ff4k.feature(FEATURE_DARK_MODE)
        assertEquals(DESCRIPTION_DARK_MODE, darkModeFeature.description)
        assertEquals(GROUP_UI, darkModeFeature.group)
        assertEquals(strategy, darkModeFeature.flippingStrategy)
        assertEquals(setOf(PERMISSION_ADMIN, PERMISSION_USER), darkModeFeature.permissions)
        assertNotNull(darkModeFeature.customProperties[PROPERTY_THEME])

        assertEquals(3, ff4k.properties().size)
        assertTrue(ff4k.hasProperty(PROPERTY_PORT))
        assertTrue(ff4k.hasProperty(PROPERTY_MAX_RETRIES))
        assertTrue(ff4k.hasProperty(PROPERTY_API_URL))

        val maxRetriesProperty = ff4k.property<Int>(PROPERTY_MAX_RETRIES)
        assertNotNull(maxRetriesProperty)
        assertEquals(DESCRIPTION_MAX_RETRIES, maxRetriesProperty.description)
        assertTrue(maxRetriesProperty.readOnly)
    }

    @Test
    fun `ff4k features block can add pre-built features`() = runTest {
        // Given
        val feature1 = Feature(FEATURE_DARK_MODE, isEnabled = true)
        val feature2 = Feature(FEATURE_BETA, isEnabled = false)

        // When
        val ff4k = ff4k {
            features {
                feature(feature1)
                feature(feature2)
            }
        }

        // Then
        assertEquals(2, ff4k.features().size)
        assertTrue(ff4k.check(FEATURE_DARK_MODE))
        assertFalse(ff4k.check(FEATURE_BETA))
    }

    @Test
    fun `ff4k properties block can add pre-built properties`() = runTest {
        // Given
        val property1 = PropertyString(PROPERTY_API_URL, VALUE_API_URL)
        val property2 = PropertyInt(PROPERTY_MAX_RETRIES, VALUE_MAX_RETRIES)

        // When
        val ff4k = ff4k {
            properties {
                property(property1)
                property(property2)
            }
        }

        // Then
        assertEquals(2, ff4k.properties().size)
        assertEquals(VALUE_API_URL, ff4k.property<String>(PROPERTY_API_URL)?.value)
        assertEquals(VALUE_MAX_RETRIES, ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.value)
    }

    private class TestStrategy : FlippingStrategy {
        override val initParams = emptyMap<String, String>()
    }

    private companion object {
        private const val FEATURE_DARK_MODE = "dark-mode"
        private const val FEATURE_BETA = "beta-program"
        private const val FEATURE_PREMIUM = "premium-tier"
        private const val FEATURE_LEGACY = "legacy-feature"
        private const val FEATURE_NON_EXISTENT = "non-existent"

        private const val DESCRIPTION_DARK_MODE = "Enable dark mode theme"
        private const val DESCRIPTION_MAX_RETRIES = "Maximum retry attempts"

        private const val GROUP_UI = "ui"
        private const val GROUP_EXPERIMENTAL = "experimental"

        private const val PERMISSION_ADMIN = "ROLE_ADMIN"
        private const val PERMISSION_USER = "ROLE_USER"

        private const val PROPERTY_API_URL = "api.base.url"
        private const val PROPERTY_MAX_RETRIES = "max-retries"
        private const val PROPERTY_TIMEOUT_MS = "timeout-ms"
        private const val PROPERTY_PORT = "server.port"
        private const val PROPERTY_THEME = "theme"

        private const val VALUE_API_URL = "https://api.example.com"
        private const val VALUE_MAX_RETRIES = 3
        private const val VALUE_TIMEOUT_MS = 5000L
        private const val VALUE_PORT = 8080
        private const val VALUE_THEME = "dark"
    }
}
