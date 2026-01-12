package com.yonatankarp.ff4k.dsl

import com.yonatankarp.ff4k.core.Feature
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InMemoryFeatureStoreBuilderTest {

    @Test
    fun `should create empty store with no configuration`() = runTest {
        // When
        val store = inMemoryFeatureStore { }

        // Then
        assertTrue(store.isEmpty())
        assertEquals(0, store.count())
    }

    @Test
    fun `should create store with inline feature builder`() = runTest {
        // When
        val store = inMemoryFeatureStore {
            feature("dark-mode") {
                enable()
                description = "Enable dark mode UI"
            }
        }

        // Then
        assertEquals(1, store.count())
        assertTrue("dark-mode" in store)
        val feature = store["dark-mode"]
        assertNotNull(feature)
        assertEquals(true, feature.isEnabled)
        assertEquals("Enable dark mode UI", feature.description)
    }

    @Test
    fun `should create store with multiple inline features`() = runTest {
        // When
        val store = inMemoryFeatureStore {
            feature("feature-1") {
                enable()
            }

            feature("feature-2") {
                disable()
                description = "Second feature"
            }

            feature("feature-3") {
                enable()
                inGroup("test-group")
            }
        }

        // Then
        assertEquals(3, store.count())
        assertTrue("feature-1" in store)
        assertTrue("feature-2" in store)
        assertTrue("feature-3" in store)
    }

    @Test
    fun `should add features using unary plus operator`() = runTest {
        // Given
        val feature1 = Feature("feature-1", isEnabled = true)
        val feature2 = Feature("feature-2", isEnabled = false)

        // When
        val store = inMemoryFeatureStore {
            +feature1
            +feature2
        }

        // Then
        assertEquals(2, store.count())
        assertTrue("feature-1" in store)
        assertTrue("feature-2" in store)
    }

    @Test
    fun `should add feature using feature function`() = runTest {
        // Given
        val existingFeature = Feature("existing", isEnabled = true)

        // When
        val store = inMemoryFeatureStore {
            feature(existingFeature)
        }

        // Then
        assertEquals(1, store.count())
        assertTrue("existing" in store)
    }

    @Test
    fun `should add features from collection`() = runTest {
        // Given
        val featureList = listOf(
            Feature("feature-1", isEnabled = true),
            Feature("feature-2", isEnabled = false),
            Feature("feature-3", isEnabled = true),
        )

        // When
        val store = inMemoryFeatureStore {
            features(featureList)
        }

        // Then
        assertEquals(3, store.count())
        assertTrue("feature-1" in store)
        assertTrue("feature-2" in store)
        assertTrue("feature-3" in store)
    }

    @Test
    fun `should mix different approaches for adding features`() = runTest {
        // Given
        val existingFeature = Feature("existing", isEnabled = true)
        val featureList = listOf(
            Feature("from-list-1", isEnabled = true),
            Feature("from-list-2", isEnabled = false),
        )

        // When
        val store = inMemoryFeatureStore {
            +existingFeature
            features(featureList)

            feature("inline-feature") {
                enable()
                description = "Inline created"
            }

            +Feature("operator-feature", isEnabled = false)
        }

        // Then
        assertEquals(5, store.count())
        assertTrue("existing" in store)
        assertTrue("from-list-1" in store)
        assertTrue("from-list-2" in store)
        assertTrue("inline-feature" in store)
        assertTrue("operator-feature" in store)
    }

    @Test
    fun `should handle duplicate feature UIDs by keeping last one`() = runTest {
        // When
        val store = inMemoryFeatureStore {
            feature("duplicate") {
                enable()
                description = "First"
            }

            feature("duplicate") {
                disable()
                description = "Second"
            }
        }

        // Then
        assertEquals(1, store.count())
        val feature = store["duplicate"]
        assertNotNull(feature)
        assertEquals(false, feature.isEnabled)
        assertEquals("Second", feature.description)
    }

    @Test
    fun `should create store with features having properties`() = runTest {
        // When
        val store = inMemoryFeatureStore {
            feature("api-config") {
                enable()
                description = "API Configuration"

                property("max-requests") {
                    value = 1000
                    description = "Maximum requests per hour"
                }

                property("timeout") {
                    value = 30
                }
            }
        }

        // Then
        assertEquals(1, store.count())
        val feature = store["api-config"]
        assertNotNull(feature)
        assertEquals(2, feature.customProperties.size)
        assertTrue(feature.customProperties.containsKey("max-requests"))
        assertTrue(feature.customProperties.containsKey("timeout"))
    }

    @Test
    fun `should create store with features having permissions`() = runTest {
        // When
        val store = inMemoryFeatureStore {
            feature("admin-panel") {
                enable()

                permissions {
                    +"ROLE_ADMIN"
                    +"ROLE_SUPER_ADMIN"
                }
            }
        }

        // Then
        assertEquals(1, store.count())
        val feature = store["admin-panel"]
        assertNotNull(feature)
        assertEquals(2, feature.permissions.size)
        assertTrue(feature.permissions.contains("ROLE_ADMIN"))
        assertTrue(feature.permissions.contains("ROLE_SUPER_ADMIN"))
    }
}
