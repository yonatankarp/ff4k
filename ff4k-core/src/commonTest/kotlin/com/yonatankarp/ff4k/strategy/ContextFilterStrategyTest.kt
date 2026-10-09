package com.yonatankarp.ff4k.strategy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ContextFilterStrategyTest :
    FunSpec({
        val strategy = ContextFilterStrategy("region", setOf("eu", "us"))

        test("true when the context value is allowed") {
            strategy.evaluate(feature, mapOf("region" to "eu")) shouldBe true
        }

        test("false when the value is not allowed or the key is missing") {
            strategy.evaluate(feature, mapOf("region" to "asia")) shouldBe false
            strategy.evaluate(feature, emptyMap()) shouldBe false
        }

        test("negation acts as a deny list") {
            (!strategy).evaluate(feature, mapOf("region" to "eu")) shouldBe false
            (!strategy).evaluate(feature, emptyMap()) shouldBe true
        }
    })
