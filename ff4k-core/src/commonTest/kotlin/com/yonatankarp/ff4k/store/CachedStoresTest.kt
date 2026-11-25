package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.enable
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class CachedFeatureStoreContractTest : FeatureStoreContractTest(locksDuringUpdate = true) {
    override suspend fun createStore(): FeatureStore = InMemoryFeatureStore().cached()
}

class CachedPropertyStoreContractTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore().cached()
}

class CachedStoresTest :
    FunSpec({
        test("reads are served from the snapshot until the ttl passes") {
            val time = TestTimeSource()
            val backend = InMemoryFeatureStore()
            val cached = backend.cached(10.seconds, time)
            backend.put(Feature("f"))
            cached.get("f")?.enabled shouldBe false

            backend.enable("f") // behind the cache's back
            cached.get("f")?.enabled shouldBe false
            time += 10.seconds
            cached.get("f")?.enabled shouldBe true
        }

        test("writes through the cache are visible immediately") {
            val backend = InMemoryFeatureStore()
            val cached = backend.cached(1.seconds)
            cached.getAll() shouldBe emptyList()
            cached.put(Feature("f"))
            cached.get("f")?.id shouldBe "f"
            cached.enable("f").enabled shouldBe true
            cached.delete("f")
            cached.get("f").shouldBeNull()
        }

        test("property cache refreshes after the ttl") {
            val time = TestTimeSource()
            val backend = InMemoryPropertyStore()
            val cached = backend.cached(10.seconds, time)
            cached.get("p").shouldBeNull()
            backend.put(Property("p", 1))
            cached.get("p").shouldBeNull()
            time += 10.seconds
            cached.get("p")?.value shouldBe 1
            cached.put(Property("p", 2))
            cached.getAll().single().value shouldBe 2
        }
    })
