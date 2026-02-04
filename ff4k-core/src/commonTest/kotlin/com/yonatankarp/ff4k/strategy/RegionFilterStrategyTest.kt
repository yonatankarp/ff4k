package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class RegionFilterStrategyTest : FlippingStrategyContractTest() {
    init {
        test("context key is required") {
            // Given
            val strategy = createStrategyForPassingCase()
            val store = InMemoryFeatureStore()
            val emptyContext = FlippingExecutionContext()

            // When / Then
            shouldThrow<IllegalStateException> {
                strategy.evaluate("test", store, emptyContext)
            }
        }

        test("vararg constructor populates granted regions") {
            RegionFilterStrategy("eu-central-1", "us-west-2").grantedRegions shouldBe setOf("eu-central-1", "us-west-2")
        }
    }

    override fun createStrategyForPassingCase(): FlippingStrategy = RegionFilterStrategy(setOf(ALLOWED_REGIONS))

    override fun createStrategyForFailingCase(): FlippingStrategy = RegionFilterStrategy(setOf(ALLOWED_REGIONS))

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.REGION to ALLOWED_REGIONS),
    )

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.REGION to REJECTED_REGIONS),
    )

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"region","grantedRegions":["$ALLOWED_REGIONS"]}"""

    companion object {
        private const val ALLOWED_REGIONS = "eu-central-1"
        private const val REJECTED_REGIONS = "us-west-3"
    }
}
