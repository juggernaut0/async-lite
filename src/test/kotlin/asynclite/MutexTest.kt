package asynclite

import kotlin.test.Test
import kotlin.test.assertEquals

class MutexTest {
    @Test
    fun basic() = async {
        val m = Mutex()
        var s = ""
        // two async tasks modifying a shared resource
        val p1 = async {
            m.withLock {
                s += "a"
                delay(50)
                s += "b"
            }
        }
        val p2 = async {
            m.withLock {
                s += "c"
                delay(50)
                s += "d"
            }
        }
        p1.await()
        p2.await()
        assertEquals("abcd", s)
    }
}