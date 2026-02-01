package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import com.yonatankarp.ff4k.strategy.UserPonderationStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

        context("allowListStrategy") {
            test("sets AllowListStrategy with unary plus operator") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        +"user-2"
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                (feature.flippingStrategy as AllowListStrategy).allowedList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2")
            }

            test("sets AllowListStrategy with add function") {
                val feature = feature("test") {
                    allowListStrategy {
                        add("user-1")
                        add("user-2")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                (feature.flippingStrategy as AllowListStrategy).allowedList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2")
            }

            test("sets AllowListStrategy with addAll function") {
                val feature = feature("test") {
                    allowListStrategy {
                        addAll("user-1", "user-2", "user-3")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                (feature.flippingStrategy as AllowListStrategy).allowedList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2", "user-3")
            }

            test("sets AllowListStrategy with mixed methods") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        add("user-2")
                        addAll("user-3", "user-4")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                (feature.flippingStrategy as AllowListStrategy).allowedList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2", "user-3", "user-4")
            }

            test("deduplicates entries") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        +"user-1"
                        add("user-1")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                (feature.flippingStrategy as AllowListStrategy).allowedList shouldBe setOf("user-1")
            }
        }

        context("denyListStrategy") {
            test("sets DenyListStrategy with unary plus operator") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        +"user-2"
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                (feature.flippingStrategy as DenyListStrategy).denyList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2")
            }

            test("sets DenyListStrategy with add function") {
                val feature = feature("test") {
                    denyListStrategy {
                        add("user-1")
                        add("user-2")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                (feature.flippingStrategy as DenyListStrategy).denyList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2")
            }

            test("sets DenyListStrategy with addAll function") {
                val feature = feature("test") {
                    denyListStrategy {
                        addAll("user-1", "user-2", "user-3")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                (feature.flippingStrategy as DenyListStrategy).denyList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2", "user-3")
            }

            test("sets DenyListStrategy with mixed methods") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        add("user-2")
                        addAll("user-3", "user-4")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                (feature.flippingStrategy as DenyListStrategy).denyList shouldContainExactlyInAnyOrder
                    listOf("user-1", "user-2", "user-3", "user-4")
            }

            test("deduplicates entries") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        +"user-1"
                        add("user-1")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                (feature.flippingStrategy as DenyListStrategy).denyList shouldBe setOf("user-1")
            }
        }
    })
