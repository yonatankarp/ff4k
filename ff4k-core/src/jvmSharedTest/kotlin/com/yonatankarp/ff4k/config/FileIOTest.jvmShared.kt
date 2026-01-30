package com.yonatankarp.ff4k.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.random.Random

class FileIOTestJvmShared :
    FunSpec({

        test("loadResourceContent should load existing resource") {
            // Given
            val resourceName = "test-resource.txt"

            // When
            val content = loadResourceContent(resourceName)

            // Then
            content shouldBe "This is a test resource file for FileIO tests."
        }

        test("readFileContent should include expanded path in error message") {
            // Given
            val homeDir = System.getProperty("user.home").orEmpty()

            // When
            val exception = shouldThrow<IllegalArgumentException> {
                readFileContent("~/non_existent_ff4k_test_file_12345.txt")
            }

            // Then
            exception.message.orEmpty() shouldContain homeDir
            exception.message.orEmpty() shouldContain "Failed to read file"
        }

        test("writeFileContent should include expanded path in error message for invalid directory") {
            // Given
            val homeDir = System.getProperty("user.home").orEmpty()

            // When
            val exception = shouldThrow<IllegalArgumentException> {
                writeFileContent("~/non_existent_dir_ff4k/subdir/file.txt", "content")
            }

            // Then
            exception.message.orEmpty() shouldContain homeDir
            exception.message.orEmpty() shouldContain "Failed to write"
        }

        test("loadResourceContent should fall back to system classloader when context classloader is null") {
            // Given
            val resourceName = "test-resource.txt"

            withContextClassLoader(null) {
                // When
                val content = loadResourceContent(resourceName)

                // Then
                content shouldBe "This is a test resource file for FileIO tests."
            }
        }

        test("readFileContent should throw when user home is not set and path starts with tilde") {
            // Given
            val tildePath = "~/some/file.txt"

            withoutSystemProperty("user.home") {
                // When
                val exception = shouldThrow<IllegalArgumentException> {
                    readFileContent(tildePath)
                }

                // Then
                exception.message.orEmpty() shouldContain "Cannot expand '~'"
                exception.message.orEmpty() shouldContain "home directory could not be determined"
            }
        }

        test("writeFileContent should throw when user home is not set and path starts with tilde") {
            // Given
            val tildePath = "~/some/file.txt"

            withoutSystemProperty("user.home") {
                // When
                val exception = shouldThrow<IllegalArgumentException> {
                    writeFileContent(tildePath, "content")
                }

                // Then
                exception.message.orEmpty() shouldContain "Cannot expand '~'"
            }
        }
    })

private suspend fun <T> withoutSystemProperty(key: String, block: suspend () -> T): T {
    val original = System.getProperty(key)
    return try {
        System.clearProperty(key)
        block()
    } finally {
        if (original != null) System.setProperty(key, original)
    }
}

private suspend fun <T> withContextClassLoader(classLoader: ClassLoader?, block: suspend () -> T): T {
    val original = Thread.currentThread().contextClassLoader
    return try {
        Thread.currentThread().contextClassLoader = classLoader
        block()
    } finally {
        Thread.currentThread().contextClassLoader = original
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
