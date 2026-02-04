package com.yonatankarp.ff4k.dsl.strategy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

internal class ListBuilderTest :
    FunSpec({

        test("unary plus operator adds identifiers") {
            val result = ListBuilder().apply {
                +"id-1"
                +"id-2"
            }.build()

            result shouldContainExactlyInAnyOrder listOf("id-1", "id-2")
        }

        test("add function adds identifier") {
            val result = ListBuilder().apply {
                add("id-1")
                add("id-2")
            }.build()

            result shouldContainExactlyInAnyOrder listOf("id-1", "id-2")
        }

        test("addAll function adds multiple identifiers") {
            val result = ListBuilder().apply {
                addAll("id-1", "id-2", "id-3")
            }.build()

            result shouldContainExactlyInAnyOrder listOf("id-1", "id-2", "id-3")
        }

        test("mixed methods work together") {
            val result = ListBuilder().apply {
                +"id-1"
                add("id-2")
                addAll("id-3", "id-4")
            }.build()

            result shouldContainExactlyInAnyOrder listOf("id-1", "id-2", "id-3", "id-4")
        }

        test("deduplicates entries") {
            val result = ListBuilder().apply {
                +"id-1"
                +"id-1"
                add("id-1")
            }.build()

            result shouldBe setOf("id-1")
        }

        test("empty builder produces empty set") {
            val result = ListBuilder().build()

            result shouldBe emptySet()
        }
    })
