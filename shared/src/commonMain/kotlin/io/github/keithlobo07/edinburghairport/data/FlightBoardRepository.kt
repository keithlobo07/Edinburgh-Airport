package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightStatus
import io.github.keithlobo07.edinburghairport.ui.BoardFlight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Fetches live flight board data from AeroAPI (FlightAware).
 * Rate-limited: fetch once on creation + manual refresh only. No timer polling.
 */
class FlightBoardRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _departures = MutableStateFlow<List<BoardFlight>>(emptyList())
    val departures: StateFlow<List<BoardFlight>> = _departures.asStateFlow()

    private val _arrivals = MutableStateFlow<List<BoardFlight>>(emptyList())
    val arrivals: StateFlow<List<BoardFlight>> = _arrivals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refresh()
    }

    /** Re-fetches from AeroAPI. Call on manual pull-to-refresh only. */
    fun refresh() {
        scope.launch {
            _isLoading.value = true
            val liveDeps = AeroApiService.fetchDepartures()
            val liveArrs = AeroApiService.fetchArrivals()

            _departures.value = if (liveDeps.isNotEmpty()) {
                liveDeps.sortedByDescending { it.estimatedOut ?: it.scheduledOut ?: "" }
                    .map { it.toDepartureBoard() }
            } else {
                emptyList()
            }

            _arrivals.value = if (liveArrs.isNotEmpty()) {
                liveArrs.sortedByDescending { it.estimatedIn ?: it.scheduledIn ?: "" }
                    .map { it.toArrivalBoard() }
            } else {
                emptyList()
            }

            _isLoading.value = false
        }
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun AeroFlight.toDepartureBoard(): BoardFlight {
    val displayFlight = identIata ?: ident
    val displayDest = (destination?.codeIata ?: "UNK").uppercase()
    val displayTime = (estimatedOut ?: scheduledOut)?.toHhMm() ?: "--:--"
    val displayGate = gateOrigin?.takeIf { it.isNotBlank() }?.let { "GATE ${it.uppercase()}" } ?: "TBA"
    return BoardFlight(
        time = displayTime,
        flight = displayFlight.uppercase(),
        destination = displayDest,
        gate = displayGate,
        status = status.toFlightStatus(isDeparture = true),
        isDeparture = true
    )
}

private fun AeroFlight.toArrivalBoard(): BoardFlight {
    val displayFlight = identIata ?: ident
    val displayOrigin = (origin?.codeIata ?: "UNK").uppercase()
    val displayTime = (estimatedIn ?: scheduledIn)?.toHhMm() ?: "--:--"
    val displayBelt = baggageClaim?.takeIf { it.isNotBlank() }?.let { "CAROUSEL ${it.uppercase()}" }
                      ?: gateDestination?.takeIf { it.isNotBlank() }?.let { "GATE ${it.uppercase()}" }
                      ?: "TBA"
    return BoardFlight(
        time = displayTime,
        flight = displayFlight.uppercase(),
        destination = displayOrigin,
        gate = displayBelt,
        status = status.toFlightStatus(isDeparture = false),
        isDeparture = false
    )
}

/** Parses ISO-8601 datetime string → "HH:MM" local time (best-effort). */
private fun String.toHhMm(): String {
    // Format: 2024-11-23T14:30:00Z or 2024-11-23T14:30:00+01:00
    return try {
        val tIndex = indexOf('T')
        if (tIndex < 0) return this
        val timePart = substring(tIndex + 1)
        val parts = timePart.split(":", "Z", "+", "-").filter { it.isNotEmpty() }
        if (parts.size >= 2) "${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}"
        else this
    } catch (e: Exception) {
        this
    }
}

/** Maps AeroAPI status string → app FlightStatus enum. */
private fun String?.toFlightStatus(isDeparture: Boolean): FlightStatus = when {
    this == null                      -> FlightStatus.ON_TIME
    contains("cancelled", true)       -> FlightStatus.DELAYED
    contains("diverted", true)        -> FlightStatus.DELAYED
    contains("landed", true)          -> if (isDeparture) FlightStatus.DEPARTED else FlightStatus.LANDED
    contains("active", true)          -> if (isDeparture) FlightStatus.DEPARTED else FlightStatus.ON_TIME
    contains("scheduled", true)       -> FlightStatus.ON_TIME
    contains("filed", true)           -> FlightStatus.ON_TIME
    else                              -> FlightStatus.ON_TIME
}


