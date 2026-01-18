package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.dsl.core.ff4k
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for FF4k filtering, statistics, and reporting extension functions.
 *
 * @author Yonatan Karp-Rudin
 */
class FF4kStatsTest {

    @Test
    fun `enabledFeatures returns only enabled features`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = false }
                feature(FEATURE_THREE) { isEnabled = true }
            }
        }

        // When
        val enabled = ff4k.enabledFeatures()

        // Then
        assertEquals(2, enabled.size)
        assertTrue(enabled.any { it.uid == FEATURE_ONE })
        assertTrue(enabled.any { it.uid == FEATURE_THREE })
    }

    @Test
    fun `enabledFeatures returns empty list when no features are enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = false }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val enabled = ff4k.enabledFeatures()

        // Then
        assertTrue(enabled.isEmpty())
    }

    @Test
    fun `enabledFeatures returns empty list when no features exist`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val enabled = ff4k.enabledFeatures()

        // Then
        assertTrue(enabled.isEmpty())
    }

    @Test
    fun `disabledFeatures returns only disabled features`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = false }
                feature(FEATURE_THREE) { isEnabled = false }
            }
        }

        // When
        val disabled = ff4k.disabledFeatures()

        // Then
        assertEquals(2, disabled.size)
        assertTrue(disabled.any { it.uid == FEATURE_TWO })
        assertTrue(disabled.any { it.uid == FEATURE_THREE })
    }

    @Test
    fun `disabledFeatures returns empty list when all features are enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = true }
            }
        }

        // When
        val disabled = ff4k.disabledFeatures()

        // Then
        assertTrue(disabled.isEmpty())
    }

    @Test
    fun `disabledFeatures returns empty list when no features exist`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val disabled = ff4k.disabledFeatures()

        // Then
        assertTrue(disabled.isEmpty())
    }

    @Test
    fun `featuresWithPermission returns features with specified permission`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
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
            }
        }

        // When
        val adminFeatures = ff4k.featuresWithPermission(PERMISSION_ADMIN)

        // Then
        assertEquals(2, adminFeatures.size)
        assertTrue(adminFeatures.any { it.uid == FEATURE_ONE })
        assertTrue(adminFeatures.any { it.uid == FEATURE_TWO })
    }

    @Test
    fun `featuresWithPermission returns empty list when no features have permission`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) {
                    isEnabled = true
                    permissions(PERMISSION_BETA)
                }
            }
        }

        // When
        val adminFeatures = ff4k.featuresWithPermission(PERMISSION_ADMIN)

        // Then
        assertTrue(adminFeatures.isEmpty())
    }

    @Test
    fun `featuresWithPermission returns empty list when no features exist`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val adminFeatures = ff4k.featuresWithPermission(PERMISSION_ADMIN)

        // Then
        assertTrue(adminFeatures.isEmpty())
    }

    @Test
    fun `featuresWithStrategy returns empty list when no features have strategy`() = runTest {
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
        assertTrue(strategyFeatures.isEmpty())
    }

    @Test
    fun `featuresWithStrategy returns empty list when no features exist`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val strategyFeatures = ff4k.featuresWithStrategy()

        // Then
        assertTrue(strategyFeatures.isEmpty())
    }

    @Test
    fun `stats returns correct counts`() = runTest {
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
        assertEquals(5, stats.total)
        assertEquals(3, stats.enabled)
        assertEquals(2, stats.disabled)
        assertEquals(2, stats.withPermissions)
        assertEquals(0, stats.withStrategy)
        assertEquals(2, stats.groups)
    }

    @Test
    fun `stats returns zeros when no features exist`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val stats = ff4k.stats()

        // Then
        assertEquals(0, stats.total)
        assertEquals(0, stats.enabled)
        assertEquals(0, stats.disabled)
        assertEquals(0, stats.withPermissions)
        assertEquals(0, stats.withStrategy)
        assertEquals(0, stats.groups)
    }

    @Test
    fun `stats counts zero groups when features have no groups`() = runTest {
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
        assertEquals(2, stats.total)
        assertEquals(0, stats.groups)
    }

    @Test
    fun `report contains header`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "FF4K Feature Report")
        assertContains(report, "===================")
    }

    @Test
    fun `report contains statistics line`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "Total: 2")
        assertContains(report, "Enabled: 1")
        assertContains(report, "Disabled: 1")
    }

    @Test
    fun `report shows enabled feature with ON status`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "[ON]  $FEATURE_ONE")
    }

    @Test
    fun `report shows disabled feature with OFF status`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = false }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "[OFF] $FEATURE_ONE")
    }

    @Test
    fun `report shows group information`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    group = GROUP_UI
                }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "(group: $GROUP_UI)")
    }

    @Test
    fun `report shows permissions information`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    permissions(PERMISSION_ADMIN, PERMISSION_BETA)
                }
            }
        }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "[PERMISSIONS:")
        assertContains(report, PERMISSION_ADMIN)
        assertContains(report, PERMISSION_BETA)
    }

    @Test
    fun `report shows no features message when empty`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val report = ff4k.report()

        // Then
        assertContains(report, "(no features)")
    }

    @Test
    fun `report features are sorted by uid`() = runTest {
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
        val aIndex = report.indexOf("a-feature")
        val mIndex = report.indexOf("m-feature")
        val zIndex = report.indexOf("z-feature")
        assertTrue(aIndex < mIndex)
        assertTrue(mIndex < zIndex)
    }

    companion object {
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
    }
}
