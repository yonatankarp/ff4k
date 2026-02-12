package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.store.sql.SqlDialect

/**
 * Exception thrown when the database type is not supported.
 *
 * @param databaseProductName The name of the unsupported database product.
 */
class UnsupportedDatabaseException(databaseProductName: String) : RuntimeException(
    "Unsupported database: $databaseProductName. Supported: ${supportedDatabases()}",
)

private fun supportedDatabases(): String =
    SqlDialect::class.sealedSubclasses
        .mapNotNull { it.objectInstance?.databaseName }
        .joinToString()
