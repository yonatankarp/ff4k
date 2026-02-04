package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.matchers.shouldBe

class ClientFilterStrategyTest : FlippingStrategyContractTest() {

    init {
        test("vararg constructor populates granted clients") {
            ClientFilterStrategy("client-a", "client-b").grantedClients shouldBe setOf("client-a", "client-b")
        }
    }

    override fun createStrategyForPassingCase(): FlippingStrategy = ClientFilterStrategy(setOf(ALLOWED_CLIENT_HOSTNAME))

    override fun createStrategyForFailingCase(): FlippingStrategy = ClientFilterStrategy(setOf(ALLOWED_CLIENT_HOSTNAME))

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.CLIENT_HOSTNAME to ALLOWED_CLIENT_HOSTNAME),
    )

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(
        values = mapOf(ContextKeys.CLIENT_HOSTNAME to REJECTED_CLIENT_HOSTNAME),
    )

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"clientFilter","grantedClients":["$ALLOWED_CLIENT_HOSTNAME"]}"""

    override fun requiredContextKeys(): Set<String> = setOf(ContextKeys.CLIENT_HOSTNAME)

    companion object {
        private const val ALLOWED_CLIENT_HOSTNAME = "http://localhost:8080"
        private const val REJECTED_CLIENT_HOSTNAME = "http://www.google.com"
    }
}
