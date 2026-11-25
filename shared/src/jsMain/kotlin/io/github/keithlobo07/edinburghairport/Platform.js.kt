package io.github.keithlobo07.edinburghairport

import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual fun getEdinburghCurrentTime(): String {
    val d = js("new Date().toLocaleTimeString('en-GB', { timeZone: 'Europe/London', hour: '2-digit', minute: '2-digit' })")
    return d.toString()
}

actual fun getAeroApiTimeRange(): Pair<String, String> {
    val start = js("new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString().replace('.000Z', 'Z')")
    val end = js("new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString().replace('.000Z', 'Z')")
    return Pair(start.toString(), end.toString())
}

actual suspend fun fetchHttpUrl(url: String): String? = suspendCoroutine { cont ->
    try {
        window.fetch(url, RequestInit()).then(
            onFulfilled = { resp ->
                if (resp.ok) {
                    resp.text().then(
                        onFulfilled = { text -> cont.resume(text) },
                        onRejected = { cont.resume(null) }
                    )
                } else {
                    cont.resume(null)
                }
            },
            onRejected = { cont.resume(null) }
        )
    } catch (_: Throwable) {
        cont.resume(null)
    }
}

actual object PlatformStorage {
    actual fun save(key: String, value: String) {
        try { window.localStorage.setItem(key, value) } catch (_: Exception) {}
    }
    actual fun load(key: String): String? {
        return try { window.localStorage.getItem(key) } catch (_: Exception) { null }
    }
}
