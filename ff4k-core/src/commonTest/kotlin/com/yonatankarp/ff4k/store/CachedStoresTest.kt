package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.enable
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class CachedFeatureStoreContractTest : FeatureStoreContractTest() {
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

        test("the last snapshot is served while the backend is down, and refreshed once it recovers") {
            val time = TestTimeSource()
            val backend = FlakyFeatureStore(Feature("f", enabled = true))
            val cached = backend.cached(10.seconds, time)
            cached.get("f")?.enabled shouldBe true

            backend.up = false
            time += 10.seconds
            cached.get("f")?.enabled shouldBe true
            cached.getAll().single().enabled shouldBe true

            backend.up = true
            backend.enabled = false
            cached.get("f")?.enabled shouldBe true // still inside the retry window
            time += 10.seconds
            cached.get("f")?.enabled shouldBe false
        }

        test("a write followed by an outage keeps the last snapshot") {
            val backend = FlakyFeatureStore(Feature("f", enabled = true))
            val cached = backend.cached(10.seconds, TestTimeSource())
            cached.get("f")?.enabled shouldBe true

            cached.put(Feature("other"))
            backend.up = false
            cached.get("f")?.enabled shouldBe true
        }

        test("a failed first load makes one attempt per ttl and fails every caller") {
            val time = TestTimeSource()
            val backend = FlakyFeatureStore(Feature("f", enabled = true)).apply { up = false }
            val cached = backend.cached(10.seconds, time)

            val results = coroutineScope { (1..20).map { async { runCatching { cached.get("f") } } }.awaitAll() }
            results.all { it.exceptionOrNull() is IllegalStateException } shouldBe true
            backend.attempts shouldBe 1

            backend.up = true
            shouldThrow<IllegalStateException> { cached.get("f") } // still inside the retry window
            backend.attempts shouldBe 1
            time += 10.seconds
            cached.get("f")?.enabled shouldBe true
            backend.attempts shouldBe 2
        }

        test("a cancelled load is not remembered as a failure") {
            val backend = FlakyFeatureStore(Feature("f", enabled = true)).apply { cancel = true }
            val cached = backend.cached(10.seconds, TestTimeSource())

            shouldThrow<CancellationException> { cached.get("f") }
            backend.cancel = false
            cached.get("f")?.enabled shouldBe true
        }
    })

private class FlakyFeatureStore(private val feature: Feature) : FeatureStore by InMemoryFeatureStore(listOf(feature)) {
    var up = true
    var cancel = false
    var enabled = feature.enabled
    var attempts = 0

    override suspend fun getAll(): List<Feature> {
        attempts++
        yield() // lets concurrent callers queue behind this load
        if (cancel) throw CancellationException("cancelled")
        check(up) { "connection refused" }
        return listOf(feature.copy(enabled = enabled))
    }
}
