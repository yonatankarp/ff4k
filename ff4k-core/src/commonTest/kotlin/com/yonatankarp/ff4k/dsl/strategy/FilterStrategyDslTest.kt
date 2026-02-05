package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.ClientFilterStrategy
import com.yonatankarp.ff4k.strategy.RegionFilterStrategy
import com.yonatankarp.ff4k.strategy.ServerFilterStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * @author Yonatan Karp-Rudin
 */
internal class FilterStrategyDslTest :
    FunSpec({

        test("clientFilterStrategy sets ClientFilterStrategy") {
            val feature = feature("test") {
                clientFilterStrategy {
                    +"client-a"
                    +"client-b"
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<ClientFilterStrategy>()
            feature.flippingStrategy.grantedClients shouldContainExactlyInAnyOrder listOf("client-a", "client-b")
        }

        test("serverFilterStrategy sets ServerFilterStrategy") {
            val feature = feature("test") {
                serverFilterStrategy {
                    +"server-1"
                    +"server-2"
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<ServerFilterStrategy>()
            feature.flippingStrategy.targetServers shouldContainExactlyInAnyOrder listOf("server-1", "server-2")
        }

        test("regionStrategy sets RegionFilterStrategy") {
            val feature = feature("test") {
                regionStrategy {
                    +"eu-central-1"
                    +"us-west-2"
                }
            }

            feature.flippingStrategy.shouldBeInstanceOf<RegionFilterStrategy>()
            feature.flippingStrategy.grantedRegions shouldContainExactlyInAnyOrder listOf("eu-central-1", "us-west-2")
        }
    })
