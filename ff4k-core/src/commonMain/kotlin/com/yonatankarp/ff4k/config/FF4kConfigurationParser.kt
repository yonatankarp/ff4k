package com.yonatankarp.ff4k.config

/**
 * Interface for parsing and exporting FF4k configurations.
 *
 * Implementations of this interface handle the serialization and deserialization
 * of FF4k configuration data from various sources (files, resources) and formats.
 *
 * @param C The type of configuration object this parser produces and consumes.
 *
 * @see JsonFF4kConfigurationParser
 * @see FF4kConfiguration
 */
interface FF4kConfigurationParser<C> {
    /**
     * Parses a configuration from a resource file bundled with the application.
     *
     * Resource resolution is platform-specific:
     * - **JVM/Android**: Uses the classloader to load resources from the classpath.
     * - **Native**: Searches for the resource in the filesystem using platform-specific paths.
     *
     * @param file The path to the resource, relative to the resources root.
     * @return The parsed configuration object.
     * @throws IllegalArgumentException if the resource cannot be found or parsed.
     */
    suspend fun parseConfigurationResource(file: String): C

    /**
     * Parses a configuration from a file on the filesystem.
     *
     * Supports tilde (`~`) expansion to the user's home directory on all platforms.
     *
     * @param file The path to the file to parse. Can be absolute or relative.
     * @return The parsed configuration object.
     * @throws IllegalArgumentException if the file cannot be read or parsed.
     */
    suspend fun parseConfigurationFile(file: String): C

    /**
     * Exports a configuration object to its serialized string representation.
     *
     * @param configuration The configuration object to export.
     * @return The serialized configuration as a string.
     */
    suspend fun export(configuration: C): String
}
