package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import com.yonatankarp.ff4k.strategy.UserPonderationStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

internal class StrategyDslTest :
    FunSpec({

        test("ponderationStrategy with Double sets strategy") {
            val feature = feature("test") {
                ponderationStrategy(0.75)
            }

            feature.flippingStrategy.shouldBeInstanceOf<PonderationStrategy>()
            (feature.flippingStrategy as PonderationStrategy).weight shouldBe 0.75
        }

        test("ponderationStrategy with Int sets strategy") {
            val feature = feature("test") {
                ponderationStrategy(50)
            }

            feature.flippingStrategy.shouldBeInstanceOf<PonderationStrategy>()
            (feature.flippingStrategy as PonderationStrategy).weight shouldBe 0.5
        }

        test("userPonderationStrategy with Double sets strategy") {
            val feature = feature("test") {
                userPonderationStrategy(0.75)
            }

            feature.flippingStrategy.shouldBeInstanceOf<UserPonderationStrategy>()
            (feature.flippingStrategy as UserPonderationStrategy).weight shouldBe 0.75
        }

        test("userPonderationStrategy with Int sets strategy") {
            val feature = feature("test") {
                userPonderationStrategy(50)
            }

            feature.flippingStrategy.shouldBeInstanceOf<UserPonderationStrategy>()
            (feature.flippingStrategy as UserPonderationStrategy).weight shouldBe 0.5
        }
    })
