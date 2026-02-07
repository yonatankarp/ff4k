package com.yonatankarp.ff4k.store

import app.cash.sqldelight.db.SqlDriver

expect suspend fun createTestSqliteDriver(): SqlDriver
