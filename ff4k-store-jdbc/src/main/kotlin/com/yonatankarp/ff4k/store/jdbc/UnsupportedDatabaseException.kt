package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.exception.FF4kConfigurationException

/**
 * Exception thrown when the database type is not supported.
 *
 * @param databaseProductName The name of the unsupported database product.
 * @param supported A collection of supported database names.
 */
class UnsupportedDatabaseException(
    databaseProductName: String,
    supported: Collection<String>,
) : FF4kConfigurationException(
    "Unsupported database: $databaseProductName. Supported databases: ${supported.joinToString()}",
)
