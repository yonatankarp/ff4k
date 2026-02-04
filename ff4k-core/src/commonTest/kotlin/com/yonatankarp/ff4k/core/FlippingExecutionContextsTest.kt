package com.yonatankarp.ff4k.core

import io.kotest.assertions.throwables.shouldThrow
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
            val original = FlippingExecutionContext(ContextKeys.USER_ID to ALICE_USER_ID)

            // When
            val result = original.withParameter(ContextKeys.REGION, REGION_EU)

            // Then
            result.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID
            result.get<String>(ContextKeys.REGION) shouldBe REGION_EU
            // Original unchanged
            original.get<String>(ContextKeys.REGION).shouldBeNull()
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
            val original = FlippingExecutionContext(ContextKeys.USER_ID to BOB_USER_ID)

            // When
            val result = original.withParameters(
                ContextKeys.REGION to REGION_US,
                TIER_KEY to TIER_ENTERPRISE,
                REQUEST_COUNT_KEY to REQUEST_COUNT,
            )

            // Then
            result.get<String>(ContextKeys.USER_ID) shouldBe BOB_USER_ID
            result.get<String>(ContextKeys.REGION) shouldBe REGION_US
            result.get<String>(TIER_KEY) shouldBe TIER_ENTERPRISE
            result.get<Int>(REQUEST_COUNT_KEY) shouldBe REQUEST_COUNT
            // Original unchanged
            original.get<String>(ContextKeys.REGION).shouldBeNull()
        }

        test("mergeWith should merge contexts with right precedence") {
            // Given
            val baseContext = FlippingExecutionContext(
                ContextKeys.USER_ID to ALICE_USER_ID,
                TIER_KEY to TIER_FREE,
            )
            val overrideContext = FlippingExecutionContext(
                TIER_KEY to TIER_PREMIUM,
                ContextKeys.REGION to REGION_APAC,
            )

            // When
            val result = baseContext.mergeWith(overrideContext)

            // Then
            result.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID
            result.get<String>(TIER_KEY) shouldBe TIER_PREMIUM // right takes precedence
            result.get<String>(ContextKeys.REGION) shouldBe REGION_APAC
            // Originals unchanged
            baseContext.get<String>(TIER_KEY) shouldBe TIER_FREE
        }

        test("vararg constructor should create context with parameters") {
            // Given/When
            val context = FlippingExecutionContext(
                ContextKeys.USER_ID to ALICE_USER_ID,
                ContextKeys.REGION to REGION_EU,
                TIER_KEY to TIER_PREMIUM,
            )

            // Then
            context.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID
            context.get<String>(ContextKeys.REGION) shouldBe REGION_EU
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
            val requestContext = FlippingExecutionContext(ContextKeys.USER_ID to ALICE_USER_ID)

            // When/Then
            withFlippingContext(requestContext) {
                val current = currentFlippingContext()
                current.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID
            }
        }

        test("withFlippingContext should restore previous context after block") {
            // Given
            val productionContext = FlippingExecutionContext(ContextKeys.ENVIRONMENT to ENV_PRODUCTION)
            val stagingContext = FlippingExecutionContext(ContextKeys.ENVIRONMENT to ENV_STAGING)

            // When/Then
            withFlippingContext(productionContext) {
                currentFlippingContext().get<String>(ContextKeys.ENVIRONMENT) shouldBe ENV_PRODUCTION

                withFlippingContext(stagingContext) {
                    currentFlippingContext().get<String>(ContextKeys.ENVIRONMENT) shouldBe ENV_STAGING
                }

                // Restored after inner block
                currentFlippingContext().get<String>(ContextKeys.ENVIRONMENT) shouldBe ENV_PRODUCTION
            }
        }

        test("withFlippingParameters should merge with current context") {
            // Given
            val baseContext = FlippingExecutionContext(
                ContextKeys.USER_ID to ALICE_USER_ID,
                TIER_KEY to TIER_FREE,
            )

            // When/Then
            withFlippingContext(baseContext) {
                withFlippingParameters(TIER_KEY to TIER_PREMIUM, ContextKeys.REGION to REGION_EU) {
                    val current = currentFlippingContext()
                    current.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID // preserved
                    current.get<String>(TIER_KEY) shouldBe TIER_PREMIUM // overridden
                    current.get<String>(ContextKeys.REGION) shouldBe REGION_EU // added
                }

                // After withFlippingParameters block, original values restored
                val afterBlock = currentFlippingContext()
                afterBlock.get<String>(ContextKeys.USER_ID) shouldBe ALICE_USER_ID
                afterBlock.get<String>(TIER_KEY) shouldBe TIER_FREE
                (ContextKeys.REGION in afterBlock).shouldBeFalse()
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
            suspend fun innerFunction(): String? = currentFlippingContext().get<String>(ContextKeys.USER_ID)

            suspend fun middleFunction(): String? = innerFunction()

            // When/Then
            withFlippingContext(FlippingExecutionContext(ContextKeys.USER_ID to BOB_USER_ID)) {
                val result = middleFunction()
                result shouldBe BOB_USER_ID
            }
        }

        test("FlippingExecutionContext should be a CoroutineContext Element") {
            // Given
            val context = FlippingExecutionContext(ContextKeys.USER_ID to ALICE_USER_ID)

            // Then
            context.key shouldBe FlippingExecutionContext.Key
        }

        // ========== getOrThrow Tests ==========

        test("getOrThrow should return value when key exists") {
            // Given
            val context = FlippingExecutionContext(ContextKeys.USER_ID to ALICE_USER_ID)

            // When
            val result: String = context.getOrThrow(ContextKeys.USER_ID)

            // Then
            result shouldBe ALICE_USER_ID
        }

        test("getOrThrow should throw IllegalStateException when key is missing") {
            // Given
            val context = FlippingExecutionContext()

            // When/Then
            shouldThrow<IllegalStateException> {
                context.getOrThrow<String>(ContextKeys.USER_ID)
            }.message shouldBe "To work with FlippingExecutionContext you must provide '${ContextKeys.USER_ID}' parameter in execution context"
        }

        test("getOrThrow should work with different types") {
            // Given
            val context = FlippingExecutionContext(
                ContextKeys.USER_ID to ALICE_USER_ID,
                REQUEST_COUNT_KEY to REQUEST_COUNT,
            )

            // When
            val userId: String = context.getOrThrow(ContextKeys.USER_ID)
            val count: Int = context.getOrThrow(REQUEST_COUNT_KEY)

            // Then
            userId shouldBe ALICE_USER_ID
            count shouldBe REQUEST_COUNT
        }
    }) {
    companion object {
        // Context keys (custom keys not in ContextKeys)
        private const val TIER_KEY = "tier"
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
