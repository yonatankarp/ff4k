package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for top-level features() DSL function.
 *
 * @author Yonatan Karp-Rudin
 */
class FeaturesDslTest {

    @Test
    fun `features creates empty list when no features defined`() {
        // When
        val result = features { }

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `features creates single feature using DSL block`() {
        // When
        val result = features {
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
            }
        }

        // Then
        assertEquals(1, result.size)
        assertEquals(FEATURE_DARK_MODE, result[0].uid)
        assertTrue(result[0].isEnabled)
    }

    @Test
    fun `features creates multiple features using DSL blocks`() {
        // When
        val result = features {
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
                description = DESCRIPTION_DARK_MODE
            }
            feature(FEATURE_BETA) {
                isEnabled = false
                group = GROUP_EXPERIMENTAL
            }
            feature(FEATURE_PREMIUM) {
                isEnabled = true
                permissions(PERMISSION_ADMIN)
            }
        }

        // Then
        assertEquals(3, result.size)
        assertEquals(FEATURE_DARK_MODE, result[0].uid)
        assertTrue(result[0].isEnabled)
        assertEquals(DESCRIPTION_DARK_MODE, result[0].description)

        assertEquals(FEATURE_BETA, result[1].uid)
        assertFalse(result[1].isEnabled)
        assertEquals(GROUP_EXPERIMENTAL, result[1].group)

        assertEquals(FEATURE_PREMIUM, result[2].uid)
        assertTrue(result[2].isEnabled)
        assertEquals(setOf(PERMISSION_ADMIN), result[2].permissions)
    }

    @Test
    fun `features accepts pre-built features`() {
        // Given
        val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)

        // When
        val result = features {
            feature(preBuiltFeature)
        }

        // Then
        assertEquals(1, result.size)
        assertEquals(preBuiltFeature, result[0])
    }

    @Test
    fun `features accepts collection of features`() {
        // Given
        val featureList = listOf(
            Feature(FEATURE_DARK_MODE, isEnabled = true),
            Feature(FEATURE_BETA, isEnabled = false),
        )

        // When
        val result = features {
            features(featureList)
        }

        // Then
        assertEquals(2, result.size)
        assertEquals(featureList, result)
    }

    @Test
    fun `features combines all addition methods`() {
        // Given
        val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)
        val featureCollection = listOf(Feature(FEATURE_PREMIUM, isEnabled = true))

        // When
        val result = features {
            feature(preBuiltFeature)
            features(featureCollection)
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
            }
        }

        // Then
        assertEquals(3, result.size)
        assertEquals(FEATURE_LEGACY, result[0].uid)
        assertEquals(FEATURE_PREMIUM, result[1].uid)
        assertEquals(FEATURE_DARK_MODE, result[2].uid)
    }

    @Test
    fun `features preserves insertion order`() {
        // When
        val result = features {
            feature(FEATURE_THIRD) { isEnabled = true }
            feature(FEATURE_FIRST) { isEnabled = true }
            feature(FEATURE_SECOND) { isEnabled = true }
        }

        // Then
        assertEquals(FEATURE_THIRD, result[0].uid)
        assertEquals(FEATURE_FIRST, result[1].uid)
        assertEquals(FEATURE_SECOND, result[2].uid)
    }

    private companion object {
        private const val FEATURE_DARK_MODE = "dark-mode"
        private const val FEATURE_BETA = "beta-program"
        private const val FEATURE_PREMIUM = "premium-tier"
        private const val FEATURE_LEGACY = "legacy-feature"
        private const val FEATURE_FIRST = "first"
        private const val FEATURE_SECOND = "second"
        private const val FEATURE_THIRD = "third"

        private const val DESCRIPTION_DARK_MODE = "Enable dark mode theme"

        private const val GROUP_EXPERIMENTAL = "experimental"

        private const val PERMISSION_ADMIN = "ROLE_ADMIN"
    }
}
