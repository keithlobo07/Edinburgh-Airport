package io.github.keithlobo07.edinburghairport.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class PlatformLocationManager actual constructor() {
    private val locationManager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest
        distanceFilter = 1.0
    }

    private var onUpdateCallback: ((LiveGpsCoordinates) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var delegateRef: LocationDelegate? = null

    actual fun startLocationUpdates(
        onUpdate: (LiveGpsCoordinates) -> Unit,
        onError: (String) -> Unit
    ) {
        onUpdateCallback = onUpdate
        onErrorCallback = onError

        val delegate = LocationDelegate(
            onLocation = { location ->
                val (lat, lon) = location.coordinate.useContents {
                    Pair(latitude, longitude)
                }
                val distKm = GpsUtils.calculateDistanceKm(
                    lat, lon,
                    GpsUtils.EDI_AIRPORT_LAT, GpsUtils.EDI_AIRPORT_LON
                )
                val isNear = distKm <= 2.5
                val (normX, normY) = if (isNear) {
                    GpsUtils.mapGpsToTerminal(lat, lon)
                } else {
                    Pair(0.50f, 0.70f)
                }
                onUpdateCallback?.invoke(
                    LiveGpsCoordinates(
                        latitude = lat,
                        longitude = lon,
                        accuracyMeters = location.horizontalAccuracy.toFloat().coerceAtLeast(0f),
                        altitudeMeters = location.altitude,
                        speedMps = location.speed.toFloat().coerceAtLeast(0f),
                        provider = "CoreLocation",
                        timestamp = (location.timestamp.timeIntervalSince1970 * 1000.0).toLong(),
                        isNearAirport = isNear,
                        distanceToAirportKm = distKm,
                        normalizedAirportX = normX,
                        normalizedAirportY = normY
                    )
                )
            },
            onAuthChange = { status ->
                if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
                ) {
                    locationManager.startUpdatingLocation()
                }
            },
            onFail = { message ->
                onErrorCallback?.invoke(message)
            }
        )

        delegateRef = delegate
        locationManager.delegate = delegate

        val status = locationManager.authorizationStatus
        when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                locationManager.startUpdatingLocation()
            }
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }
            else -> {
                onError("Location permission denied in iOS Settings")
            }
        }
    }

    actual fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        locationManager.delegate = null
        delegateRef = null
        onUpdateCallback = null
        onErrorCallback = null
    }

    actual fun isPermissionGranted(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual fun requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    private class LocationDelegate(
        private val onLocation: (CLLocation) -> Unit,
        private val onAuthChange: (Int) -> Unit,
        private val onFail: (String) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val latest = didUpdateLocations.lastOrNull() as? CLLocation ?: return
            onLocation(latest)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            onAuthChange(manager.authorizationStatus)
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onFail(didFailWithError.localizedDescription)
        }
    }
}
