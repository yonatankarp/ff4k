package com.yonatankarp.ff4k.store

import android.database.sqlite.SQLiteConstraintException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AndroidSqliteExceptionsTest {

    @Test
    fun `should return false for non-SQLiteConstraintException`() {
        // Given
        val exception = RuntimeException("Some error")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false for SQLiteConstraintException with null message`() {
        // Given
        val exception = SQLiteConstraintException(null)

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false for SQLiteConstraintException with non-matching message`() {
        // Given
        val exception = SQLiteConstraintException("Some database error")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true for SQLiteConstraintException with unique constraint failed message`() {
        // Given
        val exception = SQLiteConstraintException("UNIQUE constraint failed: features.uid")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return true for SQLiteConstraintException with primary key message`() {
        // Given
        val exception = SQLiteConstraintException("PRIMARY KEY constraint failed")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should be case insensitive for unique constraint`() {
        // Given
        val exception = SQLiteConstraintException("Unique Constraint Failed: some_table.column")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should be case insensitive for primary key constraint`() {
        // Given
        val exception = SQLiteConstraintException("Primary Key Constraint failed")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false for primary key without constraint keyword`() {
        // Given
        val exception = SQLiteConstraintException("Primary Key violation")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true when SQLiteConstraintException is nested in cause chain`() {
        // Given
        val sqliteException = SQLiteConstraintException("unique constraint failed: features.uid")
        val wrapper = RuntimeException("Wrapper exception", sqliteException)

        // When
        val result = wrapper.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return true when SQLiteConstraintException is deeply nested in cause chain`() {
        // Given
        val sqliteException = SQLiteConstraintException("primary key constraint")
        val innerWrapper = IllegalStateException("Inner wrapper", sqliteException)
        val outerWrapper = RuntimeException("Outer wrapper", innerWrapper)

        // When
        val result = outerWrapper.isSqliteUniqueConstraintViolation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when non-matching SQLiteConstraintException is in cause chain`() {
        // Given
        val sqliteException = SQLiteConstraintException("Foreign key constraint failed")
        val wrapper = RuntimeException("Wrapper exception", sqliteException)

        // When
        val result = wrapper.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false for empty cause chain`() {
        // Given
        val exception = RuntimeException("No SQLite exception in chain")

        // When
        val result = exception.isSqliteUniqueConstraintViolation()

        // Then
        assertFalse(result)
    }
}
