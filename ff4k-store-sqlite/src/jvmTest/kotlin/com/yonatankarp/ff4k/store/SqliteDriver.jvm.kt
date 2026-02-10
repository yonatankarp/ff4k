package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase

actual suspend fun createSqliteDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    SqliteDatabase.Schema.create(driver).await()
    return driver
}
