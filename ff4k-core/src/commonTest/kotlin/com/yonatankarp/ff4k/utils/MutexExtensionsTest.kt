package com.yonatankarp.ff4k.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MutexExtensionsTest {

    @Test
    fun `should allow re-entry in the same coroutine`() = runTest {
        // Given
        val mutex = Mutex()

        // When
        mutex.withReentrantLock {
            // Then
            mutex.withReentrantLock {
                assertTrue(true, "Should be able to re-enter lock")
            }
        }
    }

    @Test
    fun `should guarantee mutual exclusion between coroutines`() = runTest {
        withDefaultTimeout {
            // Given
            val mutex = Mutex()
            var inCriticalSection = false
            val job1AcquiredLock = CompletableDeferred<Unit>()
            val releaseJob1 = CompletableDeferred<Unit>()

            val job1 = launch {
                mutex.withReentrantLock {
                    inCriticalSection = true
                    job1AcquiredLock.complete(Unit)
                    releaseJob1.await()
                    inCriticalSection = false
                }
            }

            job1AcquiredLock.await()

            // When
            val job2 = launch {
                mutex.withReentrantLock {
                    assertFalse(inCriticalSection, "Job2 entered critical section while Job1 held the lock")
                }
            }

            testScheduler.advanceTimeBy(100)

            // Then
            assertTrue(job2.isActive, "Job2 should be suspended waiting for the lock")
            assertFalse(job2.isCompleted, "Job2 should not have completed yet")

            // When
            releaseJob1.complete(Unit)
            job1.join()
            job2.join()

            // Then
            assertTrue(job2.isCompleted, "Job2 should have completed after lock release")
        }
    }

    @Test
    fun `should support nested locking of different mutexes`() = runTest {
        withDefaultTimeout {
            // Given
            val mutex1 = Mutex()
            val mutex2 = Mutex()

            // When
            mutex1.withReentrantLock {
                mutex2.withReentrantLock {
                    // Then
                    mutex1.withReentrantLock {
                        assertTrue(true, "Should be able to re-enter mutex1 while holding mutex2")
                    }
                }
            }
        }
    }

    @Test
    fun `should support interleaving locks`() = runTest {
        withDefaultTimeout {
            // Given
            val mutex = Mutex()

            launch {
                mutex.withReentrantLock {
                    // Lock held
                }
            }.join()

            // When
            mutex.withReentrantLock {
                // Then
                assertTrue(true, "Should be able to acquire lock after it was released")
            }
        }
    }

    private suspend fun <T> withDefaultTimeout(block: suspend CoroutineScope.() -> T): T = withTimeout(DEFAULT_TIMEOUT) {
        block()
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 2_000L
    }
}
