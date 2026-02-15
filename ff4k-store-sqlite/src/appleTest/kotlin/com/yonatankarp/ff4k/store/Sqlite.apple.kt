package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.inMemoryDriver
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase

suspend fun createSqliteDriver(): SqlDriver {
    val schema = SqliteDatabase.Schema
    val synchronousSchema = object : SqlSchema<QueryResult.Value<Unit>> {
        override val version: Long = schema.version
        override fun create(driver: SqlDriver): QueryResult.Value<Unit> = QueryResult.Value(Unit)

        override fun migrate(driver: SqlDriver, oldVersion: Long, newVersion: Long, vararg callbacks: AfterVersion): QueryResult.Value<Unit> = QueryResult.Value(Unit)
    }

    val driver = inMemoryDriver(synchronousSchema)

    schema.create(driver).await()

    return driver
}
