package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest

class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()
}
