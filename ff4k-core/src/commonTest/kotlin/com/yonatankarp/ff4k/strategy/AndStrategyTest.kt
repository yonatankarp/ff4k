package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest

internal class AndStrategyTest : FlippingStrategyContractTest() {
    override fun createStrategyForPassingCase(): FlippingStrategy = AndStrategy(
        listOf(AlwaysTrueFlippingStrategy, AlwaysTrueFlippingStrategy),
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = AndStrategy(
        listOf(AlwaysTrueFlippingStrategy, AlwaysFalseFlippingStrategy),
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()
    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = """{"type":"and","strategies":[{"type":"alwaysTrue"},{"type":"alwaysTrue"}]}"""
}
