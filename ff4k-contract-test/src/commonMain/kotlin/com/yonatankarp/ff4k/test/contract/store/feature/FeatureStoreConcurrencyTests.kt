@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.ANOTHER_GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.createFeature
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

internal fun FunSpec.featureStoreConcurrencyTests(createStore: suspend () -> FeatureStore) {
    test("concurrent enable and grant permissions should not lose changes") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        coroutineScope {
            val enableJobs = (1..50).map {
                launch {
                    store.enable(FEATURE_NAME)
                }
            }

            val grantJobs = (1..50).map { i ->
                launch {
                    store.grantRoleToFeature(FEATURE_NAME, "role-$i")
                }
            }

            (enableJobs + grantJobs).joinAll()
        }

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.isEnabled.shouldBeTrue()
        feature.permissions.size shouldBe 50
    }

    test("concurrent disable and grant permissions should not lose changes") {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        coroutineScope {
            val disableJobs = (1..50).map {
                launch {
                    store.disable(FEATURE_NAME)
                }
            }

            val grantJobs = (1..50).map { i ->
                launch {
                    store.grantRoleToFeature(FEATURE_NAME, "role-$i")
                }
            }

            (disableJobs + grantJobs).joinAll()
        }

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.isEnabled.shouldBeFalse()
        feature.permissions.size shouldBe 50
    }

    test("concurrent grant permissions and enable should not lose changes") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        coroutineScope {
            val grantJobs = (1..50).map { i ->
                launch {
                    store.grantRoleToFeature(FEATURE_NAME, "role-$i")
                }
            }

            val enableJobs = (1..50).map {
                launch {
                    store.enable(FEATURE_NAME)
                }
            }

            (grantJobs + enableJobs).joinAll()
        }

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.isEnabled.shouldBeTrue()
        feature.permissions.size shouldBe 50
    }

    test("concurrent addToGroup and enable should not lose changes") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        coroutineScope {
            val addGroupJobs = (1..50).map {
                launch {
                    store.addToGroup(FEATURE_NAME, GROUP_NAME)
                }
            }

            val enableJobs = (1..50).map {
                launch {
                    store.enable(FEATURE_NAME)
                }
            }

            (addGroupJobs + enableJobs).joinAll()
        }

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.isEnabled.shouldBeTrue()
        feature.group shouldBe GROUP_NAME
    }

    test("enableGroup should handle concurrent feature deletions gracefully") {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = false, group = GROUP_NAME)
        }

        // When
        coroutineScope {
            val enableJob = launch {
                store.enableGroup(GROUP_NAME)
            }

            val deleteJobs = (1..5).map { i ->
                launch {
                    store -= "feature-$i"
                }
            }

            (listOf(enableJob) + deleteJobs).joinAll()
        }

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            feature.shouldNotBeNull()
            feature.isEnabled.shouldBeTrue()
        }
        store.count() shouldBe 5
    }

    test("enableGroup should not enable features that left the group concurrently") {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = false, group = GROUP_NAME)
        }

        // When
        coroutineScope {
            val enableJob = launch {
                repeat(5) {
                    store.enableGroup(GROUP_NAME)
                }
            }

            val moveJobs = (1..5).map { i ->
                launch {
                    store.addToGroup("feature-$i", ANOTHER_GROUP_NAME)
                }
            }

            (listOf(enableJob) + moveJobs).joinAll()
        }

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            feature.shouldNotBeNull()
            feature.group shouldBe GROUP_NAME
            feature.isEnabled.shouldBeTrue()
        }
    }

    test("disableGroup should handle concurrent feature deletions gracefully") {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += Feature("feature-$i", isEnabled = true, group = GROUP_NAME)
        }

        // When
        coroutineScope {
            val disableJob = launch {
                store.disableGroup(GROUP_NAME)
            }

            val deleteJobs = (1..5).map { i ->
                launch {
                    store -= "feature-$i"
                }
            }

            (listOf(disableJob) + deleteJobs).joinAll()
        }

        // Then
        (6..10).forEach { i ->
            val feature = store["feature-$i"]
            feature.shouldNotBeNull()
            feature.isEnabled.shouldBeFalse()
        }
        store.count() shouldBe 5
    }
}
