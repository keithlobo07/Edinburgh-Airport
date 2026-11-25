package io.github.keithlobo07.edinburghairport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.Calendar
import java.util.TimeZone
import java.util.prefs.Preferences

actual fun getEdinburghCurrentTime(): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"))
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

actual fun getAeroApiTimeRange(): Pair<String, String> {
    val calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.HOUR_OF_DAY, -3)
    }
    val calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.HOUR_OF_DAY, 2)
    }
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return Pair(sdf.format(calStart.time), sdf.format(calEnd.time))
}

actual suspend fun fetchHttpUrl(url: String): String? = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        val u = URI(url).toURL()
        connection = u.openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "EdinburghAirportApp/1.0")
        if (connection.responseCode in 200..299) {
            BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    } finally {
        connection?.disconnect()
    }
}

actual object PlatformStorage {
    private val prefs = Preferences.userNodeForPackage(PlatformStorage::class.java)
    actual fun save(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }
    actual fun load(key: String): String? {
        return prefs.get(key, null as String?)
    }
}
