package com.yonatankarp.ff4k.test.contract

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.disable
import com.yonatankarp.ff4k.disableGroup
import com.yonatankarp.ff4k.enable
import com.yonatankarp.ff4k.enableGroup
import com.yonatankarp.ff4k.group
import com.yonatankarp.ff4k.strategy.ContextFilterStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Behaviour every [FeatureStore] implementation must satisfy. Subclass and implement [createStore].
 * Pass [locksDuringUpdate] for stores that hold a lock while the update transform runs, so the test that calls the
 * store from inside the transform is left out instead of deadlocking.
 */
@Ignored
abstract class FeatureStoreContractTest(
    locksDuringUpdate: Boolean = false,
    body: FunSpec.() -> Unit = {},
) : FunSpec(body) {
    abstract suspend fun createStore(): FeatureStore

    init {
        test("get returns null for an unknown feature") {
            createStore().get("missing").shouldBeNull()
        }

        test("put then get round-trips every field") {
            val store = createStore()
            val feature = Feature(
                id = "f",
                enabled = true,
                description = "desc",
                group = "g",
                permissions = setOf("admin"),
                strategy = ContextFilterStrategy("region", setOf("eu")),
                properties = listOf(Property("limit", 3)),
            )
            store.put(feature)
            store.get("f") shouldBe feature
        }

        test("put replaces an existing feature") {
            val store = createStore()
            store.put(Feature("f", enabled = false))
            store.put(Feature("f", enabled = true))
            store.get("f")?.enabled shouldBe true
            store.getAll().size shouldBe 1
        }

        test("getAll returns every feature") {
            val store = createStore()
            store.put(Feature("a"))
            store.put(Feature("b"))
            store.getAll().map { it.id } shouldContainExactlyInAnyOrder listOf("a", "b")
        }

        test("update applies the transform and returns the result") {
            val store = createStore()
            store.put(Feature("f"))
            val updated = store.update("f") { it.copy(description = "changed") }
            updated.description shouldBe "changed"
            store.get("f") shouldBe updated
        }

        test("update throws for an unknown feature") {
            shouldThrow<FeatureNotFoundException> { createStore().update("missing") { it } }
        }

        test("update rejects changing the id") {
            val store = createStore()
            store.put(Feature("f"))
            shouldThrow<IllegalArgumentException> { store.update("f") { it.copy(id = "other") } }
        }

        if (!locksDuringUpdate) {
            test("update does not overwrite a feature recreated while it ran") {
                val store = createStore()
                store.put(Feature("f", description = "original"))
                var first = true
                val updated = store.update("f") { feature ->
                    if (first) {
                        first = false
                        runBlocking {
                            store.delete("f")
                            store.put(Feature("f", description = "recreated"))
                        }
                    }
                    feature.copy(enabled = true)
                }
                val expected = Feature("f", enabled = true, description = "recreated")
                updated shouldBe expected
                store.get("f") shouldBe expected
            }
        }

        test("delete removes the feature and is a no-op when missing") {
            val store = createStore()
            store.put(Feature("f"))
            store.delete("f")
            store.delete("f")
            store.get("f").shouldBeNull()
        }

        test("enable and disable toggle the flag") {
            val store = createStore()
            store.put(Feature("f"))
            store.enable("f").enabled shouldBe true
            store.disable("f").enabled shouldBe false
        }

        test("group operations only touch members of the group") {
            val store = createStore()
            store.put(Feature("a", group = "g"))
            store.put(Feature("b", group = "g"))
            store.put(Feature("c", group = "other"))
            store.group("g").map { it.id } shouldContainExactlyInAnyOrder listOf("a", "b")
            store.enableGroup("g")
            store.getAll().filter { it.enabled }.map { it.id } shouldContainExactlyInAnyOrder listOf("a", "b")
            store.disableGroup("g")
            store.getAll().none { it.enabled } shouldBe true
        }

        test("concurrent updates do not lose writes") {
            val store = createStore()
            store.put(Feature("f"))
            coroutineScope {
                (1..50).map { i ->
                    launch { store.update("f") { it.copy(enabled = true, permissions = it.permissions + "role-$i") } }
                }.joinAll()
            }
            val feature = store.get("f")!!
            feature.enabled shouldBe true
            feature.permissions.size shouldBe 50
        }
    }
}
