package io.github.keithlobo07.edinburghairport.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

actual class PlatformLocationManager actual constructor() {
    private var locationListener: LocationListener? = null

    actual fun isPermissionGranted(): Boolean {
        val context = AndroidContextHolder.context ?: return false
        val fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    actual fun requestLocationPermission() {
        val activity = AndroidContextHolder.activity ?: return
        activity.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdates(
        onUpdate: (LiveGpsCoordinates) -> Unit,
        onError: (String) -> Unit
    ) {
        val context = AndroidContextHolder.context
        if (context == null) {
            onError("Android Context not available")
            return
        }

        if (!isPermissionGranted()) {
            onError("Location permission not granted")
            requestLocationPermission()
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onError("Location service not found")
            return
        }

        // Query last known location immediately
        try {
            val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = listOfNotNull(gpsLoc, netLoc).maxByOrNull { it.time }
            if (best != null) {
                onUpdate(createGpsCoords(best))
            }
        } catch (e: SecurityException) {
            // Handled
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                onUpdate(createGpsCoords(loc))
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                onError("Location provider $provider is disabled")
            }
        }
        locationListener = listener

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L,
                    0.0f,
                    listener,
                    Looper.getMainLooper()
                )
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0.0f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            onError("Security Exception: ${e.message}")
        } catch (e: Exception) {
            onError("Error starting GPS: ${e.message}")
        }
    }

    actual fun stopLocationUpdates() {
        val context = AndroidContextHolder.context ?: return
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        locationListener?.let {
            try {
                lm.removeUpdates(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        locationListener = null
    }

    private fun createGpsCoords(loc: Location): LiveGpsCoordinates {
        val distKm = GpsUtils.calculateDistanceKm(
            loc.latitude, loc.longitude,
            GpsUtils.EDI_AIRPORT_LAT, GpsUtils.EDI_AIRPORT_LON
        )
        val isNear = distKm <= 2.5
        val (normX, normY) = if (isNear) {
            GpsUtils.mapGpsToTerminal(loc.latitude, loc.longitude)
        } else {
            Pair(0.50f, 0.70f)
        }

        return LiveGpsCoordinates(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracyMeters = loc.accuracy,
            altitudeMeters = loc.altitude,
            speedMps = loc.speed,
            provider = loc.provider ?: "gps",
            timestamp = loc.time,
            isNearAirport = isNear,
            distanceToAirportKm = ((distKm * 10.0).toLong()) / 10.0,
            normalizedAirportX = normX,
            normalizedAirportY = normY
        )
    }
}
