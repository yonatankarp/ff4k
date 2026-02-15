package com.yonatankarp.ff4k.store.sql

import java.sql.SQLException

/**
 * Test dialect for unit testing JDBC feature store logic.
 * Uses generic SQL that works for mocking purposes.
 */
internal data object TestDialect : SqlDialect {

    override val databaseName = "Test"

    override val schemaSql: List<String> = listOf(
        // language=SQL
        """
            CREATE TABLE IF NOT EXISTS FF4K_FEATURES (
                uid TEXT PRIMARY KEY NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 0,
                group_name TEXT,
                description TEXT,
                permissions TEXT NOT NULL DEFAULT '[]',
                flipping_strategy TEXT,
                custom_properties TEXT NOT NULL DEFAULT '{}',
                version INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent(),
    )

    // language=SQL
    override val selectAllFeaturesSql: String =
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES"

    // language=SQL
    override val selectFeatureByUidSql: String =
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES WHERE uid = ?"

    // language=SQL
    override val featureExistsSql: String =
        "SELECT EXISTS(SELECT 1 FROM FF4K_FEATURES WHERE uid = ?)"

    // language=SQL
    override val countFeaturesSql: String =
        "SELECT COUNT(*) FROM FF4K_FEATURES"

    // language=SQL
    override val insertFeatureSql: String =
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

    // language=SQL
    override val updateFeatureSql: String =
        """
        UPDATE FF4K_FEATURES
        SET enabled = ?, group_name = ?, description = ?, permissions = ?, flipping_strategy = ?, custom_properties = ?, version = version + 1
        WHERE uid = ? AND version = ?
        """.trimIndent()

    // language=SQL
    override val upsertFeatureSql: String =
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (uid) DO UPDATE SET
            enabled = EXCLUDED.enabled,
            group_name = EXCLUDED.group_name,
            description = EXCLUDED.description,
            permissions = EXCLUDED.permissions,
            flipping_strategy = EXCLUDED.flipping_strategy,
            custom_properties = EXCLUDED.custom_properties,
            version = FF4K_FEATURES.version + 1
        """.trimIndent()

    // language=SQL
    override val deleteFeatureByUidSql: String =
        "DELETE FROM FF4K_FEATURES WHERE uid = ?"

    // language=SQL
    override val deleteAllFeaturesSql: String =
        "DELETE FROM FF4K_FEATURES"

    override fun isUniqueConstraintViolation(e: SQLException): Boolean =
        e.sqlState?.startsWith("23") == true
}
