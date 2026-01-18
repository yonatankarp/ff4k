package com.yonatankarp.ff4k.config

import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileIOTest {

    private suspend fun <T> withTempFile(
        path: String = createTempFilePath(),
        block: suspend (String) -> T,
    ): T = try {
        block(path)
    } finally {
        runCatching {
            deleteTempFile(path)
        }
    }

    @Test
    fun `readFileContent should throw exception for non-existent file`() = runTest {
        // Given
        val nonExistentPath = "/this/path/does/not/exist/file.txt"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            readFileContent(nonExistentPath)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should round-trip content`() = runTest {
        withTempFile { tempFile ->
            // Given
            val testContent = "Hello, FF4K!\nThis is a test file."

            // When
            writeFileContent(tempFile, testContent)
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals(testContent, readContent)
        }
    }

    @Test
    fun `writeFileContent should overwrite existing file`() = runTest {
        withTempFile { tempFile ->
            // Given
            val initialContent = "Initial content"
            val updatedContent = "Updated content"
            writeFileContent(tempFile, initialContent)

            // When
            writeFileContent(tempFile, updatedContent)
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals(updatedContent, readContent)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should handle empty content`() = runTest {
        withTempFile { tempFile ->
            // When
            writeFileContent(tempFile, "")
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals("", readContent)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should handle unicode content`() = runTest {
        withTempFile { tempFile ->
            // Given
            val unicodeContent = "Hello 世界! Привет мир! 🎉"

            // When
            writeFileContent(tempFile, unicodeContent)
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals(unicodeContent, readContent)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should handle multiline content`() = runTest {
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
            assertEquals(multilineContent, readContent)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should handle large content`() = runTest {
        withTempFile { tempFile ->
            // Given
            val largeContent = "x".repeat(100_000)

            // When
            writeFileContent(tempFile, largeContent)
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals(largeContent.length, readContent.length)
            assertEquals(largeContent, readContent)
        }
    }

    @Test
    fun `loadResourceContent should throw exception for non-existent resource`() = runTest {
        // Given
        val nonExistentResource = "non/existent/resource.txt"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            loadResourceContent(nonExistentResource)
        }
    }

    @Test
    fun `writeFileContent and readFileContent should expand tilde to home directory`() = runTest {
        // Given
        val homeDir = getHomeDirectory()
        if (homeDir.isEmpty()) return@runTest

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
            assertEquals(testContent, readContent)
            assertEquals(testContent, readViaTilde)
        }
    }

    @Test
    fun `readFileContent should throw exception for non-existent tilde path`() = runTest {
        // Given
        val homeDir = getHomeDirectory()
        if (homeDir.isEmpty()) return@runTest

        val nonExistentTildePath = "~/this_file_does_not_exist_ff4k_test.txt"

        // When
        val exception = assertFailsWith<IllegalArgumentException> {
            readFileContent(nonExistentTildePath)
        }

        // Then
        assertEquals(true, exception.message?.contains(homeDir))
    }

    @Test
    fun `writeFileContent should throw exception for invalid directory`() = runTest {
        // Given
        val invalidPath = "/this/directory/does/not/exist/file.txt"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            writeFileContent(invalidPath, "content")
        }
    }

    @Test
    fun `readFileContent should reject path traversal attempts`() = runTest {
        // Given
        val traversalPaths = listOf(
            "../etc/passwd",
            "/tmp/../etc/passwd",
            "foo/../../etc/passwd",
            "..\\windows\\system32",
        )

        // When & Then
        traversalPaths.forEach { path ->
            assertFailsWith<IllegalArgumentException> { readFileContent(path) }
        }
    }

    @Test
    fun `writeFileContent should reject path traversal attempts`() = runTest {
        // Given
        val traversalPaths = listOf(
            "../malicious.txt",
            "/tmp/../../../malicious.txt",
        )

        // When & Then
        traversalPaths.forEach { path ->
            assertFailsWith<IllegalArgumentException> { writeFileContent(path, "content") }
        }
    }

    @Test
    fun `loadResourceContent should reject path traversal attempts`() = runTest {
        // Given
        val traversalPath = "../../../etc/passwd"

        // When & Then
        assertFailsWith<IllegalArgumentException> { loadResourceContent(traversalPath) }
    }

    @Test
    fun `readFileContent and writeFileContent should allow paths with dots in filenames`() = runTest {
        val dottedPath = createTempFilePath().replace(".txt", ".config.backup.txt")
        withTempFile(dottedPath) { path ->
            // Given
            val content = "dotted filename content"

            // When
            writeFileContent(path, content)
            val readContent = readFileContent(path)

            // Then
            assertEquals(content, readContent)
        }
    }

    @Test
    fun `readFileContent and writeFileContent should allow single dot in path segments`() = runTest {
        withTempFile { tempFile ->
            // Given
            val content = "test content"

            // When
            writeFileContent(tempFile, content)
            val readContent = readFileContent(tempFile)

            // Then
            assertEquals(content, readContent)
        }
    }
}

expect fun createTempFilePath(): String
expect fun deleteTempFile(path: String)
expect fun getHomeDirectory(): String
