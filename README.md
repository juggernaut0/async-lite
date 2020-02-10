# Async-Lite

Lightweight, promise-backed coroutine builders for Kotlin/JS

## Why?

Because kotlinx.coroutines, while powerful, can be unwieldy and bloat your JS bundle with unnecessary features.

## Getting Started

Add the dependency to your build.gradle
```groovy
repositories {
    // ...
    maven { url "https://juggernaut0.github.io/m2/repository" }
}
dependencies {
    // ...
    compile "com.github.juggernaut0:async-lite:0.1.0"
}
```

Use `async` to asynchronously start a coroutine and return it as a normal JS Promise. In suspending code, `await` any 
Promise to suspend until it resolves (or rejects).

```kotlin
fun main() {
    val prom: Promise<String> = async {
        // Start two fetches in parallel
        val resp1 = window.fetch("/res1")
        val resp2 = window.fetch("/res2")
        // Await the responses
        val text1 = resp1.await().text().await()
        val text2 = resp2.await().text().await()

        text1 + text2
    }
}
```

Use `delay` as a suspending alternative to `window.setTimeout`.

```kotlin
async {
    delay(8000)
    console.log("Eight seconds have passed")
}
```
