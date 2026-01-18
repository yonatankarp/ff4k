package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.dsl.core.ff4k
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for FF4k conditional execution and batch check extension functions.
 *
 * @author Yonatan Karp-Rudin
 */
class FF4kExtensionsTest {

    @Test
    fun `ifEnabled executes block when feature is enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                }
            }
        }

        // When
        val result = ff4k.ifEnabled(FEATURE_TEST) { VALUE_EXECUTED }

        // Then
        assertEquals(VALUE_EXECUTED, result)
    }

    @Test
    fun `ifEnabled returns null when feature is disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = false
                }
            }
        }

        // When
        val result = ff4k.ifEnabled(FEATURE_TEST) {
            fail("Block should not execute when feature is disabled")
        }

        // Then
        assertNull(result)
    }

    @Test
    fun `ifEnabled executes block when strategy matches context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }

        // When
        val result = ff4k.ifEnabled(
            FEATURE_TEST,
            FlippingExecutionContext(KEY_USER_ID to TARGET_USER_ID),
        ) { VALUE_EXECUTED }

        // Then
        assertEquals(VALUE_EXECUTED, result)
    }

    @Test
    fun `ifEnabled returns null when strategy does not match context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }

        // When
        val result = ff4k.ifEnabled(
            FEATURE_TEST,
            FlippingExecutionContext(KEY_USER_ID to OTHER_USER_ID),
        ) {
            fail("Block should not execute when strategy does not match")
        }

        // Then
        assertNull(result)
    }

    @Test
    fun `ifEnabledOrElse executes enabled block when feature is enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                }
            }
        }

        // When
        val result = ff4k.ifEnabledOrElse(
            FEATURE_TEST,
            enabled = { VALUE_ENABLED },
            disabled = { fail("Disabled block should not execute when feature is enabled") },
        )

        // Then
        assertEquals(VALUE_ENABLED, result)
    }

    @Test
    fun `ifEnabledOrElse executes disabled block when feature is disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = false
                }
            }
        }

        // When
        val result = ff4k.ifEnabledOrElse(
            FEATURE_TEST,
            enabled = { fail("Enabled block should not execute when feature is disabled") },
            disabled = { VALUE_DISABLED },
        )

        // Then
        assertEquals(VALUE_DISABLED, result)
    }

    @Test
    fun `ifEnabledOrElse executes enabled block when strategy matches context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }

        // When
        val result = ff4k.ifEnabledOrElse(
            FEATURE_TEST,
            FlippingExecutionContext(KEY_USER_ID to TARGET_USER_ID),
            enabled = { VALUE_ENABLED },
            disabled = { fail("Disabled block should not execute when strategy matches") },
        )

        // Then
        assertEquals(VALUE_ENABLED, result)
    }

    @Test
    fun `ifEnabledOrElse executes disabled block when strategy does not match context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }

        // When
        val result = ff4k.ifEnabledOrElse(
            FEATURE_TEST,
            FlippingExecutionContext(KEY_USER_ID to OTHER_USER_ID),
            enabled = { fail("Enabled block should not execute when strategy does not match") },
            disabled = { VALUE_DISABLED },
        )

        // Then
        assertEquals(VALUE_DISABLED, result)
    }

    @Test
    fun `whenEnabled executes block when feature is enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                }
            }
        }
        var executed = false

        // When
        ff4k.whenEnabled(FEATURE_TEST) { executed = true }

        // Then
        assertTrue(executed)
    }

    @Test
    fun `whenEnabled does not execute block when feature is disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = false
                }
            }
        }

        // When/Then
        ff4k.whenEnabled(FEATURE_TEST) {
            fail("Block should not execute when feature is disabled")
        }
    }

    @Test
    fun `whenEnabled executes block when strategy matches context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }
        var executed = false

        // When
        ff4k.whenEnabled(FEATURE_TEST, FlippingExecutionContext(KEY_USER_ID to TARGET_USER_ID)) {
            executed = true
        }

        // Then
        assertTrue(executed)
    }

    @Test
    fun `whenEnabled does not execute block when strategy does not match context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_TEST) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
            }
        }

        // When/Then
        ff4k.whenEnabled(FEATURE_TEST, FlippingExecutionContext(KEY_USER_ID to OTHER_USER_ID)) {
            fail("Block should not execute when strategy does not match")
        }
    }

    @Test
    fun `checkAll returns true when all features are enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = true }
                feature(FEATURE_THREE) { isEnabled = true }
            }
        }

        // When
        val result = ff4k.checkAll(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAll returns false when any feature is disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = false }
                feature(FEATURE_THREE) { isEnabled = true }
            }
        }

        // When
        val result = ff4k.checkAll(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE)

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkAll returns false when all features are disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = false }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val result = ff4k.checkAll(FEATURE_ONE, FEATURE_TWO)

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkAll returns true for empty feature list`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val result = ff4k.checkAll()

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAll returns true when strategy matches context for all features`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
                feature(FEATURE_TWO) { isEnabled = true }
            }
        }

        // When
        val result = ff4k.checkAll(
            FEATURE_ONE,
            FEATURE_TWO,
            executionContext = FlippingExecutionContext(KEY_USER_ID to TARGET_USER_ID),
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAll returns false when strategy does not match context`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
                feature(FEATURE_TWO) { isEnabled = true }
            }
        }

        // When
        val result = ff4k.checkAll(
            FEATURE_ONE,
            FEATURE_TWO,
            executionContext = FlippingExecutionContext(KEY_USER_ID to OTHER_USER_ID),
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkAny returns true when all features are enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = true }
                feature(FEATURE_TWO) { isEnabled = true }
            }
        }

        // When
        val result = ff4k.checkAny(FEATURE_ONE, FEATURE_TWO)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAny returns true when at least one feature is enabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = false }
                feature(FEATURE_TWO) { isEnabled = true }
                feature(FEATURE_THREE) { isEnabled = false }
            }
        }

        // When
        val result = ff4k.checkAny(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAny returns false when all features are disabled`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) { isEnabled = false }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val result = ff4k.checkAny(FEATURE_ONE, FEATURE_TWO)

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkAny returns false for empty feature list`() = runTest {
        // Given
        val ff4k = ff4k { }

        // When
        val result = ff4k.checkAny()

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkAny returns true when strategy matches context for at least one feature`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val result = ff4k.checkAny(
            FEATURE_ONE,
            FEATURE_TWO,
            executionContext = FlippingExecutionContext(KEY_USER_ID to TARGET_USER_ID),
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkAny returns false when strategy does not match context for any feature`() = runTest {
        // Given
        val ff4k = ff4k {
            features {
                feature(FEATURE_ONE) {
                    isEnabled = true
                    flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                }
                feature(FEATURE_TWO) { isEnabled = false }
            }
        }

        // When
        val result = ff4k.checkAny(
            FEATURE_ONE,
            FEATURE_TWO,
            executionContext = FlippingExecutionContext(KEY_USER_ID to OTHER_USER_ID),
        )

        // Then
        assertFalse(result)
    }

    companion object {
        // Feature IDs
        private const val FEATURE_TEST = "test-feature"
        private const val FEATURE_ONE = "feature-1"
        private const val FEATURE_TWO = "feature-2"
        private const val FEATURE_THREE = "feature-3"

        // Values
        private const val VALUE_EXECUTED = "executed"
        private const val VALUE_ENABLED = "enabled"
        private const val VALUE_DISABLED = "disabled"

        // Context keys and values
        private const val KEY_USER_ID = "userId"
        private const val TARGET_USER_ID = "user-123"
        private const val OTHER_USER_ID = "user-456"
    }

    /**
     * Simple flipping strategy that checks if the context contains a specific user ID.
     */
    private class UserIdStrategy(private val allowedUserId: String) : FlippingStrategy {
        override val initParams: Map<String, String> = mapOf("allowedUserId" to allowedUserId)

        override suspend fun evaluate(
            featureId: String,
            store: FeatureStore?,
            context: FlippingExecutionContext,
        ): Boolean {
            val userId = context.get<String>(KEY_USER_ID)
            return userId == allowedUserId
        }
    }
}
