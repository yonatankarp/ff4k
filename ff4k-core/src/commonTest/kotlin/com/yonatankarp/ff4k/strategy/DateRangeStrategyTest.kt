package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.test.contract.strategy.FlippingStrategyContractTest
import com.yonatankarp.ff4k.utils.fixedClock
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

internal class DateRangeStrategyTest :
    FlippingStrategyContractTest({

        val startDate = Instant.parse("2025-01-01T00:00:00.000Z")
        val endDate = Instant.parse("2025-01-30T00:00:00.000Z")

        context("evaluate boundary conditions") {
            test("throws IllegalArgumentException when startDate is after endDate") {
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    DateRangeStrategy(
                        startDate = endDate,
                        endDate = startDate,
                    )
                }
            }

            withData(
                nameFn = { "current: ${it.currentDate} -> expect: ${it.shouldBeEnabled}" },
                DateRangeTestCase(startDate, true),
                DateRangeTestCase(endDate, false),
                DateRangeTestCase(startDate.plus(1.milliseconds), true),
                DateRangeTestCase(endDate.minus(1.milliseconds), true),
                DateRangeTestCase(startDate.minus(1.milliseconds), false),
                DateRangeTestCase(endDate.plus(1.milliseconds), false),
            ) { (currentDate, shouldBeEnabled) ->
                // Given
                val strategy = DateRangeStrategy(
                    startDate = startDate,
                    endDate = endDate,
                    clock = fixedClock(currentDate),
                )
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val result = strategy.evaluate("test", store, context)

                // Then
                result shouldBe shouldBeEnabled
            }
        }
    }) {

    private val startDate = Instant.parse("2025-01-01T00:00:00.000Z")
    private val endDate = Instant.parse("2025-01-30T00:00:00.000Z")

    override fun createStrategyForPassingCase(): FlippingStrategy = DateRangeStrategy(
        startDate = startDate,
        endDate = endDate,
        clock = fixedClock(startDate.plus(1.days)),
    )

    override fun createStrategyForFailingCase(): FlippingStrategy = DateRangeStrategy(
        startDate = startDate,
        endDate = endDate,
        clock = fixedClock(startDate.minus(1.days)),
    )

    override fun contextThatShouldPass(): FlippingExecutionContext = FlippingExecutionContext()

    override fun contextThatShouldFail(): FlippingExecutionContext = FlippingExecutionContext()

    override fun expectedJsonForSampleParams(): String = // language=json
        """{"type":"dateRange","startDate":"2025-01-01T00:00:00Z","endDate":"2025-01-30T00:00:00Z"}"""
}

private data class DateRangeTestCase(val currentDate: Instant, val shouldBeEnabled: Boolean)
