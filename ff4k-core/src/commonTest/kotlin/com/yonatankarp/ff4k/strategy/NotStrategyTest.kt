package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest

internal class NotStrategyTest : FlippingStrategyContractTest() {
    override fun createStrategyForPassingCase(): FlippingStrategy = NotStrategy(
        AlwaysFalseFlippingStrategy,
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = NotStrategy(
        AlwaysTrueFlippingStrategy,
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()
    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = """{"type":"not","strategy":{"type":"alwaysFalse"}}"""
}
