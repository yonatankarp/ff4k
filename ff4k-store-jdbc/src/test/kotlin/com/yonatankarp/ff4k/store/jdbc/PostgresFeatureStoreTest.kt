package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import kotlinx.coroutines.Dispatchers
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

class PostgresFeatureStoreTest : FeatureStoreContractTest() {

    init {
        afterSpec {
            postgres.close()
        }
    }

    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(
            dataSource = postgres.toDataSource(),
            ioDispatcher = Dispatchers.IO.limitedParallelism(1),
        ).also { it.clear() }

    companion object {
        private val postgres: PostgreSQLContainer by lazy {
            PostgreSQLContainer("postgres:14-alpine").apply {
                start()
            }
        }
    }
}

private fun PostgreSQLContainer.toDataSource(): DataSource =
    PGSimpleDataSource().apply {
        setUrl(this@toDataSource.jdbcUrl)
        user = this@toDataSource.username
        password = this@toDataSource.password
    }
