package com.yonatankarp.ff4k.store

private const val SQLITE_CONSTRAINT_PRIMARYKEY = 1555
private const val SQLITE_CONSTRAINT_UNIQUE = 2067

internal actual fun Throwable.isSqliteUniqueConstraintViolation(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val message = current.message?.lowercase() ?: ""
        if ("unique constraint" in message ||
            "primary key constraint" in message ||
            "error code $SQLITE_CONSTRAINT_PRIMARYKEY" in message ||
            "error code $SQLITE_CONSTRAINT_UNIQUE" in message
        ) {
            return true
        }
        current = current.cause
    }
    return false
}
