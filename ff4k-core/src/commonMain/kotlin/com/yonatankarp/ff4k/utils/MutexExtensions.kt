package com.yonatankarp.ff4k.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A reentrant lock implementation for Kotlin Coroutines Mutex.
 *
 * This function checks if the current coroutine context already holds the lock for this specific Mutex.
 * If it does, it executes the block directly.
 * If not, it acquires the lock, adds a marker to the context, and then executes the block.
 *
 * This implementation supports nesting different mutexes (e.g. locking A then B) because `withContext`
 * restores the previous context value upon completion.
 */
suspend fun <T> Mutex.withReentrantLock(block: suspend () -> T): T {
    val key = ReentrantMutexContextElement.Key
    val element = currentCoroutineContext()[key]

    return if (element != null && element.heldMutexes.any { it === this }) {
        block()
    } else {
        withLock {
            val currentHeld = element?.heldMutexes ?: emptyList()
            val newHeld = currentHeld + this@withReentrantLock
            withContext(ReentrantMutexContextElement(newHeld)) {
                block()
            }
        }
    }
}

/**
 * Context element to track which Mutexes are currently held by the coroutine.
 */
internal class ReentrantMutexContextElement(
    val heldMutexes: List<Mutex>,
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<ReentrantMutexContextElement>
}
