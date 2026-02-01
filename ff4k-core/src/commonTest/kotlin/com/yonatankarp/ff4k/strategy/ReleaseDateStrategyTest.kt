package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

internal class ReleaseDateStrategyTest :
    FlippingStrategyContractTest({

        val releaseDate = Instant.parse("2025-12-08T00:00:00.000Z")

        context("evaluate boundary conditions") {
            test("returns true when now equals releaseDate (inclusive)") {
                // Given
                val strategy = ReleaseDateStrategy(
                    releaseDate = releaseDate,
                    clock = fixedClock(releaseDate),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns true when now is 1ms after releaseDate") {
                // Given
                val strategy = ReleaseDateStrategy(
                    releaseDate = releaseDate,
                    clock = fixedClock(releaseDate.plus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns false when now is 1ms before releaseDate") {
                // Given
                val strategy = ReleaseDateStrategy(
                    releaseDate = releaseDate,
                    clock = fixedClock(releaseDate.minus(1.milliseconds)),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }

            test("returns true when now is far in the future") {
                // Given
                val farFuture = Instant.parse("2099-01-01T00:00:00.000Z")
                val strategy = ReleaseDateStrategy(
                    releaseDate = releaseDate,
                    clock = fixedClock(farFuture),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeTrue()
            }

            test("returns false when now is far in the past") {
                // Given
                val farPast = Instant.parse("2000-01-01T00:00:00.000Z")
                val strategy = ReleaseDateStrategy(
                    releaseDate = releaseDate,
                    clock = fixedClock(farPast),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result.shouldBeFalse()
            }
        }
    }) {

    private val releaseDate = Instant.parse("2025-12-08T00:00:00.000Z")

    override fun createStrategyForPassingCase(): FlippingStrategy = ReleaseDateStrategy(
        releaseDate = releaseDate,
        clock = fixedClock(releaseDate),
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = ReleaseDateStrategy(
        releaseDate = releaseDate,
        clock = fixedClock(Instant.parse("2025-12-07T23:59:59.999Z")),
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"releaseDate","releaseDate":"2025-12-08T00:00:00Z"}"""
}
