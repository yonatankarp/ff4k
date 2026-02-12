package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.store.AbstractFeatureStore
import com.yonatankarp.ff4k.store.sql.SqlDialect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.modules.SerializersModule
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Internal JDBC-backed implementation of [FeatureStore][com.yonatankarp.ff4k.core.FeatureStore].
 */
internal class JdbcFeatureStore(
    private val dataSource: DataSource,
    private val dialect: SqlDialect,
    customSerializersModule: SerializersModule = SerializersModule {},
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractFeatureStore() {

    private val mapper = JdbcFeatureMapper(customSerializersModule)

    fun ensureSchema() {
        dataSource.transact { conn ->
            dialect.schemaSql.forEach { sql ->
                conn.createStatement().use { stmt -> stmt.execute(sql) }
            }
        }
    }

    override suspend fun contains(featureId: String): Boolean =
        withContext(dispatcher) {
            dataSource.query(dialect.featureExistsSql) {
                setString(1, featureId)
                executeQuery().use { it.next() && it.getBoolean(1) }
            }
        }

    override suspend fun plusAssign(feature: Feature) {
        require(feature.uid.isNotBlank()) { "featureId cannot be empty" }
        withContext(dispatcher) {
            try {
                dataSource.transact { conn ->
                    conn.insertFeature(feature)
                }
            } catch (e: SQLException) {
                if (e.isUniqueConstraintViolation()) {
                    throw FeatureAlreadyExistsException(feature.uid)
                }
                throw e
            }
        }
    }

    override suspend fun minusAssign(featureId: String) {
        require(featureId.isNotBlank()) { "featureId cannot be empty" }
        val rowsAffected = withContext(dispatcher) {
            dataSource.transact { conn ->
                conn.prepare(dialect.deleteFeatureByUidSql) {
                    setString(1, featureId)
                    executeUpdate()
                }
            }
        }
        if (rowsAffected == 0) {
            throw FeatureNotFoundException(featureId)
        }
    }

    override suspend fun update(feature: Feature) {
        update(feature.uid) { feature }
    }

    override suspend fun get(featureId: String): Feature? =
        withContext(dispatcher) {
            dataSource.query(dialect.selectFeatureByUidSql) {
                setString(1, featureId)
                executeQuery().use { rs ->
                    if (rs.next()) mapper.toDomain(mapper.readRow(rs)) else null
                }
            }
        }

    override suspend fun getAll(): Map<String, Feature> =
        withContext(dispatcher) {
            dataSource.query(dialect.selectAllFeaturesSql) {
                executeQuery().use { rs ->
                    generateSequence { if (rs.next()) mapper.toDomain(mapper.readRow(rs)) else null }
                        .associateBy { it.uid }
                }
            }
        }

    override suspend fun clear() {
        withContext(dispatcher) {
            dataSource.execute(dialect.deleteAllFeaturesSql)
        }
    }

    override suspend fun isEmpty(): Boolean = count() == 0

    override suspend fun count(): Int =
        withContext(dispatcher) {
            dataSource.query(dialect.countFeaturesSql) {
                executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }

    override suspend fun update(featureId: String, transform: (Feature) -> Feature) {
        repeat(MAX_RETRIES) {
            val updated = withContext(dispatcher) {
                val row = selectRow(featureId) ?: throw FeatureNotFoundException(featureId)
                val transformed = transform(mapper.toDomain(row))

                check(transformed.uid == featureId) {
                    "Cannot change feature uid during update. Expected: $featureId, got: ${transformed.uid}"
                }

                updateWithVersion(transformed, row.version)
            }
            if (updated > 0) return
        }
        error("Failed to update feature '$featureId' after $MAX_RETRIES retries due to concurrent modifications")
    }

    override suspend fun createOrUpdate(feature: Feature) {
        withContext(dispatcher) {
            dataSource.query(dialect.upsertFeatureSql) {
                bindFeature(feature)
                executeUpdate()
            }
        }
    }

    private fun selectRow(featureId: String): FeatureRow? =
        dataSource.query(dialect.selectFeatureByUidSql) {
            setString(1, featureId)
            executeQuery().use { rs -> if (rs.next()) mapper.readRow(rs) else null }
        }

    private fun Connection.insertFeature(feature: Feature) {
        prepare(dialect.insertFeatureSql) {
            bindFeature(feature)
            executeUpdate()
        }
    }

    private fun updateWithVersion(feature: Feature, expectedVersion: Long): Int =
        dataSource.transact { conn ->
            conn.prepare(dialect.updateFeatureSql) {
                setLong(1, mapper.encodeEnabled(feature.isEnabled))
                setString(2, feature.group)
                setString(3, feature.description)
                setString(4, mapper.encodePermissions(feature.permissions))
                setString(5, mapper.encodeStrategy(feature.flippingStrategy))
                setString(6, mapper.encodeProperties(feature.customProperties))
                setString(7, feature.uid)
                setLong(8, expectedVersion)
                executeUpdate()
            }
        }

    private fun PreparedStatement.bindFeature(feature: Feature) {
        setString(1, feature.uid)
        setLong(2, mapper.encodeEnabled(feature.isEnabled))
        setString(3, feature.group)
        setString(4, feature.description)
        setString(5, mapper.encodePermissions(feature.permissions))
        setString(6, mapper.encodeStrategy(feature.flippingStrategy))
        setString(7, mapper.encodeProperties(feature.customProperties))
    }

    companion object {
        private const val MAX_RETRIES = 10
    }
}

private inline fun <T> DataSource.query(sql: String, block: PreparedStatement.() -> T): T =
    connection.use { it.prepareStatement(sql).use(block) }

private inline fun <T> DataSource.transact(block: (Connection) -> T): T =
    connection.use { conn ->
        val originalAutoCommit = conn.autoCommit
        conn.autoCommit = false
        try {
            block(conn).also { conn.commit() }
        } catch (t: Throwable) {
            conn.rollback()
            throw t
        } finally {
            conn.autoCommit = originalAutoCommit
        }
    }

private fun DataSource.execute(sql: String) {
    connection.use { it.createStatement().use { stmt -> stmt.execute(sql) } }
}

private inline fun <T> Connection.prepare(sql: String, block: PreparedStatement.() -> T): T =
    prepareStatement(sql).use(block)

/**
 * Checks if this SQLException represents a unique constraint violation.
 * SQL state class "23" indicates integrity constraint violations:
 * - "23505" is PostgreSQL's unique violation
 * - "23000" is the general integrity constraint violation (MySQL, H2)
 */
private fun SQLException.isUniqueConstraintViolation(): Boolean =
    sqlState?.startsWith("23") == true
