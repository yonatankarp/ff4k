package com.yonatankarp.ff4k.config

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun readFileContent(filePath: String): String = withContext(Dispatchers.IO) {
    validatePath(filePath)
    val expandedPath = expandPath(filePath)
    try {
        File(expandedPath).readText()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        throw IllegalArgumentException(
            "Failed to read file '$expandedPath' (${e::class.java.simpleName}: ${e.message})",
            e,
        )
    }
}

actual suspend fun writeFileContent(filePath: String, content: String) {
    withContext(Dispatchers.IO) {
        validatePath(filePath)
        val expandedPath = expandPath(filePath)
        try {
            File(expandedPath).writeText(content)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw IllegalArgumentException(
                "Failed to write to file '$expandedPath' (${e::class.java.simpleName}: ${e.message})",
                e,
            )
        }
    }
}

actual suspend fun loadResourceContent(resourcePath: String): String = withContext(Dispatchers.IO) {
    validatePath(resourcePath)
    val classLoader = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
    try {
        classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        if (e is IllegalArgumentException) throw e
        throw IllegalArgumentException(
            "Failed to load resource '$resourcePath' (${e::class.java.simpleName}: ${e.message})",
            e,
        )
    }
}

private fun validatePath(path: String) {
    val segments = path.replace("\\", "/").split("/")
    require(segments.none { it == ".." }) { "Path traversal detected: $path" }
}

private fun expandPath(path: String): String {
    if (!path.startsWith("~")) return path
    if (path.length > 1 && path[1] != '/') {
        throw IllegalArgumentException(
            "Unsupported tilde expansion in path '$path': only '~' and '~/...' patterns are supported. " +
                "The '~username' syntax is not supported.",
        )
    }
    val home = System.getProperty("user.home")
        ?: throw IllegalArgumentException(
            "Cannot expand '~' in path '$path': home directory could not be determined. " +
                "Set the 'user.home' system property, or use an absolute path.",
        )
    return home + path.substring(1)
}
