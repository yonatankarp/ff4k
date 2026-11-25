package com.yonatankarp.ff4k.store.jdbc

/** The SQL that differs between databases. Implement it to support another database. */
interface JdbcDialect {
    /** Upsert into `table(id, data, version)` with two parameters: id and data. Bumps `version` on conflict. */
    fun upsert(table: String): String

    /** A condition that the `data` column equals one parameter, exactly (case- and accent-sensitive). */
    fun dataEquals(): String = "data = ?"

    object Postgres : JdbcDialect {
        override fun upsert(table: String): String = "INSERT INTO $table (id, data, version) VALUES (?, ?, 1) " +
            "ON CONFLICT (id) DO UPDATE SET data = EXCLUDED.data, version = $table.version + 1"
    }

    /** Requires MySQL 8.0.19 or later for the row alias syntax. */
    object Mysql : JdbcDialect {
        override fun upsert(table: String): String = "INSERT INTO $table (id, data, version) VALUES (?, ?, 1) AS new " +
            "ON DUPLICATE KEY UPDATE data = new.data, version = $table.version + 1"

        // the default TEXT collation ignores case and accents
        override fun dataEquals(): String = "data = CAST(? AS BINARY)"
    }
}
