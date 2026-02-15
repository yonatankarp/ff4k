package com.yonatankarp.ff4k.store.sql

import java.sql.SQLException

/**
 * Interface defining SQL dialect-specific statements for feature store operations.
 *
 * Implementations provide database-specific SQL syntax for DDL and DML operations.
 * Type encoding/decoding is handled separately by the mapper using kotlinx.serialization.
 */
interface SqlDialect {
    /** Human-readable database name (e.g., "PostgreSQL", "MySQL"). */
    val databaseName: String

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
     * Parameters are bound via [PreparedStatement][java.sql.PreparedStatement] in order:
     * uid, enabled, group, description, permissions, flipping_strategy, custom_properties.
     */
    val insertFeatureSql: String

    /**
     * SQL statement for updating an existing feature with optimistic locking.
     * Parameters are bound via [PreparedStatement][java.sql.PreparedStatement] in order:
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
     * in the system. The `FeatureStore.createOrUpdate()` operation itself uses
     * last-writer-wins semantics and does not rely on optimistic locking.
     */
    val upsertFeatureSql: String

    /** SQL statement for deleting a single feature by uid. Parameter 1: feature uid. */
    val deleteFeatureByUidSql: String

    /** SQL statement for deleting all features. Takes no parameters. */
    val deleteAllFeaturesSql: String

    /**
     * Checks if the given [SQLException] represents a unique constraint violation.
     *
     * Each database uses different SQL states and error codes to indicate unique
     * constraint violations. Implementations should check for their specific codes.
     *
     * @param e The SQLException to check.
     * @return `true` if this exception represents a unique constraint violation, `false` otherwise.
     */
    fun isUniqueConstraintViolation(e: SQLException): Boolean
}
