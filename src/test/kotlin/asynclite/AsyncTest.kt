package asynclite

import kotlin.js.Promise
import kotlin.test.*

class AsyncTest {
    @Test
    fun asyncTest() = async {
        var s = "a"
        delay(50)
        s += "b"
        delay(50)
        assertEquals("ab", s)
    }

    @Test
    fun cancellation() = async {
        var cancellation: Cancellation? = null
        async {
            delay(100) { cancellation = it }
            fail()
        }
        delay(50)
        cancellation!!.cancel()
        delay(100)
    }

    @Test
    fun rejected() = async {
        val p: Promise<Unit> = async {
            delay(50)
            throw RuntimeException("hi")
        }
        delay(25)
        try {
            p.await()
            fail()
        } catch (e: RuntimeException) {
            assertEquals("hi", e.message)
        }
    }

    @Test
    fun rejectedAndCaughtLater() = async {
        val p: Promise<Result<Unit>> = async {
            delay(25)
            throw RuntimeException("hi")
        }.wrap() // wrapped because jest does not like "uncaught" rejected promises
        delay(50)
        try {
            p.await()
            fail()
        } catch (e: RuntimeException) {
            assertEquals("hi", e.message)
        }
    }

    @Test
    fun scoped() = async {
        var flag = false
        scope {
            async {
                delay(50)
                flag = true
            }
        }
        assertTrue(flag)
    }
}
