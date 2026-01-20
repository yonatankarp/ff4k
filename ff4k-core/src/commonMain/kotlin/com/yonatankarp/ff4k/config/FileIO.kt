package com.yonatankarp.ff4k.config

/**
 * Reads the entire content of a file as a UTF-8 string.
 *
 * Supports tilde (`~`) expansion to the user's home directory on all platforms.
 *
 * @param filePath The path to the file to read. Can be absolute or relative.
 * @return The file content as a string.
 * @throws IllegalArgumentException if the file cannot be opened or read,
 *         if the path contains directory traversal sequences (`..`),
 *         or if the path starts with `~` and the home directory cannot be determined.
 */
internal expect suspend fun readFileContent(filePath: String): String

/**
 * Writes content to a file, creating it if it doesn't exist or overwriting if it does.
 *
 * Supports tilde (`~`) expansion to the user's home directory on all platforms.
 *
 * @param filePath The path to the file to write. Can be absolute or relative.
 * @param content The content to write to the file.
 * @throws IllegalArgumentException if the file cannot be opened or written,
 *         if the path contains directory traversal sequences (`..`),
 *         or if the path starts with `~` and the home directory cannot be determined.
 */
internal expect suspend fun writeFileContent(filePath: String, content: String)

/**
 * Loads the content of a resource file as a UTF-8 string.
 *
 * Resource resolution is platform-specific:
 * - **JVM/Android**: Uses the classloader to load resources from the classpath.
 * - **Native**: Searches for the resource file in the filesystem. The search order is:
 *   1. `$FF4K_RESOURCES_PATH/<resourcePath>` (if `FF4K_RESOURCES_PATH` env var is set)
 *   2. `<resourcePath>` (direct path)
 *   3. `resources/<resourcePath>`
 *
 * @param resourcePath The path to the resource, relative to the resources root.
 * @return The resource content as a string.
 * @throws IllegalArgumentException if the resource cannot be found,
 *         or if the path contains directory traversal sequences (`..`).
 */
internal expect suspend fun loadResourceContent(resourcePath: String): String
