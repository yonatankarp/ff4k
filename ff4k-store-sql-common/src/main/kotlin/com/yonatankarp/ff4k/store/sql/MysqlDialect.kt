package com.yonatankarp.ff4k.store.sql

/**
 * MySQL-specific SQL dialect for feature store operations.
 *
 * Uses:
 * - VARCHAR(255) for uid (MySQL has length limits on indexed columns)
 * - INTEGER for boolean (0/1) for consistency with other SQL stores
 * - TEXT for other string columns
 * - `ON DUPLICATE KEY UPDATE` for upsert
 */
data object MysqlDialect : SqlDialect {

    override val databaseName = "MySQL"

    override val createFeatureTableSql: String =
        // language=sql
        """
        CREATE TABLE IF NOT EXISTS FF4K_FEATURES (
            uid VARCHAR(255) PRIMARY KEY NOT NULL,
            enabled INTEGER NOT NULL DEFAULT 0,
            group_name TEXT,
            description TEXT,
            permissions TEXT NOT NULL,
            flipping_strategy TEXT,
            custom_properties TEXT NOT NULL,
            version INTEGER NOT NULL DEFAULT 1
        )
        """.trimIndent()

    override val selectAllFeaturesSql: String =
        // language=sql
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES"

    override val selectFeatureByUidSql: String =
        // language=sql
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES WHERE uid = ?"

    override val featureExistsSql: String =
        // language=sql
        "SELECT EXISTS(SELECT 1 FROM FF4K_FEATURES WHERE uid = ?)"

    override val countFeaturesSql: String =
        // language=sql
        "SELECT COUNT(*) FROM FF4K_FEATURES"

    override val insertFeatureSql: String =
        // language=sql
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

    override val updateFeatureSql: String =
        // language=sql
        """
        UPDATE FF4K_FEATURES
        SET enabled = ?, group_name = ?, description = ?, permissions = ?, flipping_strategy = ?, custom_properties = ?, version = version + 1
        WHERE uid = ? AND version = ?
        """.trimIndent()

    override val upsertFeatureSql: String =
        // language=sql
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            enabled = VALUES(enabled),
            group_name = VALUES(group_name),
            description = VALUES(description),
            permissions = VALUES(permissions),
            flipping_strategy = VALUES(flipping_strategy),
            custom_properties = VALUES(custom_properties),
            version = version + 1
        """.trimIndent()

    override val deleteFeatureByUidSql: String =
        // language=sql
        "DELETE FROM FF4K_FEATURES WHERE uid = ?"

    override val deleteAllFeaturesSql: String =
        // language=sql
        "DELETE FROM FF4K_FEATURES"
}