package com.yonatankarp.ff4k.store.sql

/**
 * Interface defining SQL statements for feature store operations.
 *
 * Implementations provide database-specific SQL syntax for DDL and DML operations.
 * Parameter placeholders are generated via [marker], allowing different placeholder
 * styles (e.g., `?` for JDBC, `$1` for R2DBC PostgreSQL).
 */
interface SqlStatements {
    /** Human-readable database name (e.g., "PostgreSQL", "MySQL"). */
    val databaseName: String

    /**
     * Returns the parameter placeholder for the given 1-based index.
     *
     * JDBC implementations typically return `"?"` for all indices, while
     * R2DBC PostgreSQL returns `"$1"`, `"$2"`, etc.
     */
    fun marker(index: Int): String

    // DDL
    /** SQL statements to create the feature table and indexes. Each statement is executed separately. */
    val schemaSql: List<String>

    // Queries

    /** SQL query that returns all feature rows. Takes no parameters. */
    val selectAllFeaturesSql: String

    /** SQL query that returns a single feature row by uid. Parameter 1: feature uid. */
    val selectFeatureByUidSql: String

    /** SQL query that returns a single boolean indicating whether a feature exists. Parameter 1: feature uid. */
    val featureExistsSql: String

    /** SQL query that returns a single integer with the total number of features. Takes no parameters. */
    val countFeaturesSql: String

    // Mutations

    /**
     * SQL statement for inserting a new feature.
     * Parameters are bound in order:
     * uid, enabled, group, description, permissions, flipping_strategy, custom_properties.
     */
    val insertFeatureSql: String

    /**
     * SQL statement for updating an existing feature with optimistic locking.
     * Parameters are bound in order:
     * enabled, group, description, permissions, flipping_strategy, custom_properties, uid, expected_version.
     * The statement should increment the version and only update the row matching both the uid and expected version.
     */
    val updateFeatureSql: String

    /**
     * SQL statement for inserting or updating a feature (upsert).
     *
     * Implementations should ensure that the underlying statement increments the
     * `version` field on both insert and update so that version information is
     * consistently maintained for optimistic-locking aware operations elsewhere
     * in the system.
     */
    val upsertFeatureSql: String

    /** SQL statement for deleting a single feature by uid. Parameter 1: feature uid. */
    val deleteFeatureByUidSql: String

    /** SQL statement for deleting all features. Takes no parameters. */
    val deleteAllFeaturesSql: String
}
