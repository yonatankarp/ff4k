package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest

class InMemoryFeatureStoreTest : FeatureStoreContractTest(locksDuringUpdate = true) {
    override suspend fun createStore(): FeatureStore = InMemoryFeatureStore()
}
