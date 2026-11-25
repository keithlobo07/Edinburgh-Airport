@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.keithlobo07.edinburghairport

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny

@JsFun("() => new Date().toLocaleTimeString('en-GB', { timeZone: 'Europe/London', hour: '2-digit', minute: '2-digit' })")
private external fun currentEdinburghTimeJs(): String

@JsFun("(offsetHours) => new Date(Date.now() + offsetHours * 60 * 60 * 1000).toISOString().replace('.000Z', 'Z')")
private external fun aeroApiIsoUtc(offsetHours: Int): String

@JsFun("(url, onDone) => { fetch(url).then(r => r.ok ? r.text() : null).then(t => onDone(t)).catch(() => onDone(null)); }")
private external fun fetchTextJs(url: String, onDone: (String?) -> Unit)

actual fun getEdinburghCurrentTime(): String = currentEdinburghTimeJs()

actual fun getAeroApiTimeRange(): Pair<String, String> {
    return Pair(aeroApiIsoUtc(-3), aeroApiIsoUtc(2))
}

actual suspend fun fetchHttpUrl(url: String): String? = suspendCoroutine { cont ->
    try {
        fetchTextJs(url) { result -> cont.resume(result) }
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
