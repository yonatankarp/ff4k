package com.yonatankarp.ff4k.store

import io.kotest.core.spec.style.FunSpec

class AppleSqliteUniqueConstraintTest :
    FunSpec({
        sqliteUniqueConstraintTests { SqliteFeatureStore(createSqliteDriver()) }
    })
