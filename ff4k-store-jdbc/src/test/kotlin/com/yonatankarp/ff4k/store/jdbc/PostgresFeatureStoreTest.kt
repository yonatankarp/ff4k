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
            postgres?.stop()
            postgres = null
        }
    }

    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(
            dataSource = container().toDataSource(),
            ioDispatcher = Dispatchers.IO.limitedParallelism(1),
        ).also { it.clear() }

    companion object {
        @Volatile
        private var postgres: PostgreSQLContainer? = null

        fun container(): PostgreSQLContainer =
            postgres ?: synchronized(this) {
                postgres ?: PostgreSQLContainer("postgres:14-alpine")
                    .apply { start() }
                    .also { postgres = it }
            }
    }
}

private fun PostgreSQLContainer.toDataSource(): DataSource =
    PGSimpleDataSource().apply {
        setUrl(this@toDataSource.jdbcUrl)
        user = this@toDataSource.username
        password = this@toDataSource.password
    }
