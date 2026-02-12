package com.yonatankarp.ff4k.store.jdbc

import com.mysql.cj.jdbc.MysqlDataSource
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import javax.sql.DataSource
import org.testcontainers.mysql.MySQLContainer

class MysqlFeatureStoreTest : FeatureStoreContractTest() {

    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(mysql.toDataSource()).also { it.clear() }

    companion object {
        private val mysql = MySQLContainer("mysql:8.0.19").apply {
            start()
        }
    }
}

private fun MySQLContainer.toDataSource(): DataSource =
    MysqlDataSource().apply {
        setUrl(jdbcUrl)
        user = username
        password = this@toDataSource.password
    }