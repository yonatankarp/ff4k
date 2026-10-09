package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.FlippingStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CompositeStrategiesTest :
    FunSpec({
        val yes = FlippingStrategy { _, _ -> true }
        val no = FlippingStrategy { _, _ -> false }

        test("and requires every strategy") {
            (yes and yes).evaluate(feature, emptyMap()) shouldBe true
            (yes and no).evaluate(feature, emptyMap()) shouldBe false
            AndStrategy(emptyList()).evaluate(feature, emptyMap()) shouldBe true
        }

        test("or requires any strategy") {
            (no or yes).evaluate(feature, emptyMap()) shouldBe true
            (no or no).evaluate(feature, emptyMap()) shouldBe false
            OrStrategy(emptyList()).evaluate(feature, emptyMap()) shouldBe false
        }

        test("not inverts") {
            (!yes).evaluate(feature, emptyMap()) shouldBe false
            (!no).evaluate(feature, emptyMap()) shouldBe true
        }

        test("chaining flattens into one composite") {
            (yes and no and yes) shouldBe AndStrategy(listOf(yes, no, yes))
            (yes or no or yes) shouldBe OrStrategy(listOf(yes, no, yes))
        }
    })
