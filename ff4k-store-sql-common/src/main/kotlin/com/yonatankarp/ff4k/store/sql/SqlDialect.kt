package com.yonatankarp.ff4k.store.sql

/**
 * Sealed interface defining SQL dialect-specific statements for feature store operations.
 *
 * Implementations provide database-specific SQL syntax for DDL and DML operations.
 * Type encoding/decoding is handled separately by the mapper using kotlinx.serialization.
 */
sealed interface SqlDialect {
    /** Human-readable database name (e.g., "PostgreSQL", "MySQL"). */
    val databaseName: String

    // DDL
    /** SQL statements to create the feature table and indexes. Each statement is executed separately. */
    val schemaSql: List<String>

    // Queries
    val selectAllFeaturesSql: String
    val selectFeatureByUidSql: String
    val featureExistsSql: String
    val countFeaturesSql: String

    // Mutations
    val insertFeatureSql: String
    val updateFeatureSql: String
    val upsertFeatureSql: String
    val deleteFeatureByUidSql: String
    val deleteAllFeaturesSql: String
}
