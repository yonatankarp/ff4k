package com.yonatankarp.ff4k.strategy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe

class PercentageStrategyTest :
    FunSpec({
        test("0 is never active and 100 always is") {
            PercentageStrategy(0).evaluate(feature, emptyMap()) shouldBe false
            PercentageStrategy(100).evaluate(feature, emptyMap()) shouldBe true
        }

        test("roughly the requested share of evaluations is active") {
            val strategy = PercentageStrategy(30)
            val hits = (1..1000).count { strategy.evaluate(feature, emptyMap()) }
            hits shouldBeInRange 200..400
        }

        test("rejects percentages outside 0..100") {
            shouldThrow<IllegalArgumentException> { PercentageStrategy(101) }
            shouldThrow<IllegalArgumentException> { UserPercentageStrategy(-1) }
        }

        test("user percentage is stable per user and false without a user") {
            val strategy = UserPercentageStrategy(50)
            val first = strategy.evaluate(feature, mapOf("userId" to "alice"))
            repeat(10) { strategy.evaluate(feature, mapOf("userId" to "alice")) shouldBe first }
            strategy.evaluate(feature, emptyMap()) shouldBe false
        }

        test("user percentage buckets users across the whole range") {
            val strategy = UserPercentageStrategy(50)
            val hits = (1..1000).count { strategy.evaluate(feature, mapOf("userId" to "user-$it")) }
            hits shouldBeInRange 400..600
            UserPercentageStrategy(100).evaluate(feature, mapOf("userId" to "x")) shouldBe true
            UserPercentageStrategy(0).evaluate(feature, mapOf("userId" to "x")) shouldBe false
        }

        test("user percentage reads a custom context key") {
            UserPercentageStrategy(100, key = "tenant").evaluate(feature, mapOf("tenant" to "t")) shouldBe true
        }
    })
