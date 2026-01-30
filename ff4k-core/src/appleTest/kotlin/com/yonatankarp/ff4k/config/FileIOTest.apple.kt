package com.yonatankarp.ff4k.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.getpid
import platform.posix.remove
import kotlin.random.Random

class FileIOTestNative :
    FunSpec({

        test("loadResourceContent should load existing resource") {
            // Given - create a temp file to act as a resource
            val tempFile = createTempFilePath()
            val expectedContent = "This is a test resource file for FileIO tests."

            try {
                // Setup - write the resource file
                writeFileContent(tempFile, expectedContent)

                // When - load it as a resource (native loads from filesystem)
                val content = loadResourceContent(tempFile)

                // Then
                content shouldBe expectedContent
            } finally {
                deleteTempFile(tempFile)
            }
        }
    })

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
