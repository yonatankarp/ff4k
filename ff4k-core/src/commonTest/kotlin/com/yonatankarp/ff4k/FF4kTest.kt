package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.strategy.ContextFilterStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class FF4kTest :
    FunSpec({
        suspend fun ff4k(vararg features: Feature) = FF4k().apply { features.forEach { this.features.put(it) } }

        test("check is true for an enabled feature without strategy") {
            ff4k(Feature("f", enabled = true)).check("f") shouldBe true
        }

        test("check is false for a disabled feature even if its strategy passes") {
            val feature = Feature("f", enabled = false, strategy = FlippingStrategy { _, _ -> true })
            ff4k(feature).check("f") shouldBe false
        }

        test("check is false for an unknown feature") {
            ff4k().check("missing") shouldBe false
        }

        test("check delegates to the strategy with the context") {
            val feature = Feature("f", enabled = true, strategy = ContextFilterStrategy("region", setOf("eu")))
            val ff4k = ff4k(feature)
            ff4k.check("f", mapOf("region" to "eu")) shouldBe true
            ff4k.check("f", mapOf("region" to "us")) shouldBe false
            ff4k.check("f") shouldBe false
        }

        test("property returns the typed value or null on type mismatch") {
            val ff4k = FF4k().apply { properties.put(Property("limit", 3)) }
            ff4k.property<Int>("limit") shouldBe 3
            ff4k.property<String>("limit").shouldBeNull()
            ff4k.property<Int>("missing").shouldBeNull()
        }

        test("can be built from a configuration") {
            val configuration = FF4kConfiguration(
                features = listOf(Feature("f", enabled = true)),
                properties = listOf(Property("limit", 3)),
            )
            val ff4k = FF4k(configuration)
            ff4k.check("f") shouldBe true
            ff4k.property<Int>("limit") shouldBe 3
        }
    })
