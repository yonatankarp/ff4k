package com.yonatankarp.ff4k.test.contract.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Contract test for FlippingStrategy implementations.
 *
 * Extend this class to test custom flipping strategy implementations.
 *
 * @author Yonatan Karp-Rudin
 */
abstract class FlippingStrategyContractTest {

    /**
     * Creates an instance of the strategy being tested with the given init parameters.
     */
    protected abstract fun createStrategy(initParams: Map<String, String>): FlippingStrategy

    /**
     * Provides a set of init parameters for the strategy.
     */
    protected abstract fun sampleInitParams(): Map<String, String>

    /**
     * Provides an execution context that should result in the strategy evaluating to true.
     */
    protected abstract fun contextThatShouldPass(): FlippingExecutionContext

    /**
     * Provides an execution context that should result in the strategy evaluating to false.
     */
    protected abstract fun contextThatShouldFail(): FlippingExecutionContext

    @Test
    fun `should store init params`() = runTest {
        // Given
        val initParams = sampleInitParams()

        // When
        val strategy = createStrategy(initParams)

        // Then
        assertEquals(initParams, strategy.initParams)
    }

    @Test
    fun `should evaluate to true when context matches strategy criteria`() = runTest {
        // Given
        val initParams = sampleInitParams()
        val strategy = createStrategy(initParams)
        val context = contextThatShouldPass()

        // When
        val result = strategy.evaluate(FEATURE_ID, null, context)

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `should evaluate to false when context does not match strategy criteria`() = runTest {
        // Given
        val initParams = sampleInitParams()
        val strategy = createStrategy(initParams)
        val context = contextThatShouldFail()

        // When
        val result = strategy.evaluate(FEATURE_ID, null, context)

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `should handle null feature store`() = runTest {
        // Given
        val initParams = sampleInitParams()
        val strategy = createStrategy(initParams)
        val context = contextThatShouldPass()
        val store: FeatureStore? = null

        // When
        val result = strategy.evaluate(FEATURE_ID, store, context)

        // Then
        assertEquals(true, result)
    }

    private companion object {
        const val FEATURE_ID = "test-feature"
    }
}
