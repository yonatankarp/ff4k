package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.config.FF4kConfiguration
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

internal class InMemoryFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore() = InMemoryFeatureStore()

    init {
        test("test store initialized with feature map contains features") {
            // Given
            val initialFeatures = mapOf(
                "feature-1" to Feature("feature-1", isEnabled = true),
                "feature-2" to Feature("feature-2", isEnabled = false),
                "feature-3" to Feature("feature-3", isEnabled = true),
            )

            // When
            val store = InMemoryFeatureStore(initialFeatures)

            // Then
            store.count() shouldBe 3
            ("feature-1" in store).shouldBeTrue()
            ("feature-2" in store).shouldBeTrue()
            ("feature-3" in store).shouldBeTrue()
            store["feature-1"].shouldNotBeNull().isEnabled.shouldBeTrue()
            store["feature-2"].shouldNotBeNull().isEnabled.shouldBeFalse()
            store["feature-3"].shouldNotBeNull().isEnabled.shouldBeTrue()
        }

        test("test store initialized with FF4kConfiguration contains features") {
            // Given
            val config = FF4kConfiguration(
                features = mapOf(
                    "config-feature-1" to Feature("config-feature-1", isEnabled = true),
                    "config-feature-2" to Feature("config-feature-2", isEnabled = false),
                ),
            )

            // When
            val store = InMemoryFeatureStore(config)

            // Then
            store.count() shouldBe 2
            ("config-feature-1" in store).shouldBeTrue()
            ("config-feature-2" in store).shouldBeTrue()
            store["config-feature-1"].shouldNotBeNull().isEnabled.shouldBeTrue()
            store["config-feature-2"].shouldNotBeNull().isEnabled.shouldBeFalse()
        }

        test("test store initialized with empty configuration is empty") {
            // Given
            val config = FF4kConfiguration()

            // When
            val store = InMemoryFeatureStore(config)

            // Then
            store.isEmpty().shouldBeTrue()
            store.count() shouldBe 0
        }

        test("test concurrent writes are not creating race condition") {
            // Given
            val store = InMemoryFeatureStore()

            // When
            coroutineScope {
                val jobs = (1..100).map { i ->
                    launch {
                        store += Feature("feature-$i", isEnabled = true)
                    }
                }
                jobs.joinAll()
            }

            // Then
            store.count() shouldBe 100
        }

        test("test concurrent reads and writes do not cause deadlock or corruption") {
            // Given
            val store = InMemoryFeatureStore()
            store += Feature("test", isEnabled = false)

            // When
            coroutineScope {
                val readJobs = (1..50).map {
                    launch {
                        repeat(100) {
                            store["test"]
                        }
                    }
                }

                val writeJobs = (1..50).map {
                    launch {
                        repeat(100) {
                            store.toggle("test")
                        }
                    }
                }

                (readJobs + writeJobs).joinAll()
            }

            // Then
            ("test" in store).shouldBeTrue()
            store["test"].shouldNotBeNull()
        }
    }
}
