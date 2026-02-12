package com.yonatankarp.ff4k.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Duration.Companion.seconds

internal class MutexExtensionsTest :
    FunSpec({

        test("should allow re-entry in the same coroutine") {
            // Given
            val mutex = Mutex()

            // When
            mutex.withReentrantLock {
                // Then
                mutex.withReentrantLock {
                    true.shouldBeTrue()
                }
            }
        }

        test("should guarantee mutual exclusion between coroutines").config(timeout = 2.seconds) {
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
                    inCriticalSection.shouldBeFalse()
                }
            }

            // Then
            job2.isActive.shouldBeTrue()
            job2.isCompleted.shouldBeFalse()

            // When
            releaseJob1.complete(Unit)
            job1.join()
            job2.join()

            // Then
            job2.isCompleted.shouldBeTrue()
        }

        test("should support nested locking of different mutexes").config(timeout = 2.seconds) {
            // Given
            val mutex1 = Mutex()
            val mutex2 = Mutex()

            // When
            mutex1.withReentrantLock {
                mutex2.withReentrantLock {
                    // Then
                    mutex1.withReentrantLock {
                        true.shouldBeTrue()
                    }
                }
            }
        }

        test("should support interleaving locks").config(timeout = 2.seconds) {
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
                true.shouldBeTrue()
            }
        }
    })
