package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.dsl.core.ff4k
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * Tests for FF4k filtering, statistics, and reporting extension functions.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FF4kStatsTest :
    FunSpec({

        context("enabledFeatures") {
            withData(
                nameFn = { it.description },
                enabledFeaturesData,
            ) { (_, featuresBlock, expectedUids) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val enabled = ff4k.enabledFeatures()

                // Then
                enabled.size shouldBe expectedUids.size
                expectedUids.forEach { uid ->
                    enabled.any { it.uid == uid } shouldBe true
                }
            }
        }

        context("disabledFeatures") {
            withData(
                nameFn = { it.description },
                disabledFeaturesData,
            ) { (_, featuresBlock, expectedUids) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val disabled = ff4k.disabledFeatures()

                // Then
                disabled.size shouldBe expectedUids.size
                expectedUids.forEach { uid ->
                    disabled.any { it.uid == uid } shouldBe true
                }
            }
        }

        context("featuresWithPermission") {
            withData(
                nameFn = { it.description },
                featuresWithPermissionData,
            ) { (_, featuresBlock, permission, expectedUids) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val adminFeatures = ff4k.featuresWithPermission(permission)

                // Then
                adminFeatures.size shouldBe expectedUids.size
                expectedUids.forEach { uid ->
                    adminFeatures.any { it.uid == uid } shouldBe true
                }
            }
        }

        test("featuresWithStrategy returns empty list when no features have strategy") {
            // Given
            val ff4k = ff4k {
                features {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = false }
                }
            }

            // When
            val strategyFeatures = ff4k.featuresWithStrategy()

            // Then
            strategyFeatures.shouldBeEmpty()
        }

        test("featuresWithStrategy returns empty list when no features exist") {
            // Given
            val ff4k = ff4k { }

            // When
            val strategyFeatures = ff4k.featuresWithStrategy()

            // Then
            strategyFeatures.shouldBeEmpty()
        }

        test("stats returns correct counts") {
            // Given
            val ff4k = ff4k {
                features {
                    feature(FEATURE_ONE) {
                        isEnabled = true
                        group = GROUP_UI
                    }
                    feature(FEATURE_TWO) {
                        isEnabled = false
                        group = GROUP_UI
                    }
                    feature(FEATURE_THREE) {
                        isEnabled = true
                        group = GROUP_BILLING
                        permissions(PERMISSION_ADMIN)
                    }
                    feature(FEATURE_FOUR) {
                        isEnabled = true
                    }
                    feature(FEATURE_FIVE) {
                        isEnabled = false
                        permissions(PERMISSION_BETA)
                    }
                }
            }

            // When
            val stats = ff4k.stats()

            // Then
            stats.total shouldBe 5
            stats.enabled shouldBe 3
            stats.disabled shouldBe 2
            stats.withPermissions shouldBe 2
            stats.withStrategy shouldBe 0
            stats.groups shouldBe 2
        }

        test("stats returns zeros when no features exist") {
            // Given
            val ff4k = ff4k { }

            // When
            val stats = ff4k.stats()

            // Then
            stats.total shouldBe 0
            stats.enabled shouldBe 0
            stats.disabled shouldBe 0
            stats.withPermissions shouldBe 0
            stats.withStrategy shouldBe 0
            stats.groups shouldBe 0
        }

        test("stats counts zero groups when features have no groups") {
            // Given
            val ff4k = ff4k {
                features {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = true }
                }
            }

            // When
            val stats = ff4k.stats()

            // Then
            stats.total shouldBe 2
            stats.groups shouldBe 0
        }

        context("report generation") {
            withData(
                nameFn = { it.description },
                reportGenerationData,
            ) { (description, featuresBlock, expectedContent) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val report = ff4k.report()

                // Then
                expectedContent.forEach { content ->
                    report shouldContain content
                }
            }
        }

        test("report features are sorted by uid") {
            // Given
            val ff4k = ff4k {
                features {
                    feature("z-feature") { isEnabled = true }
                    feature("a-feature") { isEnabled = true }
                    feature("m-feature") { isEnabled = true }
                }
            }

            // When
            val report = ff4k.report()

            // Then
            report shouldContain "a-feature"
            report shouldContain "m-feature"
            report shouldContain "z-feature"

            val aIndex = report.indexOf("a-feature")
            val mIndex = report.indexOf("m-feature")
            val zIndex = report.indexOf("z-feature")

            aIndex shouldNotBe -1
            mIndex shouldNotBe -1
            zIndex shouldNotBe -1

            (aIndex < mIndex) shouldBe true
            (mIndex < zIndex) shouldBe true
        }
    }) {
    private companion object {
        // Feature IDs
        private const val FEATURE_ONE = "feature-1"
        private const val FEATURE_TWO = "feature-2"
        private const val FEATURE_THREE = "feature-3"
        private const val FEATURE_FOUR = "feature-4"
        private const val FEATURE_FIVE = "feature-5"

        // Groups
        private const val GROUP_UI = "ui"
        private const val GROUP_BILLING = "billing"

        // Permissions
        private const val PERMISSION_ADMIN = "ADMIN"
        private const val PERMISSION_BETA = "BETA"

        private val enabledFeaturesData = listOf(
            FeatureFilterData(
                description = "returns only enabled features",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = false }
                    feature(FEATURE_THREE) { isEnabled = true }
                },
                expectedUids = listOf(FEATURE_ONE, FEATURE_THREE),
            ),
            FeatureFilterData(
                description = "returns empty list when no features are enabled",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = false }
                    feature(FEATURE_TWO) { isEnabled = false }
                },
                expectedUids = emptyList(),
            ),
            FeatureFilterData(
                description = "returns empty list when no features exist",
                featuresBlock = { },
                expectedUids = emptyList(),
            ),
        )

        private val disabledFeaturesData = listOf(
            FeatureFilterData(
                description = "returns only disabled features",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = false }
                    feature(FEATURE_THREE) { isEnabled = false }
                },
                expectedUids = listOf(FEATURE_TWO, FEATURE_THREE),
            ),
            FeatureFilterData(
                description = "returns empty list when all features are enabled",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = true }
                },
                expectedUids = emptyList(),
            ),
            FeatureFilterData(
                description = "returns empty list when no features exist",
                featuresBlock = { },
                expectedUids = emptyList(),
            ),
        )

        private val featuresWithPermissionData = listOf(
            PermissionFilterData(
                description = "returns features with specified permission",
                featuresBlock = {
                    feature(FEATURE_ONE) {
                        isEnabled = true
                        permissions(PERMISSION_ADMIN, PERMISSION_BETA)
                    }
                    feature(FEATURE_TWO) {
                        isEnabled = true
                        permissions(PERMISSION_ADMIN)
                    }
                    feature(FEATURE_THREE) {
                        isEnabled = true
                        permissions(PERMISSION_BETA)
                    }
                    feature(FEATURE_FOUR) {
                        isEnabled = true
                    }
                },
                permission = PERMISSION_ADMIN,
                expectedUids = listOf(FEATURE_ONE, FEATURE_TWO),
            ),
            PermissionFilterData(
                description = "returns empty list when no features have permission",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) {
                        isEnabled = true
                        permissions(PERMISSION_BETA)
                    }
                },
                permission = PERMISSION_ADMIN,
                expectedUids = emptyList(),
            ),
            PermissionFilterData(
                description = "returns empty list when no features exist",
                featuresBlock = { },
                permission = PERMISSION_ADMIN,
                expectedUids = emptyList(),
            ),
        )

        private val reportGenerationData = listOf(
            ReportTestData(
                description = "contains header",
                featuresBlock = { feature(FEATURE_ONE) { isEnabled = true } },
                expectedContent = listOf("FF4K Feature Report", "==================="),
            ),
            ReportTestData(
                description = "contains statistics line",
                featuresBlock = {
                    feature(FEATURE_ONE) { isEnabled = true }
                    feature(FEATURE_TWO) { isEnabled = false }
                },
                expectedContent = listOf("Total: 2", "Enabled: 1", "Disabled: 1"),
            ),
            ReportTestData(
                description = "shows enabled feature with ON status",
                featuresBlock = { feature(FEATURE_ONE) { isEnabled = true } },
                expectedContent = listOf("[ON]  $FEATURE_ONE"),
            ),
            ReportTestData(
                description = "shows disabled feature with OFF status",
                featuresBlock = { feature(FEATURE_ONE) { isEnabled = false } },
                expectedContent = listOf("[OFF] $FEATURE_ONE"),
            ),
            ReportTestData(
                description = "shows group information",
                featuresBlock = {
                    feature(FEATURE_ONE) {
                        isEnabled = true
                        group = GROUP_UI
                    }
                },
                expectedContent = listOf("(group: $GROUP_UI)"),
            ),
            ReportTestData(
                description = "shows permissions information",
                featuresBlock = {
                    feature(FEATURE_ONE) {
                        isEnabled = true
                        permissions(PERMISSION_ADMIN, PERMISSION_BETA)
                    }
                },
                expectedContent = listOf(
                    "[PERMISSIONS:",
                    PERMISSION_ADMIN,
                    PERMISSION_BETA,
                ),
            ),
            ReportTestData(
                description = "shows no features message when empty",
                featuresBlock = { },
                expectedContent = listOf("(no features)"),
            ),
        )
    }
}
private data class ReportTestData(
    val description: String,
    val featuresBlock: com.yonatankarp.ff4k.dsl.feature.FeaturesBuilder.() -> Unit,
    val expectedContent: List<String>,
)

private data class FeatureFilterData(
    val description: String,
    val featuresBlock: com.yonatankarp.ff4k.dsl.feature.FeaturesBuilder.() -> Unit,
    val expectedUids: List<String>,
)

private data class PermissionFilterData(
    val description: String,
    val featuresBlock: com.yonatankarp.ff4k.dsl.feature.FeaturesBuilder.() -> Unit,
    val permission: String,
    val expectedUids: List<String>,
)
