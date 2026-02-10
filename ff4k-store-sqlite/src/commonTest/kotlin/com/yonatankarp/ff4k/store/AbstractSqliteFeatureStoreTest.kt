package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest

abstract class AbstractSqliteFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = SqliteFeatureStore(createSqliteDriver())
}
