package com.yonatankarp.ff4k.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FlippingExecutionContext extension functions and coroutine context propagation.
 *
 * @author Yonatan Karp-Rudin
 */
class FlippingExecutionContextsTest {

    // ========== Extension Function Tests ==========

    @Test
    fun `withParameter should create new context with added parameter`() {
        // Given
        val original = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

        // When
        val result = original.withParameter(REGION_KEY, REGION_EU)

        // Then
        assertEquals(ALICE_USER_ID, result[USER_ID_KEY])
        assertEquals(REGION_EU, result[REGION_KEY])
        // Original unchanged
        assertNull(original[REGION_KEY])
    }

    @Test
    fun `withParameter should override existing parameter`() {
        // Given
        val original = FlippingExecutionContext(TIER_KEY to TIER_FREE)

        // When
        val result = original.withParameter(TIER_KEY, TIER_PREMIUM)

        // Then
        assertEquals(TIER_PREMIUM, result[TIER_KEY])
        assertEquals(TIER_FREE, original[TIER_KEY])
    }

    @Test
    fun `withParameters should create new context with multiple parameters`() {
        // Given
        val original = FlippingExecutionContext(USER_ID_KEY to BOB_USER_ID)

        // When
        val result = original.withParameters(
            REGION_KEY to REGION_US,
            TIER_KEY to TIER_ENTERPRISE,
            REQUEST_COUNT_KEY to REQUEST_COUNT,
        )

        // Then
        assertEquals(BOB_USER_ID, result[USER_ID_KEY])
        assertEquals(REGION_US, result[REGION_KEY])
        assertEquals(TIER_ENTERPRISE, result[TIER_KEY])
        assertEquals(REQUEST_COUNT, result[REQUEST_COUNT_KEY])
        // Original unchanged
        assertNull(original[REGION_KEY])
    }

    @Test
    fun `mergeWith should merge contexts with right precedence`() {
        // Given
        val baseContext = FlippingExecutionContext(
            USER_ID_KEY to ALICE_USER_ID,
            TIER_KEY to TIER_FREE,
        )
        val overrideContext = FlippingExecutionContext(
            TIER_KEY to TIER_PREMIUM,
            REGION_KEY to REGION_APAC,
        )

        // When
        val result = baseContext.mergeWith(overrideContext)

        // Then
        assertEquals(ALICE_USER_ID, result[USER_ID_KEY])
        assertEquals(TIER_PREMIUM, result[TIER_KEY]) // right takes precedence
        assertEquals(REGION_APAC, result[REGION_KEY])
        // Originals unchanged
        assertEquals(TIER_FREE, baseContext[TIER_KEY])
    }

    @Test
    fun `vararg constructor should create context with parameters`() {
        // Given/When
        val context = FlippingExecutionContext(
            USER_ID_KEY to ALICE_USER_ID,
            REGION_KEY to REGION_EU,
            TIER_KEY to TIER_PREMIUM,
        )

        // Then
        assertEquals(ALICE_USER_ID, context[USER_ID_KEY])
        assertEquals(REGION_EU, context[REGION_KEY])
        assertEquals(TIER_PREMIUM, context[TIER_KEY])
    }

    // ========== Coroutine Context Propagation Tests ==========

    @Test
    fun `currentFlippingContext should return empty context when none set`() = runTest {
        // When
        val context = currentFlippingContext()

        // Then
        assertTrue(context.isEmpty)
    }

    @Test
    fun `withFlippingContext should make context available in block`() = runTest {
        // Given
        val requestContext = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

        // When/Then
        withFlippingContext(requestContext) {
            val current = currentFlippingContext()
            assertEquals(ALICE_USER_ID, current[USER_ID_KEY])
        }
    }

    @Test
    fun `withFlippingContext should restore previous context after block`() = runTest {
        // Given
        val productionContext = FlippingExecutionContext(ENVIRONMENT_KEY to ENV_PRODUCTION)
        val stagingContext = FlippingExecutionContext(ENVIRONMENT_KEY to ENV_STAGING)

        // When/Then
        withFlippingContext(productionContext) {
            assertEquals(ENV_PRODUCTION, currentFlippingContext()[ENVIRONMENT_KEY])

            withFlippingContext(stagingContext) {
                assertEquals(ENV_STAGING, currentFlippingContext()[ENVIRONMENT_KEY])
            }

            // Restored after inner block
            assertEquals(ENV_PRODUCTION, currentFlippingContext()[ENVIRONMENT_KEY])
        }
    }

    @Test
    fun `withFlippingParameters should merge with current context`() = runTest {
        // Given
        val baseContext = FlippingExecutionContext(
            USER_ID_KEY to ALICE_USER_ID,
            TIER_KEY to TIER_FREE,
        )

        // When/Then
        withFlippingContext(baseContext) {
            withFlippingParameters(TIER_KEY to TIER_PREMIUM, REGION_KEY to REGION_EU) {
                val current = currentFlippingContext()
                assertEquals(ALICE_USER_ID, current[USER_ID_KEY]) // preserved
                assertEquals(TIER_PREMIUM, current[TIER_KEY]) // overridden
                assertEquals(REGION_EU, current[REGION_KEY]) // added
            }

            // After withFlippingParameters block, original values restored
            val afterBlock = currentFlippingContext()
            assertEquals(ALICE_USER_ID, afterBlock[USER_ID_KEY])
            assertEquals(TIER_FREE, afterBlock[TIER_KEY])
            assertFalse(REGION_KEY in afterBlock)
        }
    }

    @Test
    fun `withFlippingParameters should create context when none exists`() = runTest {
        // When/Then
        withFlippingParameters(TENANT_KEY to ACME_TENANT_ID) {
            val current = currentFlippingContext()
            assertEquals(ACME_TENANT_ID, current[TENANT_KEY])
        }
    }

    @Test
    fun `context should propagate through nested suspend calls`() = runTest {
        // Given
        suspend fun innerFunction(): String? = currentFlippingContext()[USER_ID_KEY]

        suspend fun middleFunction(): String? = innerFunction()

        // When/Then
        withFlippingContext(FlippingExecutionContext(USER_ID_KEY to BOB_USER_ID)) {
            val result = middleFunction()
            assertEquals(BOB_USER_ID, result)
        }
    }

    @Test
    fun `FlippingExecutionContext should be a CoroutineContext Element`() {
        // Given
        val context = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

        // Then
        assertEquals(FlippingExecutionContext.Key, context.key)
    }

    companion object {
        // Context keys
        private const val USER_ID_KEY = "userId"
        private const val REGION_KEY = "region"
        private const val TIER_KEY = "tier"
        private const val ENVIRONMENT_KEY = "environment"
        private const val TENANT_KEY = "tenantId"
        private const val REQUEST_COUNT_KEY = "requestCount"

        // User IDs
        private const val ALICE_USER_ID = "user-alice-001"
        private const val BOB_USER_ID = "user-bob-002"

        // Regions
        private const val REGION_EU = "eu-west-1"
        private const val REGION_US = "us-east-1"
        private const val REGION_APAC = "ap-southeast-1"

        // Subscription tiers
        private const val TIER_FREE = "free"
        private const val TIER_PREMIUM = "premium"
        private const val TIER_ENTERPRISE = "enterprise"

        // Environments
        private const val ENV_PRODUCTION = "production"
        private const val ENV_STAGING = "staging"

        // Tenant IDs
        private const val ACME_TENANT_ID = "tenant-acme-corp"

        // Numeric values
        private const val REQUEST_COUNT = 42
    }
}
