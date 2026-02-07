package com.yonatankarp.ff4k.store.sqldelight

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest

internal class SqlDelightFeatureStoreContractTestApple : FeatureStoreContractTest() {
    override suspend fun createStore(): SqlDelightFeatureStore {
        val driver = NativeSqliteDriver(
            schema = FF4kDatabase.Schema.synchronous(),
            name = ":memory:",
        )
        return SqlDelightFeatureStore(driver)
    }
}
