package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.store.sql.BasePostgresStatements
import com.yonatankarp.ff4k.store.sql.SqlDialect
import java.sql.SQLException

/**
 * JDBC PostgreSQL dialect — extends [BasePostgresStatements] with JDBC-specific
 * parameter markers (`?`) and unique constraint violation detection.
 */
data object JdbcPostgresDialect : BasePostgresStatements(), SqlDialect {
    @Suppress("UNUSED_PARAMETER")
    override fun marker(index: Int): String = "?"

    override fun isUniqueConstraintViolation(e: SQLException): Boolean =
        "23505" == e.sqlState
}
