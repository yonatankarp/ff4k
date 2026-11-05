package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
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

        test("user percentage buckets stay stable across releases") {
            rolloutBucket("dark-mode", "alice") shouldBe 55
            rolloutBucket("dark-mode", "bob") shouldBe 39
            rolloutBucket("beta-checkout", "alice") shouldBe 0
            rolloutBucket("beta-checkout", "bob") shouldBe 46
            rolloutBucket("f", "550e8400-e29b-41d4-a716-446655440000") shouldBe 40
            rolloutBucket("f", "42") shouldBe 38
        }

        test("each feature picks its users independently") {
            val users = (1..100_000).map { "user-$it" }

            suspend fun rollout(
                id: String,
                percentage: Int,
            ) = users.filter { UserPercentageStrategy(percentage).evaluate(Feature(id, enabled = true), mapOf("userId" to it)) }.toSet()

            // same-length ids are the case a plain "feature:user" hashCode gets wrong
            val first = rollout("checkout-v2", 10)
            first.size shouldBeInRange 9_500..10_500
            first.intersect(rollout("new-search-", 10)).size shouldBeInRange 800..1_200
            rollout("small", 1).intersect(rollout("big", 50)).size shouldBeInRange 400..600
        }

        test("raising the percentage keeps the users already active") {
            val users = (1..1_000).map { mapOf("userId" to "user-$it") }
            val at10 = users.filter { UserPercentageStrategy(10).evaluate(feature, it) }
            at10.filterNot { UserPercentageStrategy(20).evaluate(feature, it) } shouldBe emptyList()
        }

        test("user percentage reads a custom context key") {
            UserPercentageStrategy(100, key = "tenant").evaluate(feature, mapOf("tenant" to "t")) shouldBe true
        }
    })
