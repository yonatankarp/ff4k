package com.yonatankarp.ff4k.store.sql

import java.sql.SQLException

/**
 * JDBC-specific extension of [SqlStatements] that adds exception inspection.
 *
 * Implementations provide [isUniqueConstraintViolation] to detect database-specific
 * unique constraint violations from [SQLException].
 */
interface SqlDialect : SqlStatements {
    /**
     * Checks if the given [SQLException] represents a unique constraint violation.
     *
     * Each database uses different SQL states and error codes to indicate unique
     * constraint violations. Implementations should check for their specific codes.
     *
     * @param e The SQLException to check.
     * @return `true` if this exception represents a unique constraint violation, `false` otherwise.
     */
    fun isUniqueConstraintViolation(e: SQLException): Boolean
}
