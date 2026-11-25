package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield

class OptimisticUpdateTest :
    FunSpec({
        test("retries until the conditional write lands, re-reading each time") {
            val snapshots = ArrayDeque(listOf(Feature("f", description = "a"), Feature("f", description = "b")))
            val written = mutableListOf<Feature>()

            val result = optimisticUpdate(
                id = "f",
                transform = { it.copy(enabled = true) },
                read = { snapshots.first() },
                decode = { it },
                write = { snapshot, updated ->
                    snapshots.removeFirst()
                    (snapshot.description == "b").also { if (it) written += updated }
                },
            )

            result shouldBe Feature("f", enabled = true, description = "b")
            written shouldBe listOf(result)
        }

        test("a write that never lands can still be cancelled") {
            coroutineScope {
                val looping = async { optimisticUpdate("f", { it }, read = { Feature("f") }, decode = { it }, write = { _, _ -> false }) }
                yield()
                looping.cancel()
                shouldThrow<CancellationException> { looping.await() }
            }
        }

        test("throws FeatureNotFoundException when there is nothing to read") {
            shouldThrow<FeatureNotFoundException> {
                optimisticUpdate<Feature>("f", { it }, read = { null }, decode = { it }, write = { _, _ -> true })
            }
        }

        test("rejects a transform that changes the id, without writing") {
            var writes = 0
            shouldThrow<IllegalArgumentException> {
                optimisticUpdate("f", { it.copy(id = "g") }, read = { Feature("f") }, decode = { it }, write = { _, _ -> writes++ == 0 })
            }
            writes shouldBe 0
        }
    })
