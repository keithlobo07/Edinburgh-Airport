package io.github.keithlobo07.edinburghairport.data

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class PlatformLocationManager actual constructor() {
    private var watchId: Int? = null

    actual fun startLocationUpdates(onUpdate: (LiveGpsCoordinates) -> Unit, onError: (String) -> Unit) {
        val geo: dynamic = js("navigator.geolocation")
        if (geo == null || js("typeof navigator.geolocation === 'undefined'") as Boolean) {
            onError("Browser geolocation unavailable")
            return
        }
        val options: dynamic = js("({ enableHighAccuracy: true, maximumAge: 5000, timeout: 10000 })")
        watchId = geo.watchPosition(
            { pos: dynamic ->
                val lat = (pos.coords.latitude as Number).toDouble()
                val lon = (pos.coords.longitude as Number).toDouble()
                val acc = (pos.coords.accuracy as? Number)?.toFloat() ?: 15f
                val distKm = GpsUtils.calculateDistanceKm(lat, lon, GpsUtils.EDI_AIRPORT_LAT, GpsUtils.EDI_AIRPORT_LON)
                val isNear = distKm <= 2.5
                val (normX, normY) = if (isNear) GpsUtils.mapGpsToTerminal(lat, lon) else Pair(0.50f, 0.70f)
                onUpdate(
                    LiveGpsCoordinates(
                        latitude = lat,
                        longitude = lon,
                        accuracyMeters = acc,
                        provider = "web-geolocation",
                        isNearAirport = isNear,
                        distanceToAirportKm = ((distKm * 10.0).toLong()) / 10.0,
                        normalizedAirportX = normX,
                        normalizedAirportY = normY
                    )
                )
            },
            { err: dynamic ->
                onError(err?.message?.toString() ?: "Location permission denied")
            },
            options
        ) as? Int
    }

    actual fun stopLocationUpdates() {
        val id = watchId ?: return
        js("if (navigator.geolocation) navigator.geolocation.clearWatch(id)")
        watchId = null
    }

    actual fun isPermissionGranted(): Boolean = js("'geolocation' in navigator") as Boolean
    actual fun requestLocationPermission() {}
}
