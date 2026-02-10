package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.SqlDriver

/**
 * Android implementation stub for test driver creation.
 *
 * Android unit tests are currently not supported due to Robolectric's incompatibility
 * with Kotest. See https://github.com/yonatankarp/ff4k/issues/181 for details.
 *
 * The SQLite functionality is tested via JVM tests which cover the platform-agnostic
 * database logic.
 */
actual suspend fun createSqliteDriver(): SqlDriver = throw UnsupportedOperationException(
    "Android unit tests are not currently supported. See https://github.com/yonatankarp/ff4k/issues/181",
)
