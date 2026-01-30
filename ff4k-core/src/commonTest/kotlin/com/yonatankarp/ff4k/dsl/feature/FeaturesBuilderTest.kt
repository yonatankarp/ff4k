package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Tests for FeaturesBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FeaturesBuilderTest :
    FunSpec({

        test("builds empty list when no features added") {
            // Given
            val builder = FeaturesBuilder()

            // When
            val result = builder.build()

            // Then
            result.shouldBeEmpty()
        }

        test("adds pre-built feature using feature method") {
            // Given
            val builder = FeaturesBuilder()
            val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

            // When
            val result = builder.apply {
                feature(feature)
            }.build()

            // Then
            result.size shouldBe 1
            result[0] shouldBe feature
        }

        test("adds multiple pre-built features") {
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
            result.size shouldBe 2
            result[0] shouldBe feature1
            result[1] shouldBe feature2
        }

        test("adds collection of features using features method") {
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
            result.size shouldBe 3
            result shouldBe featureList
        }

        test("creates feature inline using DSL block") {
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
            result.size shouldBe 1
            result[0].uid shouldBe FEATURE_DARK_MODE
            result[0].isEnabled.shouldBeTrue()
            result[0].description shouldBe DESCRIPTION_DARK_MODE
        }

        test("creates multiple features inline using DSL blocks") {
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
            result.size shouldBe 2
            result[0].uid shouldBe FEATURE_DARK_MODE
            result[0].isEnabled.shouldBeTrue()
            result[0].group shouldBe GROUP_UI
            result[1].uid shouldBe FEATURE_BETA
            result[1].isEnabled.shouldBeFalse()
            result[1].group shouldBe GROUP_EXPERIMENTAL
        }

        test("combines pre-built features and DSL-defined features") {
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
            result.size shouldBe 2
            result[0].uid shouldBe FEATURE_LEGACY
            result[1].uid shouldBe FEATURE_DARK_MODE
        }

        test("combines collection and individual features") {
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
            result.size shouldBe 3
            result[0].uid shouldBe FEATURE_DARK_MODE
            result[1].uid shouldBe FEATURE_BETA
            result[2].uid shouldBe FEATURE_PREMIUM
        }

        test("preserves insertion order") {
            // Given
            val builder = FeaturesBuilder()

            // When
            val result = builder.apply {
                feature(FEATURE_THIRD) { isEnabled = true }
                feature(FEATURE_FIRST) { isEnabled = true }
                feature(FEATURE_SECOND) { isEnabled = true }
            }.build()

            // Then
            result[0].uid shouldBe FEATURE_THIRD
            result[1].uid shouldBe FEATURE_FIRST
            result[2].uid shouldBe FEATURE_SECOND
        }

        test("allows duplicate features") {
            // Given
            val builder = FeaturesBuilder()
            val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

            // When
            val result = builder.apply {
                feature(feature)
                feature(feature)
            }.build()

            // Then
            result.size shouldBe 2
        }
    }) {
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
