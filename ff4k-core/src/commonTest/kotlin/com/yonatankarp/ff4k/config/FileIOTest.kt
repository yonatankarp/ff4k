package com.yonatankarp.ff4k.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.random.Random

internal class FileIOTest :
    FunSpec({

        suspend fun <T> withTempFile(
            path: String = createTempFilePath(),
            block: suspend (String) -> T,
        ): T = try {
            block(path)
        } finally {
            runCatching {
                deleteTempFile(path)
            }
        }

        test("readFileContent should throw exception for non-existent file") {
            // Given
            val nonExistentPath = "/this/path/does/not/exist/file.txt"

            // When & Then
            shouldThrow<IllegalArgumentException> {
                readFileContent(nonExistentPath)
            }
        }

        test("writeFileContent and readFileContent should round-trip content") {
            withTempFile { tempFile ->
                // Given
                val testContent = "Hello, FF4K!\nThis is a test file."

                // When
                writeFileContent(tempFile, testContent)
                val readContent = readFileContent(tempFile)

                // Then
                readContent shouldBe testContent
            }
        }

        test("writeFileContent should overwrite existing file") {
            withTempFile { tempFile ->
                // Given
                val initialContent = "Initial content"
                val updatedContent = "Updated content"
                writeFileContent(tempFile, initialContent)

                // When
                writeFileContent(tempFile, updatedContent)
                val readContent = readFileContent(tempFile)

                // Then
                readContent shouldBe updatedContent
            }
        }

        test("writeFileContent and readFileContent should handle empty content") {
            withTempFile { tempFile ->
                // When
                writeFileContent(tempFile, "")
                val readContent = readFileContent(tempFile)

                // Then
                readContent shouldBe ""
            }
        }

        test("writeFileContent and readFileContent should handle unicode content") {
            withTempFile { tempFile ->
                // Given
                val unicodeContent = "Hello 世界! Привет мир! 🎉"

                // When
                writeFileContent(tempFile, unicodeContent)
                val readContent = readFileContent(tempFile)

                // Then
                readContent shouldBe unicodeContent
            }
        }

        test("writeFileContent and readFileContent should handle multiline content") {
            withTempFile { tempFile ->
                // Given
                val multilineContent = """
                Line 1
                Line 2
                Line 3

                Line after empty line
                """.trimIndent()

                // When
                writeFileContent(tempFile, multilineContent)
                val readContent = readFileContent(tempFile)

                // Then
                readContent shouldBe multilineContent
            }
        }

        test("writeFileContent and readFileContent should handle large content") {
            withTempFile { tempFile ->
                // Given
                val largeContent = "x".repeat(100_000)

                // When
                writeFileContent(tempFile, largeContent)
                val readContent = readFileContent(tempFile)

                // Then
                readContent.length shouldBe largeContent.length
                readContent shouldBe largeContent
            }
        }

        test("loadResourceContent should throw exception for non-existent resource") {
            // Given
            val nonExistentResource = "non/existent/resource.txt"

            // When & Then
            shouldThrow<IllegalArgumentException> {
                loadResourceContent(nonExistentResource)
            }
        }

        test("writeFileContent and readFileContent should expand tilde to home directory") {
            // Given
            val homeDir = getHomeDirectory()
            if (homeDir.isNotEmpty()) {
                val fileName = "ff4k_tilde_test_${Random.nextLong()}.txt"
                val tildePath = "~/$fileName"
                val expandedPath = "$homeDir/$fileName"

                withTempFile(expandedPath) {
                    // Given
                    val testContent = "Tilde expansion test content"

                    // When
                    writeFileContent(tildePath, testContent)
                    val readContent = readFileContent(expandedPath)
                    val readViaTilde = readFileContent(tildePath)

                    // Then
                    readContent shouldBe testContent
                    readViaTilde shouldBe testContent
                }
            }
        }

        test("readFileContent should throw exception for non-existent tilde path") {
            // Given
            val homeDir = getHomeDirectory()
            if (homeDir.isNotEmpty()) {
                val nonExistentTildePath = "~/this_file_does_not_exist_ff4k_test.txt"

                // When
                val exception = shouldThrow<IllegalArgumentException> {
                    readFileContent(nonExistentTildePath)
                }

                // Then
                exception.message.shouldNotBeNull().let {
                    it shouldContain homeDir
                }
            }
        }

        test("writeFileContent should throw exception for invalid directory") {
            // Given
            val invalidPath = "/this/directory/does/not/exist/file.txt"

            // When & Then
            shouldThrow<IllegalArgumentException> {
                writeFileContent(invalidPath, "content")
            }
        }

        test("readFileContent should reject path traversal attempts") {
            // Given
            val traversalPaths = listOf(
                "../etc/passwd",
                "/tmp/../etc/passwd",
                "foo/../../etc/passwd",
                "..\\windows\\system32",
            )

            // When & Then
            traversalPaths.forEach { path ->
                shouldThrow<IllegalArgumentException> { readFileContent(path) }
            }
        }

        test("writeFileContent should reject path traversal attempts") {
            // Given
            val traversalPaths = listOf(
                "../malicious.txt",
                "/tmp/../../../malicious.txt",
            )

            // When & Then
            traversalPaths.forEach { path ->
                shouldThrow<IllegalArgumentException> { writeFileContent(path, "content") }
            }
        }

        test("loadResourceContent should reject path traversal attempts") {
            // Given
            val traversalPath = "../../../etc/passwd"

            // When & Then
            shouldThrow<IllegalArgumentException> { loadResourceContent(traversalPath) }
        }

        test("readFileContent and writeFileContent should allow paths with dots in filenames") {
            val dottedPath = createTempFilePath().replace(".txt", ".config.backup.txt")
            withTempFile(dottedPath) { path ->
                // Given
                val content = "dotted filename content"

                // When
                writeFileContent(path, content)
                val readContent = readFileContent(path)

                // Then
                readContent shouldBe content
            }
        }

        test("readFileContent and writeFileContent should allow single dot in path segments") {
            withTempFile { tempFile ->
                // Given
                val content = "test content"
                val separator = if (tempFile.contains("/")) "/" else "\\"
                val lastSeparatorIndex = tempFile.lastIndexOf(separator)
                val pathWithDot = if (lastSeparatorIndex >= 0) {
                    val parent = tempFile.substring(0, lastSeparatorIndex)
                    val name = tempFile.substring(lastSeparatorIndex + 1)
                    "$parent$separator.$separator$name"
                } else {
                    ".$separator$tempFile"
                }

                // When
                writeFileContent(pathWithDot, content)
                val readContent = readFileContent(pathWithDot)

                // Then
                readContent shouldBe content
            }
        }
    })

expect fun createTempFilePath(): String

expect fun deleteTempFile(path: String)

expect fun getHomeDirectory(): String
