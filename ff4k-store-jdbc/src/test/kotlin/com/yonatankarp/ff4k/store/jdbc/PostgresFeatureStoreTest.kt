package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.postgresql.PostgreSQLContainer

class PostgresFeatureStoreTest : FeatureStoreContractTest() {

    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(
            dataSource = postgres.toDataSource(),
            // Use limited parallelism to avoid excessive contention in concurrent tests
            ioDispatcher = Dispatchers.IO.limitedParallelism(1),
        ).also { it.clear() }

    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            start()
        }
    }
}

private fun PostgreSQLContainer.toDataSource(): DataSource =
    PGSimpleDataSource().apply {
        setUrl(jdbcUrl)
        user = username
        password = this@toDataSource.password
    }
