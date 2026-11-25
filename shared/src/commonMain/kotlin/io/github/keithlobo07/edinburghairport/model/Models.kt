package io.github.keithlobo07.edinburghairport.model

import androidx.compose.ui.graphics.Color
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.StatusRed
import org.jetbrains.compose.resources.DrawableResource

enum class FlightStateMode {
    NO_FLIGHT,        // State A - Search bar + scan boarding pass
    ACTIVE_DEPARTURE, // State B - Departure card (Gate, Bag drop, walking time)
    ACTIVE_ARRIVAL    // State C - Arrival card (Carousel, estimated bag time)
}

enum class AppProfile(val title: String, val subtitle: String) {
    NO_FLIGHT("No Flight", "Passenger without an active flight"),
    DEPARTURE("Departure", "Passenger with an active departure"),
    ARRIVAL("Arrival", "Passenger with an active arrival"),
    ADMIN("Admin", "All testing and simulation tools")
}

enum class FlightStatus(val label: String, val color: Color) {
    ON_TIME("On Time", StatusGreen),
    GATE_OPEN("Gate Open", StatusAmber),
    BOARDING("Boarding", StatusAmber),
    GATE_CLOSING("Gate Closing", StatusRed),
    DEPARTED("Departed", Color(0xFF64748B)),
    DELAYED("Delayed", StatusRed),
    LANDED("Landed", StatusGreen),
    BAGGAGE_READY("Baggage Ready", StatusGreen)
}

data class FlightInfo(
    val flightNumber: String,
    val airline: String,
    val destination: String,
    val destinationCode: String,
    val origin: String,
    val originCode: String,
    val scheduledTime: String,
    val estimatedTime: String,
    val status: FlightStatus,
    val statusText: String,
    val checkInDesk: String,
    val gate: String?,
    val walkingTime: String?,
    val baggageCarousel: String? = null,
    val baggageEstimatedTime: String? = null,
    val terminal: String = "Main Terminal",
    val isDeparture: Boolean = true,
    val isCached: Boolean = true,
    val lastUpdated: String = "2 mins ago",
    val passengerName: String = "Keith Lobo",
    val seat: String = "12A",
    val boardingGroup: String = "Group 2",
    val zone: String = "Zone B",
    val bookingReference: String = "AB12CD",
    val cabinClass: String = "Economy Class",
    val boardingIndex: String = "0001",
    val flightDate: String = "24/11/2026"
)

enum class StatusLevel(val color: Color) {
    GREEN(StatusGreen),
    AMBER(StatusAmber),
    RED(StatusRed)
}

data class LiveOpsData(
    val securityWaitMinutes: Int = 5,
    val securityStatusLevel: StatusLevel = StatusLevel.GREEN,
    val securityLabel: String = "security hall time",
    val fastTrackPrice: String = "£10.00",
    
    val tramNextMinutes: Int = 8,
    val tramIntervalMinutes: Int = 8,
    val tramStatusLevel: StatusLevel = StatusLevel.AMBER,
    val tramLabel: String = "Next tram",
    val tramTicketTitle: String = "Buy Tram Airport Single",
    val tramPrice: String = "£7.90",
    
    val busNextMinutes: Int = 12,
    val busIntervalMinutes: Int = 12,
    val busStatusLevel: StatusLevel = StatusLevel.RED,
    val busLabel: String = "Next bus",
    val busTicketTitle: String = "Buy Airlink 100 ticket",
    val busPrice: String = "£6.00"
)

data class VisitorAttraction(
    val id: String,
    val title: String,
    val shortDescription: String,
    val websiteUrl: String,
    val category: String,
    val durationFromAirport: String,
    val imageResource: DrawableResource? = null,
    val accentColorHex: Long = 0xFF752969
)

enum class QuickAccessType(val title: String, val subtitle: String) {
    TERMINAL_MAP("Terminal Map", "Interactive 3D Wayfinding"),
    PARKING("Parking", "Pre-book & Find My Car"),
    SHOP_AND_DINE("Shops & Dining", "Restaurants & Stores"),
    LOUNGE_ACCESS("Lounge Access", "Live Capacity & Booking")
}

enum class HomeNavigationTab(val label: String) {
    MY_TRIP("My Trip"),
    FLIGHTS("Flights"),
    HOME("Home"),
    EXPLORE("Explore"),
    MAP("Map")
}

data class FlightAlert(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)

enum class PassType(val title: String) {
    FLIGHT("Boarding Pass"),
    TRANSIT("Transit Ticket"),
    PARKING("Parking Pass"),
    FAST_TRACK("Fast-Track Security")
}

@kotlinx.serialization.Serializable
data class AirportDigitalPass(
    val id: String,
    val type: PassType,
    val title: String,
    val subtitle: String,
    val reference: String,
    val barcodeValue: String,
    val primaryFieldLabel: String,
    val primaryFieldValue: String,
    val secondaryFieldLabel: String,
    val secondaryFieldValue: String,
    val auxiliaryFieldLabel: String,
    val auxiliaryFieldValue: String,
    val priceFormatted: String,
    val validUntil: String,
    val status: String = "ACTIVE & READY"
)
