package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yonatankarp.ff4k.FF4k
import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private suspend fun inMemoryDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { SqliteDatabase.Schema.create(it).await() }

class SqliteFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = SqliteFeatureStore(inMemoryDriver())
}

class SqlitePropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = SqlitePropertyStore(inMemoryDriver())
}

class SqliteUnreadableRowTest :
    FunSpec({
        test("an undecodable feature does not hide the others") {
            val driver = inMemoryDriver()
            driver.execute(null, """INSERT INTO features (id, data) VALUES ('unknown-type', '{"id":"unknown-type","strategy":{"type":"geoFence"}}')""", 0).await()
            val store = SqliteFeatureStore(driver)
            store.put(Feature("healthy", enabled = true))

            store.getAll().map { it.id } shouldBe listOf("healthy")
            FF4k(store.cached()).check("healthy") shouldBe true
        }

        test("an undecodable property does not hide the others") {
            val driver = inMemoryDriver()
            driver.execute(null, """INSERT INTO properties (name, data) VALUES ('unknown-type', '{"name":"unknown-type","type":"uuid","value":"x"}')""", 0).await()
            val store = SqlitePropertyStore(driver)
            store.put(Property("healthy", 1))

            store.getAll().map { it.name } shouldBe listOf("healthy")
            FF4k(properties = store.cached()).property<Int>("healthy") shouldBe 1
        }
    })
