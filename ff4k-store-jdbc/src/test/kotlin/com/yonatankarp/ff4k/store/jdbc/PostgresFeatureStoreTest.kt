package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.postgresql.PostgreSQLContainer

class PostgresFeatureStoreTest : FeatureStoreContractTest() {

    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(postgres.toDataSource()).also { it.clear() }

    companion object {
        private val postgres = PostgreSQLContainer("postgres:9.5-alpine").apply {
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
