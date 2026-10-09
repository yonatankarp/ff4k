package com.yonatankarp.ff4k.test.contract

import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Behaviour every [PropertyStore] implementation must satisfy. Subclass and implement [createStore]. */
@Ignored
abstract class PropertyStoreContractTest(body: FunSpec.() -> Unit = {}) : FunSpec(body) {
    abstract suspend fun createStore(): PropertyStore

    init {
        test("get returns null for an unknown property") {
            createStore().get("missing").shouldBeNull()
        }

        test("put then get preserves the value type") {
            val store = createStore()
            val properties = listOf(
                Property("string", "text", "desc"),
                Property("int", 1),
                Property("long", 2L),
                Property("double", 1.5),
                Property("boolean", true),
                Property("instant", Instant.parse("2026-01-01T00:00:00Z")),
                Property("date", LocalDate(2026, 1, 1)),
            )
            properties.forEach { store.put(it) }
            properties.forEach { store.get(it.name) shouldBe it }
        }

        test("put replaces an existing property") {
            val store = createStore()
            store.put(Property("p", 1))
            store.put(Property("p", 2))
            store.get("p")?.value shouldBe 2
            store.getAll().size shouldBe 1
        }

        test("getAll returns every property") {
            val store = createStore()
            store.put(Property("a", 1))
            store.put(Property("b", 2))
            store.getAll().map { it.name } shouldContainExactlyInAnyOrder listOf("a", "b")
        }

        test("delete removes the property and is a no-op when missing") {
            val store = createStore()
            store.put(Property("p", 1))
            store.delete("p")
            store.delete("p")
            store.get("p").shouldBeNull()
        }
    }
}
