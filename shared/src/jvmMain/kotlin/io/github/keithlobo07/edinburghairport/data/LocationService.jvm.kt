package io.github.keithlobo07.edinburghairport.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URI

actual class PlatformLocationManager actual constructor() {
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    actual fun startLocationUpdates(onUpdate: (LiveGpsCoordinates) -> Unit, onError: (String) -> Unit) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            val coords = resolveDesktopIpLocation() ?: defaultAirportCoords()
            onUpdate(coords)
            while (isActive) {
                delay(30_000L)
                resolveDesktopIpLocation()?.let(onUpdate)
            }
        }
    }

    actual fun stopLocationUpdates() {
        pollingJob?.cancel()
        pollingJob = null
    }

    actual fun isPermissionGranted(): Boolean = true
    actual fun requestLocationPermission() {}

    private fun resolveDesktopIpLocation(): LiveGpsCoordinates? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URI("http://ip-api.com/json/?fields=status,lat,lon").toURL().openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
            }
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = Json.parseToJsonElement(body).jsonObject
            if (json["status"]?.jsonPrimitive?.content != "success") return null
            val lat = json["lat"]?.jsonPrimitive?.doubleOrNull ?: return null
            val lon = json["lon"]?.jsonPrimitive?.doubleOrNull ?: return null

            val distKm = GpsUtils.calculateDistanceKm(lat, lon, GpsUtils.EDI_AIRPORT_LAT, GpsUtils.EDI_AIRPORT_LON)
            val isNear = distKm <= 2.5
            val (normX, normY) = if (isNear) GpsUtils.mapGpsToTerminal(lat, lon) else Pair(0.50f, 0.70f)
            LiveGpsCoordinates(
                latitude = lat,
                longitude = lon,
                accuracyMeters = 50f,
                provider = "desktop-ip",
                isNearAirport = isNear,
                distanceToAirportKm = ((distKm * 10.0).toLong()) / 10.0,
                normalizedAirportX = normX,
                normalizedAirportY = normY
            )
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun defaultAirportCoords() = LiveGpsCoordinates(
        latitude = GpsUtils.EDI_AIRPORT_LAT,
        longitude = GpsUtils.EDI_AIRPORT_LON,
        accuracyMeters = 10f,
        provider = "desktop-default",
        isNearAirport = true,
        distanceToAirportKm = 0.0,
        normalizedAirportX = 0.50f,
        normalizedAirportY = 0.70f
    )
}
