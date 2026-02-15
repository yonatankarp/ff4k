package com.yonatankarp.ff4k.store

import android.database.sqlite.SQLiteConstraintException

internal actual fun Throwable.isSqliteUniqueConstraintViolation(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is SQLiteConstraintException) {
            val message = current.message?.lowercase() ?: ""
            if ("unique constraint failed" in message || "primary key constraint" in message) {
                return true
            }
        }
        current = current.cause
    }
    return false
}
