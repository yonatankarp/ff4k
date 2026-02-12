package com.yonatankarp.ff4k.store.sql

/**
 * PostgreSQL-specific SQL dialect for feature store operations.
 *
 * Uses:
 * - TEXT types for string columns
 * - INTEGER for boolean (0/1) for consistency with other SQL stores
 * - `ON CONFLICT ... DO UPDATE` for upsert
 */
data object PostgresDialect : SqlDialect {

    override val databaseName = "PostgreSQL"

    override val schemaSql: List<String> = listOf(
        // language=sql
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
        // language=sql
        "CREATE INDEX IF NOT EXISTS ff4k_features_enabled_idx ON FF4K_FEATURES (enabled)",
        // language=sql
        "CREATE INDEX IF NOT EXISTS ff4k_features_group_name_idx ON FF4K_FEATURES (group_name)",
    )

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

    // Note: Uses last-writer-wins semantics without optimistic locking (no version check).
    // This is intentional for createOrUpdate which doesn't accept an expected version.
    override val upsertFeatureSql: String =
    // language=sql
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

    override val deleteFeatureByUidSql: String =
        // language=sql
        "DELETE FROM FF4K_FEATURES WHERE uid = ?"

    override val deleteAllFeaturesSql: String =
        // language=sql
        "DELETE FROM FF4K_FEATURES"
}
