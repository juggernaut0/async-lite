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

fun <T> async(block: suspend CoroutineScope.() -> T): Promise<T> = mkPromise(block)

private fun <T> mkPromise(block: suspend CoroutineScope.() -> T): Promise<T> {
    val scope = CoroutineScopeImpl(arrayOf())
    return Promise<T> { resolve, reject ->
        block.startCoroutine(scope, Continuation(EmptyCoroutineContext) { it.fold(resolve, reject) })
    }.then { res ->
        scope.waitForChildren(res)
    }.flatten()
}

fun <T> CoroutineScope.async(block: suspend CoroutineScope.() -> T): Promise<T> {
    val p = mkPromise(block)
    this as CoroutineScopeImpl
    children.asDynamic().push(p)
    return p
}

private fun <T> CoroutineScopeImpl.waitForChildren(res: T): Promise<T> {
    return Promise.allSettled(children).then { res }
}

private inline fun <T> Promise.Companion.allSettled(ps: Array<Promise<T>>): Promise<Array<PromiseOutcome<T>>> {
    return asDynamic().allSettled(ps).unsafeCast<Promise<Array<PromiseOutcome<T>>>>()
}

interface PromiseOutcome<T>

private inline fun <T> Promise<Promise<T>>.flatten(): Promise<T> {
    return then { it }
}

suspend fun <T> CoroutineScope.scope(block: suspend CoroutineScope.() -> T): T {
    var t: T? = null
    async {
        t = block()
    }.await()
    return t.unsafeCast<T>()
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

interface CoroutineScope
private class CoroutineScopeImpl(val children: Array<Promise<*>>) : CoroutineScope
