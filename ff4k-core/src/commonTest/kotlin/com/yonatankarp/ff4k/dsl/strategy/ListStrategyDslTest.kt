package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * @author Yonatan Karp-Rudin
 */
internal class ListStrategyDslTest :
    FunSpec({

        test("allowListStrategy sets AllowListStrategy") {
            val feature = feature("test") {
                allowListStrategy {
                    +"user-1"
                    +"user-2"
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
            feature.flippingStrategy.allowedList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
        }

        test("denyListStrategy sets DenyListStrategy") {
            val feature = feature("test") {
                denyListStrategy {
                    +"user-1"
                    +"user-2"
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
            feature.flippingStrategy.denyList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
        }
    })
