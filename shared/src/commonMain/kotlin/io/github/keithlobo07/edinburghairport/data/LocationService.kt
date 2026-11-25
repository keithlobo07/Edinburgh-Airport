package io.github.keithlobo07.edinburghairport.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LiveGpsCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val speedMps: Float = 0f,
    val provider: String = "gps",
    val timestamp: Long = 0L,
    val isNearAirport: Boolean = false,
    val distanceToAirportKm: Double = 0.0,
    val normalizedAirportX: Float = 0.50f,
    val normalizedAirportY: Float = 0.70f
) {
    val displayCoords: String
        get() = "${formatCoord(latitude, true)}, ${formatCoord(longitude, false)}"

    private fun formatCoord(value: Double, isLat: Boolean): String {
        val absVal = if (value < 0) -value else value
        val dir = if (isLat) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        val rounded = ((absVal * 10000.0).toLong()) / 10000.0
        return "$rounded° $dir"
    }
}

object GpsUtils {
    const val EDI_AIRPORT_LAT = 55.9500
    const val EDI_AIRPORT_LON = -3.3615

    // Calibrated terminal bounding box matching the official terminal footprint
    const val MIN_LON = -3.3665 // West: Gates 1-6
    const val MAX_LON = -3.3575 // East: Gates 13-30
    const val MIN_LAT = 55.9485 // South: Security & Duty Free entrance
    const val MAX_LAT = 55.9520 // North: Gates 6-11 along the apron

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = (lat2 - lat1) * (kotlin.math.PI / 180.0)
        val dLon = (lon2 - lon1) * (kotlin.math.PI / 180.0)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * (kotlin.math.PI / 180.0)) * cos(lat2 * (kotlin.math.PI / 180.0)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun mapGpsToTerminal(lat: Double, lon: Double): Pair<Float, Float> {
        val normX = ((lon - MIN_LON) / (MAX_LON - MIN_LON)).toFloat().coerceIn(0.08f, 0.92f)
        val normY = ((MAX_LAT - lat) / (MAX_LAT - MIN_LAT)).toFloat().coerceIn(0.12f, 0.88f)
        return Pair(normX, normY)
    }
}

expect class PlatformLocationManager() {
    fun startLocationUpdates(onUpdate: (LiveGpsCoordinates) -> Unit, onError: (String) -> Unit)
    fun stopLocationUpdates()
    fun isPermissionGranted(): Boolean
    fun requestLocationPermission()
}
