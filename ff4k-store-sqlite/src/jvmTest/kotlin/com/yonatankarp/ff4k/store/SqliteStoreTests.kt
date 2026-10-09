package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest

private suspend fun inMemoryDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { SqliteDatabase.Schema.create(it).await() }

class SqliteFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = SqliteFeatureStore(inMemoryDriver())
}

class SqlitePropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = SqlitePropertyStore(inMemoryDriver())
}
