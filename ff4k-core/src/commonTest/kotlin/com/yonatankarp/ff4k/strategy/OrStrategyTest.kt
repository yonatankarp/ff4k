package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest

internal class OrStrategyTest : FlippingStrategyContractTest() {
    override fun createStrategyForPassingCase(): FlippingStrategy = OrStrategy(
        listOf(AlwaysTrueFlippingStrategy, AlwaysFalseFlippingStrategy),
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = OrStrategy(
        listOf(AlwaysFalseFlippingStrategy, AlwaysFalseFlippingStrategy),
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()
    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = """{"type":"or","strategies":[{"type":"alwaysTrue"},{"type":"alwaysFalse"}]}"""
}
