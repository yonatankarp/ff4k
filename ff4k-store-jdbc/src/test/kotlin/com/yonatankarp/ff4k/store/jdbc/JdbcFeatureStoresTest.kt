package com.yonatankarp.ff4k.store.jdbc

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.sql.Connection
import java.sql.DatabaseMetaData
import javax.sql.DataSource

class JdbcFeatureStoresTest : FunSpec({

    test("jdbcFeatureStore throws UnsupportedDatabaseException for unsupported database") {
        // Given
        val metadata = mockk<DatabaseMetaData> {
            every { databaseProductName } returns "Vertica Database"
        }
        val connection = mockk<Connection>(relaxed = true) {
            every { metaData } returns metadata
        }
        val dataSource = mockk<DataSource> {
            every { getConnection() } returns connection
        }

        // When
        val exception = shouldThrow<UnsupportedDatabaseException> {
            jdbcFeatureStore(dataSource)
        }

        // Then
        exception.message shouldContain "Vertica Database"
        exception.message shouldContain "Unsupported database"
        exception.message shouldContain "PostgreSQL"
    }
})
