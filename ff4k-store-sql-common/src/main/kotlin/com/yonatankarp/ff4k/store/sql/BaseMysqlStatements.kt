package com.yonatankarp.ff4k.store.sql

/**
 * Abstract base class providing MySQL-specific SQL statements.
 *
 * SQL strings use [marker] for parameter placeholders, allowing subclasses to
 * provide the appropriate marker style (e.g., `?` for JDBC and R2DBC MySQL).
 *
 * Uses:
 * - VARCHAR(255) for uid (MySQL has length limits on indexed columns)
 * - INTEGER for boolean (0/1) for consistency with other SQL stores
 * - TEXT for other string columns
 * - `ON DUPLICATE KEY UPDATE` for upsert
 */
abstract class BaseMysqlStatements : SqlStatements {

    override val databaseName = "MySQL"

    override val schemaSql: List<String> = listOf(
        // language=sql
        """
            CREATE TABLE IF NOT EXISTS FF4K_FEATURES (
                uid VARCHAR(255) PRIMARY KEY NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 0,
                group_name TEXT,
                description TEXT,
                permissions TEXT NOT NULL DEFAULT ('[]'),
                flipping_strategy TEXT,
                custom_properties TEXT NOT NULL DEFAULT ('{}'),
                version INTEGER NOT NULL DEFAULT 1,
                INDEX ff4k_features_enabled_idx (enabled),
                INDEX ff4k_features_group_name_idx (group_name(255))
            )
        """.trimIndent(),
    )

    override val selectAllFeaturesSql: String by lazy {
        // language=sql
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES"
    }

    override val selectFeatureByUidSql: String by lazy {
        // language=sql
        "SELECT uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties, version FROM FF4K_FEATURES WHERE uid = ${marker(1)}"
    }

    override val featureExistsSql: String by lazy {
        // language=sql
        "SELECT EXISTS(SELECT 1 FROM FF4K_FEATURES WHERE uid = ${marker(1)})"
    }

    override val countFeaturesSql: String by lazy {
        // language=sql
        "SELECT COUNT(*) FROM FF4K_FEATURES"
    }

    override val insertFeatureSql: String by lazy {
        // language=sql
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (${marker(1)}, ${marker(2)}, ${marker(3)}, ${marker(4)}, ${marker(5)}, ${marker(6)}, ${marker(7)})
        """.trimIndent()
    }

    override val updateFeatureSql: String by lazy {
        // language=sql
        """
        UPDATE FF4K_FEATURES
        SET enabled = ${marker(1)}, group_name = ${marker(2)}, description = ${marker(3)}, permissions = ${marker(4)}, flipping_strategy = ${marker(5)}, custom_properties = ${marker(6)}, version = version + 1
        WHERE uid = ${marker(7)} AND version = ${marker(8)}
        """.trimIndent()
    }

    override val upsertFeatureSql: String by lazy {
        // language=sql
        """
        INSERT INTO FF4K_FEATURES (uid, enabled, group_name, description, permissions, flipping_strategy, custom_properties)
        VALUES (${marker(1)}, ${marker(2)}, ${marker(3)}, ${marker(4)}, ${marker(5)}, ${marker(6)}, ${marker(7)}) AS new_row
        ON DUPLICATE KEY UPDATE
            enabled = new_row.enabled,
            group_name = new_row.group_name,
            description = new_row.description,
            permissions = new_row.permissions,
            flipping_strategy = new_row.flipping_strategy,
            custom_properties = new_row.custom_properties,
            version = version + 1
        """.trimIndent()
    }

    override val deleteFeatureByUidSql: String by lazy {
        // language=sql
        "DELETE FROM FF4K_FEATURES WHERE uid = ${marker(1)}"
    }

    override val deleteAllFeaturesSql: String by lazy {
        // language=sql
        "DELETE FROM FF4K_FEATURES"
    }
}
