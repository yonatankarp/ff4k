package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.store.sql.TestDialect
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcFeatureStoreTest : FunSpec({

    context("update with transform") {
        test("throws FeatureNotFoundException when feature does not exist") {
            // Given
            val resultSet = mockk<ResultSet> {
                every { next() } returns false
                every { close() } returns Unit
            }
            val preparedStatement = mockk<PreparedStatement> {
                every { setString(any(), any()) } returns Unit
                every { executeQuery() } returns resultSet
                every { close() } returns Unit
            }
            val connection = mockk<Connection> {
                every { prepareStatement(any()) } returns preparedStatement
                every { close() } returns Unit
            }
            val dataSource = mockk<DataSource> {
                every { getConnection() } returns connection
            }
            val store = JdbcFeatureStore(dataSource, TestDialect)

            // When
            val exception = shouldThrow<FeatureNotFoundException> {
                store.update("non-existent-feature") { it }
            }

            // Then
            exception.message shouldContain "non-existent-feature"
        }

        test("throws IllegalStateException when transform changes feature uid") {
            // Given
            val resultSet = mockk<ResultSet> {
                every { next() } returns true
                every { getString("uid") } returns "original-uid"
                every { getLong("enabled") } returns 1L
                every { getString("group_name") } returns null
                every { getString("description") } returns null
                every { getString("permissions") } returns "[]"
                every { getString("flipping_strategy") } returns null
                every { getString("custom_properties") } returns "{}"
                every { getLong("version") } returns 1L
                every { close() } returns Unit
            }
            val preparedStatement = mockk<PreparedStatement> {
                every { setString(any(), any()) } returns Unit
                every { executeQuery() } returns resultSet
                every { close() } returns Unit
            }
            val connection = mockk<Connection> {
                every { prepareStatement(any()) } returns preparedStatement
                every { close() } returns Unit
            }
            val dataSource = mockk<DataSource> {
                every { getConnection() } returns connection
            }
            val store = JdbcFeatureStore(dataSource, TestDialect)

            // When
            val exception = shouldThrow<IllegalStateException> {
                store.update("original-uid") { Feature(uid = "changed-uid", isEnabled = true) }
            }

            // Then
            exception.message shouldContain "Cannot change feature uid during update"
            exception.message shouldContain "original-uid"
            exception.message shouldContain "changed-uid"
        }

        test("throws IllegalStateException after max retries due to concurrent modifications") {
            // Given
            val resultSet = mockk<ResultSet> {
                every { next() } returns true
                every { getString("uid") } returns "my-feature"
                every { getLong("enabled") } returns 0L
                every { getString("group_name") } returns null
                every { getString("description") } returns null
                every { getString("permissions") } returns "[]"
                every { getString("flipping_strategy") } returns null
                every { getString("custom_properties") } returns "{}"
                every { getLong("version") } returns 1L
                every { close() } returns Unit
            }
            val selectStatement = mockk<PreparedStatement> {
                every { setString(any(), any()) } returns Unit
                every { executeQuery() } returns resultSet
                every { close() } returns Unit
            }
            val updateStatement = mockk<PreparedStatement> {
                every { setString(any(), any()) } returns Unit
                every { setLong(any(), any()) } returns Unit
                every { executeUpdate() } returns 0 // Always returns 0 to simulate version conflict
                every { close() } returns Unit
            }
            val connection = mockk<Connection> {
                every { prepareStatement(TestDialect.selectFeatureByUidSql) } returns selectStatement
                every { prepareStatement(TestDialect.updateFeatureSql) } returns updateStatement
                every { autoCommit } returns true
                every { autoCommit = any() } returns Unit
                every { commit() } returns Unit
                every { rollback() } returns Unit
                every { close() } returns Unit
            }
            val dataSource = mockk<DataSource> {
                every { getConnection() } returns connection
            }
            val store = JdbcFeatureStore(dataSource, TestDialect)

            // When
            val exception = shouldThrow<IllegalStateException> {
                store.update("my-feature") { it.enable() }
            }

            // Then
            exception.message shouldContain "Failed to update feature"
            exception.message shouldContain "my-feature"
            exception.message shouldContain "10 retries"
            exception.message shouldContain "concurrent modifications"
        }
    }
})
