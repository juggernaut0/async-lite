package asynclite

import kotlinx.browser.window
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

fun <T> async(block: suspend () -> T): Promise<T> {
    return Promise { resolve, reject ->
        block.startCoroutine(Continuation(EmptyCoroutineContext) { it.fold(resolve, reject) })
    }
}

fun <T> Promise<T>.wrap(): Promise<Result<T>> {
    return then({ Result.success(it) }, { Result.failure(it) })
}

fun <T> Promise<Result<T>>.unwrap(): Promise<T> {
    return then { it.getOrThrow() }
}

suspend fun <T> Promise<T>.await(): T {
    return suspendCoroutine { cont ->
        then({ cont.resumeWith(Result.success(it)) }, { cont.resumeWith(Result.failure(it)) })
    }
}

suspend fun <T> Promise<Result<T>>.await(): T {
    return suspendCoroutine { cont ->
        then { cont.resumeWith(it) }
    }
}

suspend fun delay(millis: Int) {
    return suspendCoroutine { cont ->
        window.setTimeout({
            cont.resumeWith(Result.success(Unit))
        }, millis)
    }
}

@OptIn(ExperimentalContracts::class)
suspend inline fun delay(millis: Int, crossinline fn: (Cancellation) -> Unit) {
    contract {
        callsInPlace(fn, kind = InvocationKind.EXACTLY_ONCE)
    }

    return suspendCoroutine { cont ->
        val handle = window.setTimeout({
            cont.resumeWith(Result.success(Unit))
        }, millis)
        fn(Cancellation(handle))
    }
}

class Cancellation(private val handle: Int) {
    fun cancel() {
        window.clearTimeout(handle)
    }
}
