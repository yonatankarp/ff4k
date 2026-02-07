package com.yonatankarp.ff4k.store.sqldelight

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest

internal class SqlDelightFeatureStoreContractTest : FeatureStoreContractTest() {
    override suspend fun createStore(): SqlDelightFeatureStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FF4kDatabase.Schema.synchronous().create(driver)
        return SqlDelightFeatureStore(driver)
    }
}
