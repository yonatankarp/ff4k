package com.yonatankarp.ff4k.store

import io.kotest.core.spec.style.FunSpec

class JvmSqliteUniqueConstraintTest :
    FunSpec({
        sqliteUniqueConstraintTests { SqliteFeatureStore(createSqliteDriver()) }
    })
