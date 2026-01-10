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
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
sealed class FF4kException(message: String, cause: Throwable? = null) : Exception(message, cause)
