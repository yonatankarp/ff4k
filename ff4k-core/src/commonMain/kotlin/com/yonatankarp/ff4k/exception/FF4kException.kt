package com.yonatankarp.ff4k.exception

/**
 * Base exception for all FF4k library operations.
 *
 * This sealed class serves as the root of the FF4k exception hierarchy, allowing
 * consumers to catch all library-specific exceptions with a single catch block if needed.
 * All exceptions thrown by FF4k components extend from this base class.
 *
 * Being a sealed class, this provides type-safe exception handling and enables
 * exhaustive when expressions for exception handling patterns.
 *
 * @param message The detail message describing the exception
 * @param cause The underlying cause of this exception, or null if none
 *
 * @see FeatureStoreException Base exception for feature store operations
 */
sealed class FF4kException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception for configuration and initialization errors in FF4k.
 *
 * This open class is part of the [FF4kException] hierarchy and is intended for
 * errors that occur during library setup, such as unsupported database types or
 * invalid configuration. Unlike the sealed store exception hierarchies, this class
 * is open to allow extension from other modules.
 *
 * @param message The detail message describing the exception
 * @param cause The underlying cause of this exception, or null if none
 *
 * @see FF4kException
 */
open class FF4kConfigurationException(message: String, cause: Throwable? = null) : FF4kException(message, cause)
