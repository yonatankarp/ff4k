package com.yonatankarp.ff4k.config

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.test.runTest
import platform.posix.getenv
import platform.posix.getpid
import platform.posix.remove
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class FileIOTestNative {

    @Test
    fun `loadResourceContent should load existing resource`() = runTest {
        // Given - create a temp file to act as a resource
        val tempFile = createTempFilePath()
        val expectedContent = "This is a test resource file for FileIO tests."

        try {
            // Setup - write the resource file
            writeFileContent(tempFile, expectedContent)

            // When - load it as a resource (native loads from filesystem)
            val content = loadResourceContent(tempFile)

            // Then
            assertEquals(expectedContent, content)
        } finally {
            deleteTempFile(tempFile)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun createTempFilePath(): String {
    val tempDir = getenv("TMPDIR")?.toKString()
        ?: getenv("TMP")?.toKString()
        ?: getenv("TEMP")?.toKString()
        ?: "/tmp"
    val pid = getpid()
    val randomId = Random.nextLong().toString(16)
    return "$tempDir/ff4k-test-$pid-$randomId.txt"
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteTempFile(path: String) {
    remove(path)
}

@OptIn(ExperimentalForeignApi::class)
actual fun getHomeDirectory(): String = getenv("HOME")?.toKString()
    ?: getenv("USERPROFILE")?.toKString()
    ?: ""
