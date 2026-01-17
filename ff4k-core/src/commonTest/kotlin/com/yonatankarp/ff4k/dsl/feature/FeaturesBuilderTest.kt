package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for FeaturesBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
class FeaturesBuilderTest {

    @Test
    fun `builds empty list when no features added`() {
        // Given
        val builder = FeaturesBuilder()

        // When
        val result = builder.build()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `adds pre-built feature using feature method`() {
        // Given
        val builder = FeaturesBuilder()
        val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

        // When
        val result = builder.apply {
            feature(feature)
        }.build()

        // Then
        assertEquals(1, result.size)
        assertEquals(feature, result[0])
    }

    @Test
    fun `adds multiple pre-built features`() {
        // Given
        val builder = FeaturesBuilder()
        val feature1 = Feature(FEATURE_DARK_MODE, isEnabled = true)
        val feature2 = Feature(FEATURE_BETA, isEnabled = false)

        // When
        val result = builder.apply {
            feature(feature1)
            feature(feature2)
        }.build()

        // Then
        assertEquals(2, result.size)
        assertEquals(feature1, result[0])
        assertEquals(feature2, result[1])
    }

    @Test
    fun `adds collection of features using features method`() {
        // Given
        val builder = FeaturesBuilder()
        val featureList = listOf(
            Feature(FEATURE_DARK_MODE, isEnabled = true),
            Feature(FEATURE_BETA, isEnabled = false),
            Feature(FEATURE_PREMIUM, isEnabled = true),
        )

        // When
        val result = builder.apply {
            features(featureList)
        }.build()

        // Then
        assertEquals(3, result.size)
        assertEquals(featureList, result)
    }

    @Test
    fun `creates feature inline using DSL block`() {
        // Given
        val builder = FeaturesBuilder()

        // When
        val result = builder.apply {
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
                description = DESCRIPTION_DARK_MODE
            }
        }.build()

        // Then
        assertEquals(1, result.size)
        assertEquals(FEATURE_DARK_MODE, result[0].uid)
        assertTrue(result[0].isEnabled)
        assertEquals(DESCRIPTION_DARK_MODE, result[0].description)
    }

    @Test
    fun `creates multiple features inline using DSL blocks`() {
        // Given
        val builder = FeaturesBuilder()

        // When
        val result = builder.apply {
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
                group = GROUP_UI
            }
            feature(FEATURE_BETA) {
                isEnabled = false
                group = GROUP_EXPERIMENTAL
            }
        }.build()

        // Then
        assertEquals(2, result.size)
        assertEquals(FEATURE_DARK_MODE, result[0].uid)
        assertTrue(result[0].isEnabled)
        assertEquals(GROUP_UI, result[0].group)
        assertEquals(FEATURE_BETA, result[1].uid)
        assertFalse(result[1].isEnabled)
        assertEquals(GROUP_EXPERIMENTAL, result[1].group)
    }

    @Test
    fun `combines pre-built features and DSL-defined features`() {
        // Given
        val builder = FeaturesBuilder()
        val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)

        // When
        val result = builder.apply {
            feature(preBuiltFeature)
            feature(FEATURE_DARK_MODE) {
                isEnabled = true
            }
        }.build()

        // Then
        assertEquals(2, result.size)
        assertEquals(FEATURE_LEGACY, result[0].uid)
        assertEquals(FEATURE_DARK_MODE, result[1].uid)
    }

    @Test
    fun `combines collection and individual features`() {
        // Given
        val builder = FeaturesBuilder()
        val featureList = listOf(
            Feature(FEATURE_DARK_MODE, isEnabled = true),
            Feature(FEATURE_BETA, isEnabled = false),
        )

        // When
        val result = builder.apply {
            features(featureList)
            feature(FEATURE_PREMIUM) {
                isEnabled = true
            }
        }.build()

        // Then
        assertEquals(3, result.size)
        assertEquals(FEATURE_DARK_MODE, result[0].uid)
        assertEquals(FEATURE_BETA, result[1].uid)
        assertEquals(FEATURE_PREMIUM, result[2].uid)
    }

    @Test
    fun `preserves insertion order`() {
        // Given
        val builder = FeaturesBuilder()

        // When
        val result = builder.apply {
            feature(FEATURE_THIRD) { isEnabled = true }
            feature(FEATURE_FIRST) { isEnabled = true }
            feature(FEATURE_SECOND) { isEnabled = true }
        }.build()

        // Then
        assertEquals(FEATURE_THIRD, result[0].uid)
        assertEquals(FEATURE_FIRST, result[1].uid)
        assertEquals(FEATURE_SECOND, result[2].uid)
    }

    @Test
    fun `allows duplicate features`() {
        // Given
        val builder = FeaturesBuilder()
        val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

        // When
        val result = builder.apply {
            feature(feature)
            feature(feature)
        }.build()

        // Then
        assertEquals(2, result.size)
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

        private const val GROUP_UI = "ui"
        private const val GROUP_EXPERIMENTAL = "experimental"
    }
}
