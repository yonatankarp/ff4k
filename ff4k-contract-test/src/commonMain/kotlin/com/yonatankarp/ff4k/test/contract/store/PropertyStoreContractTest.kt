@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreConcurrencyTests
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreCrudTests

/**
 * Abstract contract test for [PropertyStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all PropertyStore implementations
 * must pass. It implements all specific contract test interfaces:
 * - [PropertyStoreCrudTests]
 * - [PropertyStoreConcurrencyTests]
 *
 * To use this contract test, extend this class and implement the [createStore] method:
 *
 * ```kotlin
 * class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
 *     override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
 * }
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class PropertyStoreContractTest :
    PropertyStoreCrudTests,
    PropertyStoreConcurrencyTests
