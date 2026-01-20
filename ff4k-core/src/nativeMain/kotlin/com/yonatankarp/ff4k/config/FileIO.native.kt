package com.yonatankarp.ff4k.config

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun readFileContent(filePath: String): String {
    validatePath(filePath)
    val expandedPath = expandPath(filePath)
    return useFile(expandedPath, "rb") { file ->
        readFileToString(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun writeFileContent(filePath: String, content: String) {
    validatePath(filePath)
    val expandedPath = expandPath(filePath)
    useFile(expandedPath, "wb") { file ->
        writeBytesToFile(file, content, expandedPath)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun loadResourceContent(resourcePath: String): String {
    validatePath(resourcePath)
    val possiblePaths = resolveResourcePaths(resourcePath)

    return findAndReadResource(possiblePaths)
        ?: throw IllegalArgumentException(
            "Resource not found: $resourcePath (searched: ${possiblePaths.joinToString(", ")})",
        )
}

/**
 * Resolves possible paths for a resource.
 *
 * @param resourcePath The relative path to the resource.
 * @return A list of potential absolute or relative paths to check.
 */
@OptIn(ExperimentalForeignApi::class)
private fun resolveResourcePaths(resourcePath: String): List<String> {
    val resourceBasePath = getenv("FF4K_RESOURCES_PATH")?.toKString()
    return buildList {
        if (resourceBasePath != null) {
            add(joinPath(resourceBasePath, resourcePath))
        }
        add(resourcePath)
        add(joinPath("resources", resourcePath))
    }
}

/**
 * Attempts to find and read a resource from a list of paths.
 *
 * @param paths The list of paths to check.
 * @return The content of the first found resource, or null if none found.
 */
@OptIn(ExperimentalForeignApi::class)
private fun findAndReadResource(paths: List<String>): String? {
    for (path in paths) {
        val file = fopen(path, "rb") ?: continue
        try {
            return readFileToString(file)
        } finally {
            fclose(file)
        }
    }
    return null
}

/**
 * Internal helper to safely use a file pointer.
 * Opens the file, executes the block, and ensures the file is closed.
 *
 * @param path The file path to open.
 * @param mode The mode string (e.g., "rb", "wb").
 * @param block The code block to execute with the file pointer.
 * @return The result of the block execution.
 */
@OptIn(ExperimentalForeignApi::class)
private inline fun <R> useFile(
    path: String,
    mode: String,
    block: (kotlinx.cinterop.CPointer<FILE>) -> R,
): R {
    val file = fopen(path, mode) ?: throw IllegalArgumentException("Cannot open file: $path")
    try {
        return block(file)
    } finally {
        fclose(file)
    }
}

/**
 * Internal helper to write string content to a file.
 *
 * @param file The file pointer to write to.
 * @param content The string content to write.
 * @param path The path of the file (for error reporting).
 */
@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToFile(
    file: kotlinx.cinterop.CPointer<FILE>,
    content: String,
    path: String,
) {
    val bytes = content.encodeToByteArray()
    if (bytes.isNotEmpty()) {
        val bytesWritten = fwrite(bytes.refTo(0), 1u, bytes.size.toULong(), file)
        require(bytesWritten == bytes.size.toULong()) {
            "Cannot write complete content to file: $path (written $bytesWritten of ${bytes.size} bytes)"
        }
    }
}

/**
 * Internal helper to read file content into a string.
 *
 * @param file The file pointer to read from.
 * @return The file content as a string.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun readFileToString(file: kotlinx.cinterop.CPointer<FILE>): String = memScoped {
    val size = determineFileSize(file)
    if (size == 0L) return@memScoped ""
    readAndConvertToString(file, size)
}

/**
 * Internal helper to determine file size.
 *
 * @param file The file pointer.
 * @return The size of the file in bytes.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun determineFileSize(file: kotlinx.cinterop.CPointer<FILE>): Long {
    require(fseek(file, 0, SEEK_END) == 0) { "Failed to seek to end of file" }

    val size = ftell(file).toLong()
    require(size >= 0L) { "Failed to determine file size" }
    require(fseek(file, 0, SEEK_SET) == 0) { "Failed to seek to beginning of file" }

    return size
}

/**
 * Internal helper to read and convert file content.
 *
 * @param file The file pointer.
 * @param size The expected size of the file to read. Must be <= Int.MAX_VALUE.
 * @return The file content as a string.
 * @throws IllegalArgumentException if the file is too large to read into memory.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun kotlinx.cinterop.MemScope.readAndConvertToString(
    file: kotlinx.cinterop.CPointer<FILE>,
    size: Long,
): String {
    require(size <= Int.MAX_VALUE.toLong()) {
        "File too large to read into memory: $size bytes exceeds maximum of ${Int.MAX_VALUE} bytes"
    }

    val buffer = allocArray<ByteVar>(size + 1)
    val bytesRead = fread(buffer, 1u, size.toULong(), file)
    val bytesReadLong = bytesRead.toLong()

    require(bytesReadLong == size) {
        "Failed to read file content: expected $size bytes but read $bytesReadLong bytes"
    }

    buffer[bytesReadLong.toInt()] = 0
    return buffer.toKString()
}

/**
 * Validates that the path does not contain traversal sequences.
 *
 * @param path The path to validate.
 * @throws IllegalArgumentException if the path contains "..".
 */
private fun validatePath(path: String) {
    val segments = path.replace("\\", "/").split("/")
    require(segments.none { it == ".." }) { "Path traversal detected: $path" }
}

/**
 * Expands the tilde (`~`) in the path to the user's home directory.
 *
 * @param path The path to expand.
 * @return The expanded path, or the original path if no tilde is present.
 * @throws IllegalArgumentException if the path starts with `~` and the home directory cannot be determined.
 */
@OptIn(ExperimentalForeignApi::class)
private fun expandPath(path: String): String {
    if (!path.startsWith("~")) return path

    require(path.length <= 1 || path[1] == '/') {
        "Unsupported tilde expansion in path '$path': only '~' and '~/...' patterns are supported. " +
            "The '~username' syntax is not supported."
    }

    val home = getenv("HOME")?.toKString()
        ?: getenv("USERPROFILE")?.toKString()
        ?: throw IllegalArgumentException(
            "Cannot expand '~' in path '$path': home directory could not be determined. " +
                "Set the HOME or USERPROFILE environment variable, or use an absolute path.",
        )
    return home + path.substring(1)
}

/**
 * Joins multiple path segments into a single path.
 *
 * @param paths The path segments to join.
 * @return The joined path string.
 */
private fun joinPath(vararg paths: String): String = paths.joinToString(separator = "/") { it.trimEnd('/', '\\').trimStart('/', '\\') }
