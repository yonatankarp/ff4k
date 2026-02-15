package com.yonatankarp.ff4k.store

/**
 * Checks if this throwable represents a SQLite unique constraint violation.
 *
 * Platform-specific implementations check the exception message for
 * UNIQUE or PRIMARY KEY constraint failure indicators:
 * - JVM: Checks [java.sql.SQLException] message for constraint keywords
 * - Android: Checks [android.database.sqlite.SQLiteConstraintException] message
 * - Apple/iOS: Checks exception message for constraint keywords or extended error codes
 */
internal expect fun Throwable.isSqliteUniqueConstraintViolation(): Boolean
