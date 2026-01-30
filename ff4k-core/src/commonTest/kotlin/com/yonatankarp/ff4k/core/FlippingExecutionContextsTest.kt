package com.yonatankarp.ff4k.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests for FlippingExecutionContext extension functions and coroutine context propagation.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FlippingExecutionContextsTest :
    FunSpec({

        // ========== Extension Function Tests ==========

        test("withParameter should create new context with added parameter") {
            // Given
            val original = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

            // When
            val result = original.withParameter(REGION_KEY, REGION_EU)

            // Then
            result.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID
            result.get<String>(REGION_KEY) shouldBe REGION_EU
            // Original unchanged
            original.get<String>(REGION_KEY).shouldBeNull()
        }

        test("withParameter should override existing parameter") {
            // Given
            val original = FlippingExecutionContext(TIER_KEY to TIER_FREE)

            // When
            val result = original.withParameter(TIER_KEY, TIER_PREMIUM)

            // Then
            result.get<String>(TIER_KEY) shouldBe TIER_PREMIUM
            original.get<String>(TIER_KEY) shouldBe TIER_FREE
        }

        test("withParameters should create new context with multiple parameters") {
            // Given
            val original = FlippingExecutionContext(USER_ID_KEY to BOB_USER_ID)

            // When
            val result = original.withParameters(
                REGION_KEY to REGION_US,
                TIER_KEY to TIER_ENTERPRISE,
                REQUEST_COUNT_KEY to REQUEST_COUNT,
            )

            // Then
            result.get<String>(USER_ID_KEY) shouldBe BOB_USER_ID
            result.get<String>(REGION_KEY) shouldBe REGION_US
            result.get<String>(TIER_KEY) shouldBe TIER_ENTERPRISE
            result.get<Int>(REQUEST_COUNT_KEY) shouldBe REQUEST_COUNT
            // Original unchanged
            original.get<String>(REGION_KEY).shouldBeNull()
        }

        test("mergeWith should merge contexts with right precedence") {
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
            result.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID
            result.get<String>(TIER_KEY) shouldBe TIER_PREMIUM // right takes precedence
            result.get<String>(REGION_KEY) shouldBe REGION_APAC
            // Originals unchanged
            baseContext.get<String>(TIER_KEY) shouldBe TIER_FREE
        }

        test("vararg constructor should create context with parameters") {
            // Given/When
            val context = FlippingExecutionContext(
                USER_ID_KEY to ALICE_USER_ID,
                REGION_KEY to REGION_EU,
                TIER_KEY to TIER_PREMIUM,
            )

            // Then
            context.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID
            context.get<String>(REGION_KEY) shouldBe REGION_EU
            context.get<String>(TIER_KEY) shouldBe TIER_PREMIUM
        }

        // ========== Coroutine Context Propagation Tests ==========

        test("currentFlippingContext should return empty context when none set") {
            // When
            val context = currentFlippingContext()

            // Then
            context.isEmpty.shouldBeTrue()
        }

        test("withFlippingContext should make context available in block") {
            // Given
            val requestContext = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

            // When/Then
            withFlippingContext(requestContext) {
                val current = currentFlippingContext()
                current.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID
            }
        }

        test("withFlippingContext should restore previous context after block") {
            // Given
            val productionContext = FlippingExecutionContext(ENVIRONMENT_KEY to ENV_PRODUCTION)
            val stagingContext = FlippingExecutionContext(ENVIRONMENT_KEY to ENV_STAGING)

            // When/Then
            withFlippingContext(productionContext) {
                currentFlippingContext().get<String>(ENVIRONMENT_KEY) shouldBe ENV_PRODUCTION

                withFlippingContext(stagingContext) {
                    currentFlippingContext().get<String>(ENVIRONMENT_KEY) shouldBe ENV_STAGING
                }

                // Restored after inner block
                currentFlippingContext().get<String>(ENVIRONMENT_KEY) shouldBe ENV_PRODUCTION
            }
        }

        test("withFlippingParameters should merge with current context") {
            // Given
            val baseContext = FlippingExecutionContext(
                USER_ID_KEY to ALICE_USER_ID,
                TIER_KEY to TIER_FREE,
            )

            // When/Then
            withFlippingContext(baseContext) {
                withFlippingParameters(TIER_KEY to TIER_PREMIUM, REGION_KEY to REGION_EU) {
                    val current = currentFlippingContext()
                    current.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID // preserved
                    current.get<String>(TIER_KEY) shouldBe TIER_PREMIUM // overridden
                    current.get<String>(REGION_KEY) shouldBe REGION_EU // added
                }

                // After withFlippingParameters block, original values restored
                val afterBlock = currentFlippingContext()
                afterBlock.get<String>(USER_ID_KEY) shouldBe ALICE_USER_ID
                afterBlock.get<String>(TIER_KEY) shouldBe TIER_FREE
                (REGION_KEY in afterBlock).shouldBeFalse()
            }
        }

        test("withFlippingParameters should create context when none exists") {
            // When/Then
            withFlippingParameters(TENANT_KEY to ACME_TENANT_ID) {
                val current = currentFlippingContext()
                current.get<String>(TENANT_KEY) shouldBe ACME_TENANT_ID
            }
        }

        test("context should propagate through nested suspend calls") {
            // Given
            suspend fun innerFunction(): String? = currentFlippingContext().get<String>(USER_ID_KEY)

            suspend fun middleFunction(): String? = innerFunction()

            // When/Then
            withFlippingContext(FlippingExecutionContext(USER_ID_KEY to BOB_USER_ID)) {
                val result = middleFunction()
                result shouldBe BOB_USER_ID
            }
        }

        test("FlippingExecutionContext should be a CoroutineContext Element") {
            // Given
            val context = FlippingExecutionContext(USER_ID_KEY to ALICE_USER_ID)

            // Then
            context.key shouldBe FlippingExecutionContext.Key
        }
    }) {
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
