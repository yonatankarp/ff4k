package com.yonatankarp.ff4k.store.jdbc

import com.mysql.cj.jdbc.MysqlDataSource
import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.postgresql.ds.PGSimpleDataSource
import org.sqlite.SQLiteDataSource
import org.testcontainers.DockerClientFactory
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource
import kotlin.io.path.createTempFile
import kotlin.reflect.KClass

/** Fresh schema with empty tables. */
private suspend fun DataSource.reset(): DataSource = apply {
    JdbcSchema.create(this)
    withContext(Dispatchers.IO) {
        connection.use { c ->
            c.createStatement().use { s ->
                s.execute("DELETE FROM ${JdbcSchema.FEATURES_TABLE}")
                s.execute("DELETE FROM ${JdbcSchema.PROPERTIES_TABLE}")
            }
        }
    }
}

class DockerAvailable : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = DockerClientFactory.instance().isDockerAvailable
}

// ---- SQLite over JDBC: the Postgres upsert is valid SQLite, so this exercises the store without Docker.

private fun sqlite(): DataSource = SQLiteDataSource().apply { url = "jdbc:sqlite:${createTempFile(suffix = ".db")}" }

class SqliteJdbcFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = JdbcFeatureStore(sqlite().reset(), JdbcDialect.Postgres)
}

class SqliteJdbcPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = JdbcPropertyStore(sqlite().reset(), JdbcDialect.Postgres)
}

// Official images come from the ECR Public mirror so tests do not depend on Docker Hub availability or rate limits.
private fun mirrored(
    image: String,
    original: String,
): DockerImageName = DockerImageName.parse("public.ecr.aws/docker/library/$image").asCompatibleSubstituteFor(original)

// ---- PostgreSQL

// Result caches a failed start too, so one unavailable image fails every test at once instead of retrying per test.
private val postgres: Result<DataSource> by lazy {
    runCatching {
        val container = PostgreSQLContainer(mirrored("postgres:17-alpine", "postgres")).apply { start() }
        PGSimpleDataSource().apply {
            setUrl(container.jdbcUrl)
            user = container.username
            password = container.password
        }
    }
}

@EnabledIf(DockerAvailable::class)
class PostgresJdbcFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = JdbcFeatureStore(postgres.getOrThrow().reset(), JdbcDialect.Postgres)
}

@EnabledIf(DockerAvailable::class)
class PostgresJdbcPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = JdbcPropertyStore(postgres.getOrThrow().reset(), JdbcDialect.Postgres)
}

// ---- MySQL

private val mysql: Result<DataSource> by lazy {
    runCatching {
        val container = MySQLContainer(mirrored("mysql:8.4", "mysql")).apply { start() }
        MysqlDataSource().apply {
            setUrl(container.jdbcUrl)
            user = container.username
            password = container.password
        }
    }
}

@EnabledIf(DockerAvailable::class)
class MysqlJdbcFeatureStoreTest :
    FeatureStoreContractTest(body = {
        test("update does not overwrite a feature recreated with only a case change") {
            val store = JdbcFeatureStore(mysql.getOrThrow().reset(), JdbcDialect.Mysql)
            store.put(Feature("f", description = "original"))
            var first = true
            val updated = store.update("f") { feature ->
                if (first) {
                    first = false
                    runBlocking {
                        store.delete("f")
                        store.put(Feature("f", description = "ORIGINAL"))
                    }
                }
                feature.copy(enabled = true)
            }
            updated shouldBe Feature("f", enabled = true, description = "ORIGINAL")
        }
    }) {
    override suspend fun createStore(): FeatureStore = JdbcFeatureStore(mysql.getOrThrow().reset(), JdbcDialect.Mysql)
}

@EnabledIf(DockerAvailable::class)
class MysqlJdbcPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = JdbcPropertyStore(mysql.getOrThrow().reset(), JdbcDialect.Mysql)
}
