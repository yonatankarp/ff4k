package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.test.contract.store.PropertyStoreContractTest

class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
}
