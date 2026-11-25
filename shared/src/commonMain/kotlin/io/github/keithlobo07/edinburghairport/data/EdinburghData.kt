package io.github.keithlobo07.edinburghairport.data

import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.img_arthurs_seat
import edinburghairport.shared.generated.resources.img_christmas_market
import edinburghairport.shared.generated.resources.img_edinburgh_castle
import edinburghairport.shared.generated.resources.img_holyroodhouse
import edinburghairport.shared.generated.resources.img_national_museum
import edinburghairport.shared.generated.resources.img_royal_mile
import io.github.keithlobo07.edinburghairport.model.FlightAlert
import io.github.keithlobo07.edinburghairport.model.FlightInfo
import io.github.keithlobo07.edinburghairport.model.FlightStateMode
import io.github.keithlobo07.edinburghairport.model.FlightStatus
import io.github.keithlobo07.edinburghairport.model.AppProfile
import io.github.keithlobo07.edinburghairport.model.LiveOpsData
import io.github.keithlobo07.edinburghairport.PlatformStorage
import io.github.keithlobo07.edinburghairport.getEdinburghCurrentTime
import io.github.keithlobo07.edinburghairport.model.StatusLevel
import io.github.keithlobo07.edinburghairport.model.VisitorAttraction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.github.keithlobo07.edinburghairport.model.AirportDigitalPass
import io.github.keithlobo07.edinburghairport.model.PassType

object PurchasedPassStorage {
    private const val FAST_TRACK_KEY = "edinburgh_purchased_fast_track_v1"
    private const val TRAM_KEY = "edinburgh_purchased_tram_v1"
    private const val BUS_KEY = "edinburgh_purchased_bus_v1"
    private const val PARKING_KEY = "edinburgh_purchased_parking_v1"
    private const val DIGITAL_PASSES_KEY = "edinburgh_digital_passes_v2"
    private const val PREP_CHECKLIST_KEY = "edinburgh_prep_checklist_v1"

    private val json = Json { ignoreUnknownKeys = true }

    fun isFastTrackPurchased(): Boolean {
        if (!PlatformStorage.load(FAST_TRACK_KEY).isNullOrBlank()) return true
        return loadAllDigitalPasses().any { it.type == PassType.FAST_TRACK }
    }
    fun setFastTrackPurchased(purchased: Boolean) {
        if (purchased) {
            PlatformStorage.save(FAST_TRACK_KEY, "true")
            ensureFastTrackDigitalPass()
        } else {
            PlatformStorage.save(FAST_TRACK_KEY, "")
            removeDigitalPass("FAST_TRACK_EDI_FT_94821")
        }
    }

    fun ensureFastTrackDigitalPass() {
        if (PlatformStorage.load(FAST_TRACK_KEY).isNullOrBlank()) return
        val existing = loadAllDigitalPasses()
        if (existing.any { it.type == PassType.FAST_TRACK }) return
        saveDigitalPass(
            AirportDigitalPass(
                id = "FAST_TRACK_EDI_FT_94821",
                type = PassType.FAST_TRACK,
                title = "Security Fast-Track Pass",
                subtitle = "Priority Lane Direct to X-Ray",
                reference = "EDI-FT-94821",
                barcodeValue = "EDI-FT-94821",
                primaryFieldLabel = "ACCESS LANE",
                primaryFieldValue = "Lane 1 Priority Gate",
                secondaryFieldLabel = "PASS TYPE",
                secondaryFieldValue = "1x Adult Priority",
                auxiliaryFieldLabel = "OPERATING TIME",
                auxiliaryFieldValue = "04:00 - 23:00 Today",
                priceFormatted = "£10.00",
                validUntil = "Valid Today",
                status = "ACTIVE & READY"
            )
        )
    }

    fun loadTransportReference(key: String): String? = when (key) {
        "TRAM" -> PlatformStorage.load(TRAM_KEY)?.takeIf { it.isNotBlank() }
        "BUS" -> PlatformStorage.load(BUS_KEY)?.takeIf { it.isNotBlank() }
        else -> null
    }

