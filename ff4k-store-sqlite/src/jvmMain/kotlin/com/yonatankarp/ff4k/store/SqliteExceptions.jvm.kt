package com.yonatankarp.ff4k.store

import java.sql.SQLException

internal actual fun Throwable.isSqliteUniqueConstraintViolation(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is SQLException) {
            val message = current.message ?: ""
            if (message.contains("UNIQUE constraint failed", ignoreCase = true) ||
                message.contains("PRIMARY KEY constraint failed", ignoreCase = true)
            ) {
                return true
            }
        }
        current = current.cause
    }
    return false
}
