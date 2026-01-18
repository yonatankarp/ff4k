package com.yonatankarp.ff4k.config

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileIOTestJvmShared {

    @Test
    fun `loadResourceContent should load existing resource`() = runTest {
        // Given
        val resourceName = "test-resource.txt"

        // When
        val content = loadResourceContent(resourceName)

        // Then
        assertEquals("This is a test resource file for FileIO tests.", content)
    }

    @Test
    fun `readFileContent should include expanded path in error message`() = runTest {
        // Given
        val homeDir = System.getProperty("user.home").orEmpty()

        // When
        val exception = assertFailsWith<IllegalArgumentException> {
            readFileContent("~/non_existent_ff4k_test_file_12345.txt")
        }

        // Then
        assertTrue(homeDir in exception.message.orEmpty())
        assertTrue("Failed to read file" in exception.message.orEmpty())
    }

    @Test
    fun `writeFileContent should include expanded path in error message for invalid directory`() = runTest {
        // Given
        val homeDir = System.getProperty("user.home").orEmpty()

        // When
        val exception = assertFailsWith<IllegalArgumentException> {
            writeFileContent("~/non_existent_dir_ff4k/subdir/file.txt", "content")
        }

        // Then
        assertTrue(homeDir in exception.message.orEmpty())
        assertTrue("Failed to write" in exception.message.orEmpty())
    }

    @Test
    fun `loadResourceContent should fall back to system classloader when context classloader is null`() = runTest {
        // Given
        val resourceName = "test-resource.txt"

        withContextClassLoader(null) {
            // When
            val content = loadResourceContent(resourceName)

            // Then
            assertEquals("This is a test resource file for FileIO tests.", content)
        }
    }

    @Test
    fun `readFileContent should throw when user home is not set and path starts with tilde`() = runTest {
        // Given
        val tildePath = "~/some/file.txt"

        withoutSystemProperty("user.home") {
            // When
            val exception = assertFailsWith<IllegalArgumentException> {
                readFileContent(tildePath)
            }

            // Then
            assertTrue("Cannot expand '~'" in exception.message.orEmpty())
            assertTrue("home directory could not be determined" in exception.message.orEmpty())
        }
    }

    @Test
    fun `writeFileContent should throw when user home is not set and path starts with tilde`() = runTest {
        // Given
        val tildePath = "~/some/file.txt"

        withoutSystemProperty("user.home") {
            // When
            val exception = assertFailsWith<IllegalArgumentException> {
                writeFileContent(tildePath, "content")
            }

            // Then
            assertTrue("Cannot expand '~'" in exception.message.orEmpty())
        }
    }

    private inline fun <T> withoutSystemProperty(key: String, block: () -> T): T {
        val original = System.getProperty(key)
        return try {
            System.clearProperty(key)
            block()
        } finally {
            if (original != null) System.setProperty(key, original)
        }
    }

    private inline fun <T> withContextClassLoader(classLoader: ClassLoader?, block: () -> T): T {
        val original = Thread.currentThread().contextClassLoader
        return try {
            Thread.currentThread().contextClassLoader = classLoader
            block()
        } finally {
            Thread.currentThread().contextClassLoader = original
        }
    }
}

actual fun createTempFilePath(): String {
    val tempDir = System.getProperty("java.io.tmpdir")
    val pid = ProcessHandle.current().pid()
    val randomId = Random.nextLong().toString(16)
    return "$tempDir/ff4k-test-$pid-$randomId.txt"
}

actual fun deleteTempFile(path: String) {
    File(path).delete()
}

actual fun getHomeDirectory(): String = System.getProperty("user.home") ?: ""
