package asynclite

import kotlin.browser.window
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Mutex {
    private var lock: Unit? = null
    private val blocked: MutableList<Blocker> = mutableListOf()

    private val isLocked get() = lock != null

    suspend fun lock() {
        if (isLocked) {
            val blocker = Blocker()
            blocked.add(blocker)
            blocker.await()
            check(!isLocked)
        }

        lock = Unit
    }

    fun unlock() {
        lock = null

        getBlocker()?.notify()
    }

    private fun getBlocker(): Blocker? = blocked.takeIf { it.isNotEmpty() }?.removeAt(0)

    suspend inline fun <T> withLock(block: () -> T): T {
        lock()
        val result = block()
        unlock()
        return result
    }

    private class Blocker {
        private lateinit var continuation: Continuation<Unit>

        fun notify() {
            // timeout to postpone execution
            window.setTimeout({
                continuation.resume(Unit)
            })
        }

        suspend fun await() {
            suspendCoroutine<Unit> {
                continuation = it
            }
        }
    }
}