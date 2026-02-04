package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class ServerFilterStrategyTest : FlippingStrategyContractTest() {

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

        test("vararg constructor populates target servers") {
            ServerFilterStrategy("server-a", "server-b").targetServers shouldBe setOf("server-a", "server-b")
        }
    }

    override fun createStrategyForPassingCase(): FlippingStrategy = ServerFilterStrategy(setOf(ALLOWED_SERVER_HOSTNAME))

    override fun createStrategyForFailingCase(): FlippingStrategy = ServerFilterStrategy(setOf(ALLOWED_SERVER_HOSTNAME))

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.SERVER_HOSTNAME to ALLOWED_SERVER_HOSTNAME),
    )

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.SERVER_HOSTNAME to REJECTED_SERVER_HOSTNAME),
    )

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"serverFilter","targetServers":["$ALLOWED_SERVER_HOSTNAME"]}"""

    companion object {
        private const val ALLOWED_SERVER_HOSTNAME = "http://localhost:8080"
        private const val REJECTED_SERVER_HOSTNAME = "http://www.google.com"
    }
}
