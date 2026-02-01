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

internal class AllowListStrategyTest :
    FlippingStrategyContractTest({

        context("evaluate") {
            test("returns false when allow list is empty") {
                // Given
                val strategy = AllowListStrategy(emptySet())
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext(ContextKeys.USER_ID to "Alice")

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }

            test("returns false when user ID is missing from context") {
                // Given
                val strategy = AllowListStrategy(setOf("Alice", "Bob"))
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }

            test("returns true only for users in the allow list") {
                // Given
                val strategy = AllowListStrategy(setOf("Alice", "Charlie"))
                val store = InMemoryFeatureStore()

                // When / Then
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Alice")).shouldBeTrue()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Charlie")).shouldBeTrue()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "Bob")).shouldBeFalse()
                strategy.evaluate("test", store, FlippingExecutionContext(ContextKeys.USER_ID to "David")).shouldBeFalse()
            }

            test("handles null feature store") {
                // Given
                val strategy = AllowListStrategy(setOf("Alice"))
                val context = FlippingExecutionContext(ContextKeys.USER_ID to "Alice")

                // When
                val result = strategy.evaluate("test", null, context)

                // Then
                result.shouldBeTrue()
            }
        }

        context("serialization") {
            test("round-trip serialization preserves strategy") {
                // Given
                val strategy = AllowListStrategy(setOf("Alice", "Bob", "Charlie"))

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }

            test("round-trip serialization preserves empty list") {
                // Given
                val strategy = AllowListStrategy(emptySet())

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }
        }
    }) {

    override fun createStrategyForPassingCase(): FlippingStrategy = AllowListStrategy(setOf("Alice"))

    override fun createStrategyForFailingCase(): FlippingStrategy = AllowListStrategy(setOf("Alice"))

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "Alice")

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext(ContextKeys.USER_ID to "Bob")

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"allowList","allowedList":["Alice"]}"""
}
