@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.feature.featureStoreConcurrencyTests
import com.yonatankarp.ff4k.test.contract.store.feature.featureStoreCrudTests
import com.yonatankarp.ff4k.test.contract.store.feature.featureStoreGroupTests
import com.yonatankarp.ff4k.test.contract.store.feature.featureStorePermissionTests
import io.kotest.core.spec.style.FunSpec

/**
 * Abstract contract test for [FeatureStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all FeatureStore implementations
 * must pass. It implements all specific contract test interfaces:
 * - [featureStoreCrudTests]
 * - [featureStoreGroupTests]
 * - [featureStorePermissionTests]
 * - [featureStoreConcurrencyTests]
 *
 * To use this contract test, extend this class and implement the [createStore] method:
 *
 * ```kotlin
 * class InMemoryFeatureStoreTest : FeatureStoreContractTest() {
 *     override suspend fun createStore(): FeatureStore = InMemoryFeatureStore()
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class FeatureStoreContractTest : FunSpec() {
    abstract suspend fun createStore(): FeatureStore

    init {
        featureStoreCrudTests(::createStore)
        featureStoreGroupTests(::createStore)
        featureStorePermissionTests(::createStore)
        featureStoreConcurrencyTests(::createStore)
    }
}
