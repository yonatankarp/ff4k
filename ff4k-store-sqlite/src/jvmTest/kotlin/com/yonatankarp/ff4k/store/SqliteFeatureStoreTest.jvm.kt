package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest

class JvmSqliteFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = SqliteFeatureStore(createSqliteDriver())
}
