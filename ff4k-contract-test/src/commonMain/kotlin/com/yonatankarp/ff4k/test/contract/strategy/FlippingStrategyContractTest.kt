@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.strategy

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * Contract test for FlippingStrategy implementations.
 *
 * Extend this class to test custom flipping strategy implementations.
 *
 * @author Yonatan Karp-Rudin
 */
abstract class FlippingStrategyContractTest : FunSpec() {

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

    /**
     * Provides the expected JSON representation for the strategy created with [sampleInitParams].
     * The JSON should include the polymorphic type discriminator if applicable.
     */
    protected abstract fun expectedJsonForSampleParams(): String

    /**
     * The SerializersModule to use for serialization tests.
     * Defaults to [ff4kSerializersModule].
     * Override this to register the strategy implementation being tested if it's not in the default module.
     */
    protected open val serializersModule: SerializersModule = ff4kSerializersModule

    init {
        test("should store init params") {
            // Given
            val initParams = sampleInitParams()

            // When
            val strategy = createStrategy(initParams)

            // Then
            strategy.initParams shouldBe initParams
        }

        test("should evaluate to true when context matches strategy criteria") {
            // Given
            val initParams = sampleInitParams()
            val strategy = createStrategy(initParams)
            val context = contextThatShouldPass()

            // When
            val result = strategy.evaluate(FEATURE_ID, null, context)

            // Then
            result shouldBe true
        }

        test("should evaluate to false when context does not match strategy criteria") {
            // Given
            val initParams = sampleInitParams()
            val strategy = createStrategy(initParams)
            val context = contextThatShouldFail()

            // When
            val result = strategy.evaluate(FEATURE_ID, null, context)

            // Then
            result shouldBe false
        }

        test("should handle null feature store") {
            // Given
            val initParams = sampleInitParams()
            val strategy = createStrategy(initParams)
            val context = contextThatShouldPass()
            val store: FeatureStore? = null

            // When
            val result = strategy.evaluate(FEATURE_ID, store, context)

            // Then
            result shouldBe true
        }

        test("should serialize to correct json") {
            // Given
            val initParams = sampleInitParams()
            val strategy = createStrategy(initParams)
            val json = Json {
                serializersModule = this@FlippingStrategyContractTest.serializersModule
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            val expectedJson = json.parseToJsonElement(expectedJsonForSampleParams())

            // When
            val serializer = PolymorphicSerializer(FlippingStrategy::class)
            val actualJson = json.encodeToJsonElement(serializer, strategy)

            // Then
            actualJson shouldBe expectedJson
        }

        test("should deserialize from json") {
            // Given
            val initParams = sampleInitParams()
            val strategy = createStrategy(initParams)
            val json = Json {
                serializersModule = this@FlippingStrategyContractTest.serializersModule
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            val jsonString = expectedJsonForSampleParams()

            // When
            val serializer = PolymorphicSerializer(FlippingStrategy::class)
            val deserialized = json.decodeFromString(serializer, jsonString)

            // Then
            deserialized.initParams shouldBe strategy.initParams
        }
    }

    private companion object {
        const val FEATURE_ID = "test-feature"
    }
}
