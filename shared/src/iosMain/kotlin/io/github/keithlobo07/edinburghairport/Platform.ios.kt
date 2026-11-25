package io.github.keithlobo07.edinburghairport

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
import platform.Foundation.timeZoneWithName
import kotlin.coroutines.resume

actual fun getEdinburghCurrentTime(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "HH:mm"
        timeZone = NSTimeZone.timeZoneWithName("Europe/London") ?: NSTimeZone.timeZoneWithName("UTC")!!
    }
    return formatter.stringFromDate(NSDate())
}

actual fun getAeroApiTimeRange(): Pair<String, String> {
    val now = NSDate()
    val start = NSDate(timeIntervalSinceReferenceDate = now.timeIntervalSinceReferenceDate - 3.0 * 3600.0)
    val end = NSDate(timeIntervalSinceReferenceDate = now.timeIntervalSinceReferenceDate + 2.0 * 3600.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    }
    return Pair(formatter.stringFromDate(start), formatter.stringFromDate(end))
}

@OptIn(BetaInteropApi::class)
actual suspend fun fetchHttpUrl(url: String): String? = suspendCancellableCoroutine { cont ->
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
        if (cont.isActive) cont.resume(null)
        return@suspendCancellableCoroutine
    }

    val task = NSURLSession.sharedSession.dataTaskWithURL(nsUrl) { data, response, error ->
        if (!cont.isActive) return@dataTaskWithURL
        if (error != null || data == null) {
            cont.resume(null)
            return@dataTaskWithURL
        }
        val httpResp = response as? NSHTTPURLResponse
        val status = httpResp?.statusCode ?: 200L
        if (status in 200L..299L) {
            val text = NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            cont.resume(text)
        } else {
            cont.resume(null)
        }
    }

    cont.invokeOnCancellation {
        task.cancel()
    }
    task.resume()
}

actual object PlatformStorage {
    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    actual fun save(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun load(key: String): String? {
        return defaults.stringForKey(key)
    }
}
