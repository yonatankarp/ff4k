package com.yonatankarp.ff4k.config

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
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

    @Suppress("CAST_NEVER_SUCCEEDS")
    val nsContent = content as NSString
    val success = nsContent.writeToFile(expandedPath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    require(success) { "Cannot write to file: $expandedPath" }
}

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun loadResourceContent(resourcePath: String): String {
    validatePath(resourcePath)

    // Try environment variable path first (for macOS command-line tests)
    val envPath = getenv("FF4K_RESOURCES_PATH")?.toKString()
    if (envPath != null) {
        val fullPath = joinPath(envPath, resourcePath)
        val content = tryReadFile(fullPath)
        if (content != null) return content
    }

    // Try NSBundle for bundled resources (iOS/macOS app bundles)
    val bundleContent = tryLoadFromBundle(resourcePath)
    if (bundleContent != null) return bundleContent

    // Try direct path and resources/ prefix as fallback
    val directContent = tryReadFile(resourcePath)
    if (directContent != null) return directContent

    val resourcesContent = tryReadFile(joinPath("resources", resourcePath))
    if (resourcesContent != null) return resourcesContent

    val searchedPaths = buildList {
        if (envPath != null) add(joinPath(envPath, resourcePath))
        add("NSBundle (resource: $resourcePath)")
        add(resourcePath)
        add(joinPath("resources", resourcePath))
    }
    throw IllegalArgumentException(
        "Resource not found: $resourcePath (searched: ${searchedPaths.joinToString(", ")})",
    )
}

/**
 * Attempts to load a resource from the main bundle using NSBundle API.
 * This works for resources bundled with iOS/macOS applications and test bundles.
 */
@OptIn(ExperimentalForeignApi::class)
private fun tryLoadFromBundle(resourcePath: String): String? {
    // Trim leading slashes to avoid empty subdirectory strings
    val normalizedPath = resourcePath.trimStart('/')

    // Split the resource path into name and extension
    val lastSlash = normalizedPath.lastIndexOf('/')
    val fileName = if (lastSlash > 0) normalizedPath.substring(lastSlash + 1) else normalizedPath
    val subdirectory = if (lastSlash > 0) normalizedPath.substring(0, lastSlash) else null

    val lastDot = fileName.lastIndexOf('.')
    val name = if (lastDot >= 0) fileName.substring(0, lastDot) else fileName
    val ext = if (lastDot >= 0) fileName.substring(lastDot + 1) else null

    // Try to find the resource in the main bundle
    val path = if (subdirectory != null) {
        NSBundle.mainBundle.pathForResource(name, ofType = ext, inDirectory = subdirectory)
    } else {
        NSBundle.mainBundle.pathForResource(name, ofType = ext)
    }

    if (path == null) return null

    @Suppress("CAST_NEVER_SUCCEEDS")
    return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)
}

/**
 * Attempts to read a file from the filesystem, returning null if it doesn't exist.
 */
@OptIn(ExperimentalForeignApi::class)
private fun tryReadFile(path: String): String? {
    val file = fopen(path, "rb") ?: return null
    return try {
        readFileToString(file)
    } finally {
        fclose(file)
    }
}

/**
 * Internal helper to safely use a file pointer.
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
 * Internal helper to read file content into a string.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun readFileToString(file: kotlinx.cinterop.CPointer<FILE>): String = memScoped {
    val size = determineFileSize(file)
    if (size == 0L) return@memScoped ""
    readAndConvertToString(file, size)
}

/**
 * Internal helper to determine file size.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun determineFileSize(file: kotlinx.cinterop.CPointer<FILE>): Long {
    require(fseek(file, 0, SEEK_END) == 0) { "Failed to seek to end of file" }

    val size = ftell(file)
    require(size >= 0L) { "Failed to determine file size" }
    require(fseek(file, 0, SEEK_SET) == 0) { "Failed to seek to beginning of file" }

    return size
}

/**
 * Internal helper to read and convert file content.
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
 */
private fun validatePath(path: String) {
    val segments = path.replace("\\", "/").split("/")
    require(segments.none { it == ".." }) { "Path traversal detected: $path" }
}

/**
 * Expands the tilde (`~`) in the path to the user's home directory.
 */
@OptIn(ExperimentalForeignApi::class)
private fun expandPath(path: String): String {
    if (!path.startsWith("~")) return path

    require(path.length <= 1 || path[1] == '/') {
        "Unsupported tilde expansion in path '$path': only '~' and '~/...' patterns are supported. " +
            "The '~username' syntax is not supported."
    }

    val home = getenv("HOME")?.toKString()
        ?: throw IllegalArgumentException(
            "Cannot expand '~' in path '$path': home directory could not be determined. " +
                "Set the HOME environment variable, or use an absolute path.",
        )
    return home + path.substring(1)
}

/**
 * Joins multiple path segments into a single path.
 */
private fun joinPath(vararg paths: String): String {
    if (paths.isEmpty()) return ""
    if (paths.size == 1) return normalizeSingleSegment(paths.first())
    return joinMultipleSegments(paths.toList())
}

private fun normalizeSingleSegment(segment: String): String = when {
    segment == "/" -> "/"
    else -> segment.trimEnd('/', '\\')
}

private fun joinMultipleSegments(paths: List<String>): String {
    val first = paths.first()
    val prefix = if (first.startsWith("/")) "/" else ""
    val normalizedFirst = first.trim('/', '\\')
    val rest = paths.drop(1).map { it.trim('/', '\\') }
    val segments = (listOf(normalizedFirst) + rest).filter { it.isNotEmpty() }
    return prefix + segments.joinToString(separator = "/")
}
