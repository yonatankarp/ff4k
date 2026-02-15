package com.yonatankarp.ff4k.store

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.sql.SQLException

class JvmSqliteExceptionsTest :
    FunSpec({

        context("isSqliteUniqueConstraintViolation") {
            test("should return false for non-SQLException") {
                // Given
                val exception = RuntimeException("Some error")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeFalse()
            }

            test("should return false for SQLException with null message") {
                // Given
                val exception = SQLException(null as String?)

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeFalse()
            }

            test("should return false for SQLException with non-matching message") {
                // Given
                val exception = SQLException("Some database error")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeFalse()
            }

            test("should return true for SQLException with UNIQUE constraint failed message") {
                // Given
                val exception = SQLException("[SQLITE_CONSTRAINT_UNIQUE] UNIQUE constraint failed: features.uid")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should return true for SQLException with PRIMARY KEY constraint failed message") {
                // Given
                val exception = SQLException("[SQLITE_CONSTRAINT_PRIMARYKEY] PRIMARY KEY constraint failed")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should be case insensitive for UNIQUE constraint") {
                // Given
                val exception = SQLException("unique constraint failed: some_table.column")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should be case insensitive for PRIMARY KEY constraint") {
                // Given
                val exception = SQLException("primary key constraint failed")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should return true when SQLException is nested in cause chain") {
                // Given
                val sqlException = SQLException("UNIQUE constraint failed: features.uid")
                val wrapper = RuntimeException("Wrapper exception", sqlException)

                // When
                val result = wrapper.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should return true when SQLException is deeply nested in cause chain") {
                // Given
                val sqlException = SQLException("PRIMARY KEY constraint failed")
                val innerWrapper = IllegalStateException("Inner wrapper", sqlException)
                val outerWrapper = RuntimeException("Outer wrapper", innerWrapper)

                // When
                val result = outerWrapper.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeTrue()
            }

            test("should return false when non-matching SQLException is in cause chain") {
                // Given
                val sqlException = SQLException("Connection refused")
                val wrapper = RuntimeException("Wrapper exception", sqlException)

                // When
                val result = wrapper.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeFalse()
            }

            test("should return false for empty cause chain") {
                // Given
                val exception = RuntimeException("No SQL exception in chain")

                // When
                val result = exception.isSqliteUniqueConstraintViolation()

                // Then
                result.shouldBeFalse()
            }
        }
    })
