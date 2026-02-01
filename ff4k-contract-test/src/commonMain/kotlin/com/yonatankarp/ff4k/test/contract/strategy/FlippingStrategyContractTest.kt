@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.serialization.FF4kJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString

/**
 * Contract test for FlippingStrategy implementations.
 *
 * Extend this class to test custom flipping strategy implementations.
 *
 * @author Yonatan Karp-Rudin
 */
abstract class FlippingStrategyContractTest(body: FunSpec.() -> Unit = {}) : FunSpec(body) {

    /**
     * Creates an instance of the strategy configured to pass when evaluated with [contextThatShouldPass].
     * This allows strategies that depend on injected dependencies (e.g., clocks) to configure
     * themselves appropriately for the passing test case.
     */
    protected abstract fun createStrategyForPassingCase(): FlippingStrategy

    /**
     * Creates an instance of the strategy configured to fail when evaluated with [contextThatShouldFail].
     * This allows strategies that depend on injected dependencies (e.g., clocks) to configure
     * themselves appropriately for the failing test case.
     */
    protected abstract fun createStrategyForFailingCase(): FlippingStrategy

    /**
     * Provides an execution context that should result in the strategy evaluating to true.
     */
    protected abstract fun contextThatShouldPass(): FlippingExecutionContext

    /**
     * Provides an execution context that should result in the strategy evaluating to false.
     */
    protected abstract fun contextThatShouldFail(): FlippingExecutionContext

    /**
     * Provides the expected JSON representation for the strategy created by [createStrategyForPassingCase].
     * The JSON should include the polymorphic type discriminator if applicable.
     */
    protected abstract fun expectedJsonForSampleParams(): String

    init {
        test("should evaluate to true when context matches strategy criteria") {
            // Given
            val strategy = createStrategyForPassingCase()
            val context = contextThatShouldPass()

            // When
            val result = strategy.evaluate(FEATURE_ID, null, context)

            // Then
            result shouldBe true
        }

        test("should evaluate to false when context does not match strategy criteria") {
            // Given
            val strategy = createStrategyForFailingCase()
            val context = contextThatShouldFail()

            // When
            val result = strategy.evaluate(FEATURE_ID, null, context)

            // Then
            result shouldBe false
        }

        test("should handle null feature store") {
            // Given
            val strategy = createStrategyForPassingCase()
            val context = contextThatShouldPass()
            val store: FeatureStore? = null

            // When
            val result = strategy.evaluate(FEATURE_ID, store, context)

            // Then
            result shouldBe true
        }

        test("should serialize to correct json") {
            // Given
            val strategy = createStrategyForPassingCase()
            val expectedJson = FF4kJson.parseToJsonElement(expectedJsonForSampleParams())

            // When
            val serializer = PolymorphicSerializer(FlippingStrategy::class)
            val actualJson = FF4kJson.encodeToJsonElement(serializer, strategy)

            // Then
            actualJson shouldBe expectedJson
        }

        test("should work with round-trip serialization") {
            // Given
            val strategy = createStrategyForPassingCase()

            // When
            val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
            val deserializedStrategy = FF4kJson.decodeFromString<FlippingStrategy>(json)

            // Then
            deserializedStrategy shouldBe strategy
        }
    }

    private companion object {
        const val FEATURE_ID = "test-feature"
    }
}
