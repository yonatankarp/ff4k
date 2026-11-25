package com.yonatankarp.ff4k.store.jdbc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import javax.sql.DataSource

/** Creates the tables used by [JdbcFeatureStore] and [JdbcPropertyStore] when they do not exist. */
object JdbcSchema {
    const val FEATURES_TABLE = "ff4k_features"
    const val PROPERTIES_TABLE = "ff4k_properties"

    suspend fun create(dataSource: DataSource) {
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    for (table in listOf(FEATURES_TABLE, PROPERTIES_TABLE)) {
                        statement.execute(
                            "CREATE TABLE IF NOT EXISTS $table (" +
                                "id VARCHAR(255) PRIMARY KEY, data TEXT NOT NULL, version BIGINT NOT NULL DEFAULT 1)",
                        )
                    }
                }
            }
        }
    }
}

/** One JSON document per row in `table(id, data, version)`. */
internal class JdbcDocuments(
    private val dataSource: DataSource,
    private val table: String,
    private val dialect: JdbcDialect,
) {
    class Row(val data: String, val version: Long)

    suspend fun get(id: String): Row? = connection { c ->
        c.prepareStatement("SELECT data, version FROM $table WHERE id = ?").use { s ->
            s.setString(1, id)
            s.executeQuery().use { r -> if (r.next()) Row(r.getString(1), r.getLong(2)) else null }
        }
    }

    suspend fun getAll(): List<String> = connection { c ->
        c.prepareStatement("SELECT data FROM $table").use { s ->
            s.executeQuery().use { r -> buildList { while (r.next()) add(r.getString(1)) } }
        }
    }

    suspend fun upsert(id: String, data: String) {
        connection { c ->
            c.prepareStatement(dialect.upsert(table)).use { s ->
                s.setString(1, id)
                s.setString(2, data)
                s.executeUpdate()
            }
        }
    }

    /**
     * Writes [data] only if the row still has [expectedVersion] and [expectedData]; returns whether it did.
     * The data check catches a row deleted and recreated in between, which starts again at version 1.
     */
    suspend fun updateIfVersion(id: String, data: String, expectedVersion: Long, expectedData: String): Boolean = connection { c ->
        val sql = "UPDATE $table SET data = ?, version = ? WHERE id = ? AND version = ? AND ${dialect.dataEquals()}"
        c.prepareStatement(sql).use { s ->
            s.setString(1, data)
            s.setLong(2, expectedVersion + 1)
            s.setString(3, id)
            s.setLong(4, expectedVersion)
            s.setString(5, expectedData)
            s.executeUpdate() > 0
        }
    }

    suspend fun delete(id: String) {
        connection { c ->
            c.prepareStatement("DELETE FROM $table WHERE id = ?").use { s ->
                s.setString(1, id)
                s.executeUpdate()
            }
        }
    }

    private suspend fun <T> connection(block: (Connection) -> T): T = withContext(Dispatchers.IO) { dataSource.connection.use(block) }
}
