package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.serialization.FF4kJson
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString

internal class DenyListStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate") {
            test("returns true when deny list is empty") {
                // Given
                val strategy = DenyListStrategy(emptySet())
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext(ContextKeys.USER_ID to "Alice")

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns true when user ID is missing from context") {
                // Given
                val strategy = DenyListStrategy(setOf("Alice", "Bob"))
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns false only for users in the deny list") {
                // Given
                val strategy = DenyListStrategy(setOf("Alice", "Charlie"))
                val store = InMemoryFeatureStore()

                // When / Then
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Alice")).shouldBeFalse()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Charlie")).shouldBeFalse()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Bob")).shouldBeTrue()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "David")).shouldBeTrue()
            }

            test("handles null feature store") {
                // Given
                val strategy = DenyListStrategy(setOf("Alice"))
                val context = FlippingExecutionContext(ContextKeys.USER_ID to "Bob")

                // When
                val result = strategy.evaluate("test", null, context)

                // Then
                result.shouldBeTrue()
            }
        }

        context("serialization") {
            test("round-trip serialization preserves strategy") {
                // Given
                val strategy = DenyListStrategy(setOf("Alice", "Bob", "Charlie"))

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }

            test("round-trip serialization preserves empty list") {
                // Given
                val strategy = DenyListStrategy(emptySet())

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }
        }
    }) {

    override fun createStrategy(): FlippingStrategy = DenyListStrategy(setOf("Alice"))

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "Bob")

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "Alice")

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type": "denyList","denyList": ["Alice"]}"""
}
