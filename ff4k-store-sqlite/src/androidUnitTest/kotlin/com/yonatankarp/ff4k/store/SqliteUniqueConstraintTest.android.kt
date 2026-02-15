package com.yonatankarp.ff4k.store

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AndroidSqliteUniqueConstraintTest {

    private fun createStore(): SqliteFeatureStore {
        val context: Context = ApplicationProvider.getApplicationContext()
        val schema = SqliteDatabase.Schema

        // Create a synchronous wrapper schema for AndroidSqliteDriver
        val synchronousSchema = object : SqlSchema<QueryResult.Value<Unit>> {
            override val version: Long = schema.version
            override fun create(driver: SqlDriver): QueryResult.Value<Unit> = QueryResult.Value(Unit)
            override fun migrate(
                driver: SqlDriver,
                oldVersion: Long,
                newVersion: Long,
                vararg callbacks: AfterVersion,
            ): QueryResult.Value<Unit> = QueryResult.Value(Unit)
        }

        val driver = AndroidSqliteDriver(synchronousSchema, context, null)

        // Create the actual schema asynchronously
        runBlocking {
            schema.create(driver).await()
        }

        return SqliteFeatureStore(driver)
    }

    @Test
    fun `should throw FeatureAlreadyExistsException when inserting duplicate feature`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "duplicate-test", isEnabled = true)
        store += feature

        // When / Then
        try {
            store += feature
            throw AssertionError("Expected FeatureAlreadyExistsException")
        } catch (e: FeatureAlreadyExistsException) {
            assertEquals("Feature already exists: duplicate-test", e.message)
        }
    }

    @Test
    fun `should preserve original exception as cause`() = runTest {
        // Given
        val store = createStore()
        val feature = Feature(uid = "cause-test", isEnabled = true)
        store += feature

        // When / Then
        try {
            store += feature
            throw AssertionError("Expected FeatureAlreadyExistsException")
        } catch (e: FeatureAlreadyExistsException) {
            assertNotNull(e.cause)
        }
    }

    @Test
    fun `should detect constraint violation with different feature content but same uid`() = runTest {
        // Given
        val store = createStore()
        val feature1 = Feature(
            uid = "same-uid",
            isEnabled = true,
            description = "First version",
        )
        val feature2 = Feature(
            uid = "same-uid",
            isEnabled = false,
            description = "Second version",
        )

        store += feature1

        // When / Then
        try {
            store += feature2
            throw AssertionError("Expected FeatureAlreadyExistsException")
        } catch (e: FeatureAlreadyExistsException) {
            // Expected
        }
    }

    @Test
    fun `should allow inserting features with different uids`() = runTest {
        // Given
        val store = createStore()
        val feature1 = Feature(uid = "feature-1", isEnabled = true)
        val feature2 = Feature(uid = "feature-2", isEnabled = true)

        // When
        store += feature1
        store += feature2

        // Then
        assertEquals(2, store.count())
    }
}
