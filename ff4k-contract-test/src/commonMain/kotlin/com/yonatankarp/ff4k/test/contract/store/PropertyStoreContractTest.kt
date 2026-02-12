@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.test.contract.store.property.propertyStoreConcurrencyTests
import com.yonatankarp.ff4k.test.contract.store.property.propertyStoreCrudTests
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec

/**
 * Abstract contract test for [PropertyStore] implementations.
 *
 * This abstract class defines a comprehensive test suite that all PropertyStore implementations
 * must pass. It implements all specific contract test interfaces:
 * - [propertyStoreCrudTests]
 * - [propertyStoreConcurrencyTests]
 *
 * To use this contract test, extend this class and implement the [createStore] method:
 *
 * ```kotlin
 * class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
 *     override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
 * }
 * ```
 */
@Ignored
abstract class PropertyStoreContractTest(body: FunSpec.() -> Unit = {}) : FunSpec(body) {
    abstract suspend fun createStore(): PropertyStore

    init {
        propertyStoreCrudTests(::createStore)
        propertyStoreConcurrencyTests(::createStore)
    }
}
