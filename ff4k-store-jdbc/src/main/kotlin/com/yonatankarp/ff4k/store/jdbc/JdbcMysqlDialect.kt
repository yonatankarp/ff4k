package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.store.sql.BaseMysqlStatements
import com.yonatankarp.ff4k.store.sql.SqlDialect
import java.sql.SQLException

/**
 * JDBC MySQL dialect — extends [BaseMysqlStatements] with JDBC-specific
 * parameter markers (`?`) and unique constraint violation detection.
 */
data object JdbcMysqlDialect : BaseMysqlStatements(), SqlDialect {
    @Suppress("UNUSED_PARAMETER")
    override fun marker(index: Int): String = "?"

    override fun isUniqueConstraintViolation(e: SQLException): Boolean =
        e.errorCode == 1062
}
