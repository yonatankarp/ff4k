package com.yonatankarp.ff4k.dsl.feature

import com.yonatankarp.ff4k.core.Feature
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * Tests for top-level features() DSL function.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FeaturesDslTest :
    FunSpec({

        test("features creates empty list when no features defined") {
            // When
            val result = features { }

            // Then
            result.shouldBeEmpty()
        }

        test("features creates single feature using DSL block") {
            // When
            val result = features {
                feature(FEATURE_DARK_MODE) {
                    isEnabled = true
                }
            }

            // Then
            result shouldContainExactly listOf(
                Feature(FEATURE_DARK_MODE, isEnabled = true),
            )
        }

        test("features creates multiple features using DSL blocks") {
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
            result shouldContainExactly listOf(
                Feature(
                    uid = FEATURE_DARK_MODE,
                    isEnabled = true,
                    description = DESCRIPTION_DARK_MODE,
                ),
                Feature(
                    uid = FEATURE_BETA,
                    isEnabled = false,
                    group = GROUP_EXPERIMENTAL,
                ),
                Feature(
                    uid = FEATURE_PREMIUM,
                    isEnabled = true,
                    permissions = setOf(PERMISSION_ADMIN),
                ),
            )
        }

        test("features accepts pre-built features") {
            // Given
            val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)

            // When
            val result = features {
                feature(preBuiltFeature)
            }

            // Then
            result.shouldHaveSize(1)
            result[0] shouldBe preBuiltFeature
        }

        test("features accepts collection of features") {
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
            result.shouldHaveSize(2)
            result shouldBe featureList
        }

        test("features combines all addition methods") {
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
            result.shouldHaveSize(3)
            result[0].uid shouldBe FEATURE_LEGACY
            result[1].uid shouldBe FEATURE_PREMIUM
            result[2].uid shouldBe FEATURE_DARK_MODE
        }

        test("features preserves insertion order") {
            // When
            val result = features {
                feature(FEATURE_THIRD) { isEnabled = true }
                feature(FEATURE_FIRST) { isEnabled = true }
                feature(FEATURE_SECOND) { isEnabled = true }
            }

            // Then
            result shouldContainExactly listOf(
                Feature(FEATURE_THIRD, isEnabled = true),
                Feature(FEATURE_FIRST, isEnabled = true),
                Feature(FEATURE_SECOND, isEnabled = true),
            )
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

        private const val GROUP_EXPERIMENTAL = "experimental"

        private const val PERMISSION_ADMIN = "ROLE_ADMIN"
    }
}