    fun saveTransportReference(key: String, reference: String) {
        when (key) {
            "TRAM" -> PlatformStorage.save(TRAM_KEY, reference)
            "BUS" -> PlatformStorage.save(BUS_KEY, reference)
        }
    }

    fun loadParkingReference(): String? =
        PlatformStorage.load(PARKING_KEY)?.takeIf { it.isNotBlank() }
    fun saveParkingReference(reference: String) = PlatformStorage.save(PARKING_KEY, reference)

    fun loadAllDigitalPasses(): List<AirportDigitalPass> {
        val raw = PlatformStorage.load(DIGITAL_PASSES_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<AirportDigitalPass>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveDigitalPass(pass: AirportDigitalPass) {
        val existing = loadAllDigitalPasses().toMutableList()
        existing.removeAll { it.id == pass.id }
        existing.add(0, pass)
        try {
            val encoded = json.encodeToString(existing)
            PlatformStorage.save(DIGITAL_PASSES_KEY, encoded)
        } catch (_: Exception) {}
    }

    fun removeDigitalPass(id: String) {
        val existing = loadAllDigitalPasses().toMutableList()
        existing.removeAll { it.id == id }
        if (id == "FAST_TRACK_EDI_FT_94821") {
            PlatformStorage.save(FAST_TRACK_KEY, "")
        }
        if (id.startsWith("TRANSIT_")) {
            val transitPass = loadAllDigitalPasses().firstOrNull { it.id == id }
            if (transitPass?.title?.contains("Tram", ignoreCase = true) == true) {
                PlatformStorage.save(TRAM_KEY, "")
            } else if (transitPass?.title?.contains("Airlink", ignoreCase = true) == true) {
                PlatformStorage.save(BUS_KEY, "")
            }
        }
        if (id.startsWith("PARK_")) {
            PlatformStorage.save(PARKING_KEY, "")
        }
        try {
            val encoded = json.encodeToString(existing)
            PlatformStorage.save(DIGITAL_PASSES_KEY, encoded)
        } catch (_: Exception) {}
    }

    fun loadPrepChecklist(): Set<String> {
        val raw = PlatformStorage.load(PREP_CHECKLIST_KEY) ?: return setOf("check-in", "liquids")
        return try {
            json.decodeFromString<List<String>>(raw).toSet()
        } catch (_: Exception) {
            setOf("check-in", "liquids")
        }
    }

    fun savePrepChecklist(completedIds: Set<String>) {
        try {
            PlatformStorage.save(PREP_CHECKLIST_KEY, json.encodeToString(completedIds.toList()))
        } catch (_: Exception) {}
    }
}

object EdinburghData {

    val sampleActiveDeparture = FlightInfo(
        flightNumber = "BA 1445",
        airline = "British Airways",
        destination = "London Heathrow",
        destinationCode = "LHR",
        origin = "Edinburgh",
        originCode = "EDI",
        scheduledTime = "18:45",
        estimatedTime = "18:50",
        status = FlightStatus.GATE_OPEN,
        statusText = "Gate Open",
        checkInDesk = "Desks 12-18 (Zone B)",
        gate = "Gate 14",
        walkingTime = "6 min walk",
        terminal = "Main Terminal",
        isDeparture = true,
        isCached = true,
        lastUpdated = "1 min ago",
        passengerName = "Alexander MacLeod",
        seat = "14A",
        boardingGroup = "Group 1",
        zone = "Zone B"
    )

    val sampleActiveArrival = FlightInfo(
        flightNumber = "FR 6621",
        airline = "Ryanair",
        destination = "Edinburgh",
        destinationCode = "EDI",
        origin = "Dublin",
        originCode = "DUB",
        scheduledTime = "16:00",
        estimatedTime = "16:05",
        status = FlightStatus.LANDED,
        statusText = "Landed 16:05",
        checkInDesk = "Domestic/CTA Arrivals",
        gate = "Stand 24",
        walkingTime = "4 min walk to Border",
        baggageCarousel = "Carousel 3",
        baggageEstimatedTime = "First bags: ~16:22",
        terminal = "Arrivals Hall",
        isDeparture = false,
        isCached = true,
        lastUpdated = "Just now",
        passengerName = "Siobhán O'Connor",
        seat = "22F",
        boardingGroup = "Group 3",
        zone = "Zone A"
    )

    val searchableFlights = listOf(
        sampleActiveDeparture,
        FlightInfo(
            flightNumber = "EZY 312",
            airline = "easyJet",
            destination = "Amsterdam Schiphol",
            destinationCode = "AMS",
            origin = "Edinburgh",
            originCode = "EDI",
            scheduledTime = "19:15",
            estimatedTime = "19:15",
            status = FlightStatus.ON_TIME,
            statusText = "On Time",
            checkInDesk = "Desks 24-29 (Zone C)",
            gate = "Gate 21",
            walkingTime = "8 min walk",
            isDeparture = true,
            passengerName = "Emma Watson",
            seat = "08C",
            boardingGroup = "Group 2",
            zone = "Zone C"
        ),
        FlightInfo(
            flightNumber = "KL 1280",
            airline = "KLM",
            destination = "Amsterdam Schiphol",
            destinationCode = "AMS",
            origin = "Edinburgh",
            originCode = "EDI",
            scheduledTime = "17:30",
            estimatedTime = "17:45",
            status = FlightStatus.BOARDING,
            statusText = "Boarding Now",
            checkInDesk = "Desks 8-11 (Zone A)",
            gate = "Gate 10",
            walkingTime = "5 min walk",
            isDeparture = true,
            passengerName = "Jan de Vries",
            seat = "04F",
            boardingGroup = "Group 1",
            zone = "Zone A"
        ),
        FlightInfo(
            flightNumber = "LM 321",
            airline = "Loganair",
            destination = "Stornoway Isle of Lewis",
            destinationCode = "SYY",
            origin = "Edinburgh",
            originCode = "EDI",
            scheduledTime = "19:40",
            estimatedTime = "19:40",
            status = FlightStatus.ON_TIME,
            statusText = "On Time",
            checkInDesk = "Desks 1-4 (Zone A)",
            gate = "Gate 1A",
            walkingTime = "3 min walk",
            isDeparture = true,
            passengerName = "Callum Campbell",
            seat = "11B",
            boardingGroup = "Group 2",
            zone = "Zone A"
        ),
        FlightInfo(
            flightNumber = "QR 30",
            airline = "Qatar Airways",
            destination = "Doha Hamad",
            destinationCode = "DOH",
            origin = "Edinburgh",
            originCode = "EDI",
            scheduledTime = "20:15",
            estimatedTime = "20:15",
            status = FlightStatus.ON_TIME,
            statusText = "On Time",
            checkInDesk = "Desks 30-36 (Zone D)",
            gate = "Gate 17",
            walkingTime = "7 min walk",
            isDeparture = true,
            passengerName = "Fatima Al-Maktoum",
            seat = "02K",
            boardingGroup = "Group 1",
            zone = "Zone D"
        ),
        sampleActiveArrival,
        FlightInfo(
            flightNumber = "UA 37",
            airline = "United Airlines",
            destination = "Edinburgh",
            destinationCode = "EDI",
            origin = "New York Newark",
            originCode = "EWR",
            scheduledTime = "07:45",
            estimatedTime = "07:35",
            status = FlightStatus.BAGGAGE_READY,
            statusText = "Baggage on Carousel",
            checkInDesk = "International Arrivals",
            gate = "Stand 16",
            walkingTime = "5 min walk",
            baggageCarousel = "Carousel 5",
            baggageEstimatedTime = "Delivery Complete",
            terminal = "Arrivals Hall",
            isDeparture = false,
            passengerName = "Marcus Vance",
            seat = "31H",
            boardingGroup = "Group 4",
            zone = "Zone E"
        )
    )

    val visitorAttractions = listOf(
        VisitorAttraction(
            id = "castle",
            title = "Edinburgh Castle",
            shortDescription = "Edinburgh Castle is one of the most exciting historic sites in Europe. Set in the heart of Scotland's dynamic capital city, it will capture your imagination.",
            websiteUrl = "https://www.edinburghcastle.scot",
            category = "Historic Fortress",
            durationFromAirport = "35 mins via Edinburgh Tram",
            imageResource = Res.drawable.img_edinburgh_castle,
            accentColorHex = 0xFF752969
        ),
        VisitorAttraction(
            id = "museum",
            title = "National Museum of Scotland",
            shortDescription = "Thousands of amazing objects across dozens of galleries, and a world of discoveries under one roof. Explore the natural world, world cultures, science, and Scottish history.",
            websiteUrl = "https://www.nms.ac.uk/national-museum-of-scotland",
            category = "Museum & Heritage",
            durationFromAirport = "40 mins via Airlink 100",
            imageResource = Res.drawable.img_national_museum,
            accentColorHex = 0xFF5A1F51
        ),
        VisitorAttraction(
            id = "christmas_market",
            title = "Edinburgh Christmas Market",
            shortDescription = "World-famous festive celebration set in East Princes Street Gardens featuring illuminated big wheel, artisan craft stalls, and Scottish festive food & mulled drinks.",
            websiteUrl = "https://www.edinburghwinterfestivals.com/",
            category = "Festive Wonder",
            durationFromAirport = "30 mins direct via Tram to Princes St",
            imageResource = Res.drawable.img_christmas_market,
            accentColorHex = 0xFF752969
        ),
        VisitorAttraction(
            id = "holyrood",
            title = "Palace of Holyroodhouse",
            shortDescription = "The official Scottish residence of the monarch, located at the end of the historic Royal Mile against the backdrop of Salisbury Crags.",
            websiteUrl = "https://www.rct.uk/visit/palace-of-holyroodhouse",
            category = "Royal Palace",
            durationFromAirport = "42 mins via Edinburgh Tram",
            imageResource = Res.drawable.img_holyroodhouse,
            accentColorHex = 0xFF052145
        ),
        VisitorAttraction(
            id = "arthurs_seat",
            title = "Arthur's Seat & Holyrood Park",
            shortDescription = "An ancient extinct volcano soaring 251m above the city skyline, offering breathtaking 360-degree panoramic views of Edinburgh and the Firth of Forth.",
            websiteUrl = "https://www.historicenvironment.scot/visit-a-place/places/holyrood-park/",
            category = "Natural Landmark",
            durationFromAirport = "45 mins via Public Transport",
            imageResource = Res.drawable.img_arthurs_seat,
            accentColorHex = 0xFF10B981
        ),
        VisitorAttraction(
            id = "royal_mile",
            title = "The Royal Mile & Old Town",
            shortDescription = "The historic heart of Edinburgh connecting the Castle to the Palace, lined with medieval tenements, atmospheric closes, and historic pubs.",
            websiteUrl = "https://edinburgh.org/discover/edinburgh-neighbourhoods/old-town/",
            category = "Historic Walk",
            durationFromAirport = "35 mins via Airlink 100",
            imageResource = Res.drawable.img_royal_mile,
            accentColorHex = 0xFF052145
        )
    )

    val sampleAlerts = listOf(
        FlightAlert(
            id = "alt-1",
            title = "Gate Announced: Gate 14",
            message = "Flight BA 1445 is now boarding at Gate 14. Estimated walk time: 6 minutes.",
            timestamp = "3 mins ago",
            isUrgent = true
        ),
        FlightAlert(
            id = "alt-2",
            title = "Security Queue Update",
            message = "Security Hall queue is currently 5 minutes. Security running smoothly.",
            timestamp = "12 mins ago",
            isUrgent = false
        ),
        FlightAlert(
            id = "alt-3",
            title = "Next Tram Departing",
            message = "Edinburgh Tram to St Andrew Square / Newhaven departs in 8 minutes from the terminal plaza.",
            timestamp = "18 mins ago",
            isUrgent = false
        )
    )
}

@Serializable
data class SerializableFlight(
    val flightNumber: String,
    val airline: String,
    val destination: String,
    val destinationCode: String,
    val origin: String,
    val originCode: String,
    val scheduledTime: String,
    val estimatedTime: String,
    val statusName: String,
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

fun FlightInfo.toSerializable() = SerializableFlight(
    flightNumber = flightNumber,
    airline = airline,
    destination = destination,
    destinationCode = destinationCode,
    origin = origin,
    originCode = originCode,
    scheduledTime = scheduledTime,
    estimatedTime = estimatedTime,
    statusName = status.name,
    statusText = statusText,
    checkInDesk = checkInDesk,
    gate = gate,
    walkingTime = walkingTime,
    baggageCarousel = baggageCarousel,
    baggageEstimatedTime = baggageEstimatedTime,
    terminal = terminal,
    isDeparture = isDeparture,
    isCached = isCached,
    lastUpdated = lastUpdated,
    passengerName = passengerName,
    seat = seat,
    boardingGroup = boardingGroup,
    zone = zone,
    bookingReference = bookingReference,
    cabinClass = cabinClass,
    boardingIndex = boardingIndex,
    flightDate = flightDate
)

fun SerializableFlight.toFlightInfo() = FlightInfo(
    flightNumber = flightNumber,
    airline = airline,
    destination = destination,
    destinationCode = destinationCode,
    origin = origin,
    originCode = originCode,
    scheduledTime = scheduledTime,
    estimatedTime = estimatedTime,
    status = try { FlightStatus.valueOf(statusName) } catch (e: Exception) { FlightStatus.ON_TIME },
    statusText = statusText,
    checkInDesk = checkInDesk,
    gate = gate,
    walkingTime = walkingTime,
    baggageCarousel = baggageCarousel,
    baggageEstimatedTime = baggageEstimatedTime,
    terminal = terminal,
    isDeparture = isDeparture,
    isCached = isCached,
    lastUpdated = lastUpdated,
    passengerName = passengerName,
    seat = seat,
    boardingGroup = boardingGroup,
    zone = zone,
    bookingReference = bookingReference,
    cabinClass = cabinClass,
    boardingIndex = boardingIndex,
    flightDate = flightDate
)

fun FlightInfo.passIdentity(): String =
    listOf(
        passengerName.trim().uppercase(),
        bookingReference.trim().uppercase(),
        flightNumber.trim().uppercase(),
        originCode.trim().uppercase(),
        destinationCode.trim().uppercase(),
        flightDate.trim(),
        scheduledTime.trim(),
        seat.trim().uppercase(),
        cabinClass.trim().uppercase(),
        boardingIndex.trim(),
        boardingGroup.trim().uppercase(),
        zone.trim().uppercase()
    ).joinToString("|")

class FlightRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val storageKey = "edinburgh_stored_passes_v1"
    private val appProfileKey = "edinburgh_app_profile_v1"
    private val dismissedAlertsKey = "edinburgh_dismissed_alerts_v1"
    private val readAlertsKey = "edinburgh_read_alerts_v1"
    private val dismissedAlertIds = loadStringSet(dismissedAlertsKey).toMutableSet()
    private val readAlertIds = loadStringSet(readAlertsKey).toMutableSet()

    private val _currentFlightMode = MutableStateFlow(FlightStateMode.ACTIVE_DEPARTURE)
    val currentFlightMode: StateFlow<FlightStateMode> = _currentFlightMode.asStateFlow()

    private val _appProfile = MutableStateFlow(AppProfile.NO_FLIGHT)
    val appProfile: StateFlow<AppProfile> = _appProfile.asStateFlow()

    private val _activeFlight = MutableStateFlow<FlightInfo?>(null)
    val activeFlight: StateFlow<FlightInfo?> = _activeFlight.asStateFlow()

    // Database store for all scanned and saved boarding passes
    private val _storedPasses = MutableStateFlow<List<FlightInfo>>(emptyList())
    val storedPasses: StateFlow<List<FlightInfo>> = _storedPasses.asStateFlow()

    private val _liveOps = MutableStateFlow(LiveOpsData())
    val liveOps: StateFlow<LiveOpsData> = _liveOps.asStateFlow()

    private val _alerts = MutableStateFlow<List<FlightAlert>>(emptyList())
    val alerts: StateFlow<List<FlightAlert>> = _alerts.asStateFlow()

    private val _latestToast = MutableStateFlow<FlightAlert?>(null)
    val latestToast: StateFlow<FlightAlert?> = _latestToast.asStateFlow()

    init {
        _appProfile.value = loadAppProfile()
        val loaded = loadFromStorage()
        val hasStoredPassState = PlatformStorage.load(storageKey) != null
        if (loaded.isNotEmpty()) {
            _storedPasses.value = loaded
            _activeFlight.value = loaded.first()
            if (loaded.first().isDeparture) {
                _currentFlightMode.value = FlightStateMode.ACTIVE_DEPARTURE
            } else {
                _currentFlightMode.value = FlightStateMode.ACTIVE_ARRIVAL
            }
        } else if (!hasStoredPassState) {
            val initial = listOf(EdinburghData.sampleActiveDeparture)
            _storedPasses.value = initial
            _activeFlight.value = initial.first()
            saveToStorage(initial)
        } else {
            _storedPasses.value = emptyList()
            _activeFlight.value = null
            _currentFlightMode.value = FlightStateMode.NO_FLIGHT
        }
        when (_appProfile.value) {
            AppProfile.NO_FLIGHT -> setMode(FlightStateMode.NO_FLIGHT)
            AppProfile.DEPARTURE -> setMode(FlightStateMode.ACTIVE_DEPARTURE)
            AppProfile.ARRIVAL -> setMode(FlightStateMode.ACTIVE_ARRIVAL)
            AppProfile.ADMIN -> setMode(FlightStateMode.ACTIVE_DEPARTURE)
        }
        initRandomisedLiveOps()
        refreshAlerts()
    }

    fun refreshAlerts() {
        val flight = _activeFlight.value
        val currentTime = getEdinburghCurrentTime()
        val list = mutableListOf<FlightAlert>()

        if (flight != null) {
            val gateName = flight.gate ?: "Gate 12"
            val gateAlert = FlightAlert(
                id = "gate-${flight.flightNumber}",
                title = "Gate Announcement: $gateName",
                message = "Flight ${flight.flightNumber} to ${flight.destination} is now boarding at $gateName. Departure scheduled for ${flight.scheduledTime}.",
                timestamp = "$currentTime",
                isUrgent = true,
                isRead = false
            )
            list.add(gateAlert)
            _latestToast.value = gateAlert
            list.addAll(EdinburghData.sampleAlerts.filterNot { it.id == "alt-1" })

            SystemNotificationService.showNotification(
                id = 101,
                title = gateAlert.title,
                message = gateAlert.message,
                isUrgent = true,
                channelType = NotificationChannelType.FLIGHT_ALERTS
            )
        } else {
            list.addAll(EdinburghData.sampleAlerts)
        }

        _alerts.value = list
            .filterNot { dismissedAlertIds.contains(it.id) }
            .map { it.copy(isRead = readAlertIds.contains(it.id) || it.isRead) }
    }

    fun markAllAlertsAsRead() {
        _alerts.value = _alerts.value.map { it.copy(isRead = true) }
        readAlertIds.addAll(_alerts.value.map { it.id })
        saveStringSet(readAlertsKey, readAlertIds)
    }

    fun dismissAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
        dismissedAlertIds.add(alertId)
        saveStringSet(dismissedAlertsKey, dismissedAlertIds)
        if (_latestToast.value?.id == alertId) {
            _latestToast.value = null
        }
    }

    fun clearToast() {
        _latestToast.value?.let { toast ->
            dismissedAlertIds.add(toast.id)
            saveStringSet(dismissedAlertsKey, dismissedAlertIds)
            _alerts.value = _alerts.value.filterNot { it.id == toast.id }
        }
        _latestToast.value = null
    }

    fun triggerPushNotification(
        title: String,
        message: String,
        isUrgent: Boolean = false,
        channelType: NotificationChannelType = NotificationChannelType.FLIGHT_ALERTS
    ) {
        val currentTime = getEdinburghCurrentTime()
        val alert = FlightAlert(
            id = "alert-${currentTime.replace(":", "")}-${(100..999).random()}",
            title = title,
            message = message,
            timestamp = currentTime,
            isUrgent = isUrgent,
            isRead = false
        )
        val updated = mutableListOf(alert)
        updated.addAll(_alerts.value.filter { it.id != alert.id })
        _alerts.value = updated
        _latestToast.value = alert

        SystemNotificationService.showNotification(
            id = alert.id.hashCode(),
            title = title,
            message = message,
            isUrgent = isUrgent,
            channelType = channelType
        )
    }

    private fun loadStringSet(key: String): Set<String> {
        return try {
            val encoded = PlatformStorage.load(key) ?: return emptySet()
            json.decodeFromString<List<String>>(encoded).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveStringSet(key: String, values: Set<String>) {
        try {
            PlatformStorage.save(key, json.encodeToString(values.toList()))
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun saveToStorage(passes: List<FlightInfo>) {
        try {
            val serializables = passes.map { it.toSerializable() }
            val encoded = json.encodeToString(serializables)
            PlatformStorage.save(storageKey, encoded)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun loadFromStorage(): List<FlightInfo> {
        return try {
            val encoded = PlatformStorage.load(storageKey) ?: return emptyList()
            val serializables = json.decodeFromString<List<SerializableFlight>>(encoded)
            serializables.map { it.toFlightInfo() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setMode(mode: FlightStateMode) {
        _currentFlightMode.value = mode
        when (mode) {
            FlightStateMode.NO_FLIGHT -> {
                _activeFlight.value = null
                refreshAlerts()
            }
            FlightStateMode.ACTIVE_DEPARTURE -> {
                _activeFlight.value = EdinburghData.sampleActiveDeparture
                addStoredPass(EdinburghData.sampleActiveDeparture)
            }
            FlightStateMode.ACTIVE_ARRIVAL -> {
                _activeFlight.value = EdinburghData.sampleActiveArrival
                addStoredPass(EdinburghData.sampleActiveArrival)
            }
        }
    }

    fun setAppProfile(profile: AppProfile) {
        _appProfile.value = profile
        try {
            PlatformStorage.save(appProfileKey, profile.name)
        } catch (e: Exception) {
            // ignore
        }
        when (profile) {
            AppProfile.NO_FLIGHT -> setMode(FlightStateMode.NO_FLIGHT)
            AppProfile.DEPARTURE -> setMode(FlightStateMode.ACTIVE_DEPARTURE)
            AppProfile.ARRIVAL -> setMode(FlightStateMode.ACTIVE_ARRIVAL)
            AppProfile.ADMIN -> setMode(FlightStateMode.ACTIVE_DEPARTURE)
        }
    }

    private fun loadAppProfile(): AppProfile {
        return try {
            val stored = PlatformStorage.load(appProfileKey)
            stored?.let { AppProfile.valueOf(it) } ?: AppProfile.NO_FLIGHT
        } catch (e: Exception) {
            AppProfile.NO_FLIGHT
        }
    }

    fun selectFlight(flight: FlightInfo) {
        _activeFlight.value = flight
        addStoredPass(flight)
        if (flight.isDeparture) {
            _currentFlightMode.value = FlightStateMode.ACTIVE_DEPARTURE
        } else {
            _currentFlightMode.value = FlightStateMode.ACTIVE_ARRIVAL
        }
        refreshAlerts()
    }

    private fun addStoredPass(flight: FlightInfo) {
        val current = _storedPasses.value.toMutableList()
        val existsIndex = current.indexOfFirst {
            it.passIdentity() == flight.passIdentity()
        }
        if (existsIndex != -1) {
            current[existsIndex] = flight
        } else {
            current.add(0, flight)
        }
        _storedPasses.value = current
        saveToStorage(current)
    }

    fun removeStoredPass(flight: FlightInfo) {
        val current = _storedPasses.value.toMutableList()
        current.removeAll { it.passIdentity() == flight.passIdentity() }
        _storedPasses.value = current
        saveToStorage(current)
        val wasActive = _activeFlight.value?.passIdentity() == flight.passIdentity()
        if (wasActive) {
            _activeFlight.value = current.firstOrNull()
            if (_activeFlight.value == null) {
                _currentFlightMode.value = FlightStateMode.NO_FLIGHT
            } else if (_activeFlight.value!!.isDeparture) {
                _currentFlightMode.value = FlightStateMode.ACTIVE_DEPARTURE
            } else {
                _currentFlightMode.value = FlightStateMode.ACTIVE_ARRIVAL
            }
            refreshAlerts()
        }
    }

    fun clearFlight() {
        _activeFlight.value = null
        _currentFlightMode.value = FlightStateMode.NO_FLIGHT
    }

    fun updateSecurityWaitTime(minutes: Int) {
        _liveOps.value = _liveOps.value.copy(
            securityWaitMinutes = minutes,
            securityStatusLevel = statusLevelForWait(minutes, greenMax = 8, amberMax = 15)
        )
    }

    /**
     * Seeds initial live ops wait times within realistic airport ranges.
     */
    private fun initRandomisedLiveOps() {
        val secMinutes = (4..14).random()
        val tramInterval = (7..10).random()
        val tramNext = (1..tramInterval).random()
        val busInterval = (10..12).random()
        val busNext = (1..busInterval).random()

        _liveOps.value = LiveOpsData(
            securityWaitMinutes = secMinutes,
            securityStatusLevel = statusLevelForWait(secMinutes, greenMax = 8, amberMax = 15),
            tramNextMinutes = tramNext,
            tramIntervalMinutes = tramInterval,
            tramStatusLevel = statusLevelForWait(tramNext, greenMax = 4, amberMax = 8),
            busNextMinutes = busNext,
            busIntervalMinutes = busInterval,
            busStatusLevel = statusLevelForWait(busNext, greenMax = 4, amberMax = 9)
        )
    }

    /**
     * Called every minute: decrements transport countdowns down to 0 ("Due") before resetting
     * to their fixed interval, and randomises security wait time every [SECURITY_RANDOMIZE_INTERVAL_MINUTES].
     */
    fun tickLiveOps(minutesElapsed: Int) {
        val current = _liveOps.value

        val nextTram = if (current.tramNextMinutes <= 0) {
            current.tramIntervalMinutes
        } else {
            current.tramNextMinutes - 1
        }

        val nextBus = if (current.busNextMinutes <= 0) {
            current.busIntervalMinutes
        } else {
            current.busNextMinutes - 1
        }

        val nextSecurity = if (minutesElapsed > 0 && minutesElapsed % SECURITY_RANDOMIZE_INTERVAL_MINUTES == 0) {
            val delta = (-3..3).random()
            (current.securityWaitMinutes + delta).coerceIn(3, 18)
        } else {
            current.securityWaitMinutes
        }

        _liveOps.value = current.copy(
            securityWaitMinutes = nextSecurity,
            securityStatusLevel = statusLevelForWait(nextSecurity, greenMax = 8, amberMax = 15),
            tramNextMinutes = nextTram,
            tramStatusLevel = statusLevelForWait(nextTram, greenMax = 4, amberMax = 8),
            busNextMinutes = nextBus,
            busStatusLevel = statusLevelForWait(nextBus, greenMax = 4, amberMax = 9)
        )
    }

    private fun statusLevelForWait(minutes: Int, greenMax: Int, amberMax: Int): StatusLevel {
        return when {
            minutes <= greenMax -> StatusLevel.GREEN
            minutes <= amberMax -> StatusLevel.AMBER
            else -> StatusLevel.RED
        }
    }

    companion object {
        private const val SECURITY_RANDOMIZE_INTERVAL_MINUTES = 3
    }
}
