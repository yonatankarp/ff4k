@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreConcurrencyTests
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreCrudTests
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreGroupTests
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStorePermissionTests

/**
 * Abstract contract test for [FeatureStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all FeatureStore implementations
 * must pass. It implements all specific contract test interfaces:
 * - [FeatureStoreCrudTests]
 * - [FeatureStoreGroupTests]
 * - [FeatureStorePermissionTests]
 * - [FeatureStoreConcurrencyTests]
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
abstract class FeatureStoreContractTest :
    FeatureStoreCrudTests,
    FeatureStoreGroupTests,
    FeatureStorePermissionTests,
    FeatureStoreConcurrencyTests
