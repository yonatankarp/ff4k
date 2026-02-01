package com.yonatankarp.ff4k.dsl.core

import com.yonatankarp.ff4k.core.ContextKeys
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

/**
 * Tests for FlippingExecutionContextBuilder DSL.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FlippingExecutionContextBuilderTest :
    FunSpec({

        test("context function should create empty context when block is empty") {
            // When
            val ctx = context { }

            // Then
            ctx.isEmpty.shouldBeTrue()
        }

        test("context function should support infix to notation") {
            // When
            val ctx = context {
                ContextKeys.USER_ID to "user-123"
                ContextKeys.REGION to "EU"
            }

            // Then
            ctx.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
            ctx.get<String>(ContextKeys.REGION) shouldBe "EU"
        }

        test("context function should support indexed access notation") {
            // When
            val ctx = context {
                this[ContextKeys.USER_ID] = "user-456"
                this["tier"] = "premium"
            }

            // Then
            ctx.get<String>(ContextKeys.USER_ID) shouldBe "user-456"
            ctx.get<String>("tier") shouldBe "premium"
        }

        test("context function should support mixed notation") {
            // When
            val ctx = context {
                ContextKeys.USER_ID to "user-789"
                this[ContextKeys.REGION] = "US"
                "tier" to "enterprise"
            }

            // Then
            ctx.get<String>(ContextKeys.USER_ID) shouldBe "user-789"
            ctx.get<String>(ContextKeys.REGION) shouldBe "US"
            ctx.get<String>("tier") shouldBe "enterprise"
        }

        test("context function should support putAll with map") {
            // Given
            val params = mapOf(ContextKeys.USER_ID to "user-123", ContextKeys.REGION to "APAC")

            // When
            val ctx = context {
                putAll(params)
            }

            // Then
            ctx.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
            ctx.get<String>(ContextKeys.REGION) shouldBe "APAC"
        }

        test("context function should support putAll with vararg pairs") {
            // When
            val ctx = context {
                putAll(
                    Pair(ContextKeys.USER_ID, "user-123"),
                    Pair(ContextKeys.REGION, "EU"),
                    Pair("tier", "free"),
                )
            }

            // Then
            ctx.get<String>(ContextKeys.USER_ID) shouldBe "user-123"
            ctx.get<String>(ContextKeys.REGION) shouldBe "EU"
            ctx.get<String>("tier") shouldBe "free"
        }

        test("context function should support various value types") {
            // When
            val ctx = context {
                "string" to "text"
                "int" to 42
                "double" to 3.14
                "boolean" to true
                "list" to listOf(1, 2, 3)
            }

            // Then
            ctx.get<String>("string") shouldBe "text"
            ctx.get<Int>("int") shouldBe 42
            ctx.get<Double>("double") shouldBe 3.14
            ctx.get<Boolean>("boolean") shouldBe true
            ctx.get<List<Int>>("list") shouldBe listOf(1, 2, 3)
        }

        test("context function should override earlier values with same key") {
            // When
            val ctx = context {
                "tier" to "free"
                "tier" to "premium"
            }

            // Then
            ctx.get<String>("tier") shouldBe "premium"
        }

        test("builder should work with custom data classes") {
            // Given
            data class User(val id: Int, val name: String)
            val user = User(1, "Alice")

            // When
            val ctx = context {
                "user" to user
            }

            // Then
            ctx.get<User>("user") shouldBe user
        }

        test("builder set operator should override infix to value") {
            // When
            val ctx = context {
                "key" to "first"
                this["key"] = "second"
            }

            // Then
            ctx.get<String>("key") shouldBe "second"
        }

        test("putAll should merge with existing values") {
            // When
            val ctx = context {
                "existing" to "value"
                putAll(Pair("new1", "value1"), Pair("new2", "value2"))
            }

            // Then
            ctx.get<String>("existing") shouldBe "value"
            ctx.get<String>("new1") shouldBe "value1"
            ctx.get<String>("new2") shouldBe "value2"
        }
    })
