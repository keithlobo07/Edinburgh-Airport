@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.keithlobo07.edinburghairport.data

@JsFun("(onPos, onErr) => { if (!navigator.geolocation) { onErr('Browser geolocation unavailable'); return -1; } return navigator.geolocation.watchPosition(p => onPos(p.coords.latitude, p.coords.longitude, p.coords.accuracy || 15), e => onErr(e.message || 'Location denied'), { enableHighAccuracy: true, maximumAge: 5000, timeout: 10000 }); }")
private external fun wasmWatchPosition(
    onPos: (Double, Double, Float) -> Unit,
    onErr: (String) -> Unit
): Int

@JsFun("(id) => { if (id >= 0 && navigator.geolocation) navigator.geolocation.clearWatch(id); }")
private external fun wasmClearWatch(id: Int)

@JsFun("() => 'geolocation' in navigator")
private external fun wasmHasGeolocation(): Boolean

actual class PlatformLocationManager actual constructor() {
    private var watchId: Int = -1

    actual fun startLocationUpdates(onUpdate: (LiveGpsCoordinates) -> Unit, onError: (String) -> Unit) {
        stopLocationUpdates()
        watchId = wasmWatchPosition(
            onPos = { lat, lon, acc ->
                val distKm = GpsUtils.calculateDistanceKm(lat, lon, GpsUtils.EDI_AIRPORT_LAT, GpsUtils.EDI_AIRPORT_LON)
                val isNear = distKm <= 2.5
                val (normX, normY) = if (isNear) GpsUtils.mapGpsToTerminal(lat, lon) else Pair(0.50f, 0.70f)
                onUpdate(
                    LiveGpsCoordinates(
                        latitude = lat,
                        longitude = lon,
                        accuracyMeters = acc,
                        provider = "wasm-geolocation",
                        isNearAirport = isNear,
                        distanceToAirportKm = ((distKm * 10.0).toLong()) / 10.0,
                        normalizedAirportX = normX,
                        normalizedAirportY = normY
                    )
                )
            },
            onErr = onError
        )
    }

    actual fun stopLocationUpdates() {
        if (watchId >= 0) {
            wasmClearWatch(watchId)
            watchId = -1
        }
    }

    actual fun isPermissionGranted(): Boolean = wasmHasGeolocation()
    actual fun requestLocationPermission() {}
}
