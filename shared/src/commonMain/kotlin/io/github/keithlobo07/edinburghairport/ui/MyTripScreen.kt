package io.github.keithlobo07.edinburghairport.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_arrow_forward
import edinburghairport.shared.generated.resources.ic_barcode_scan
import edinburghairport.shared.generated.resources.ic_check_circle
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_google_logo
import edinburghairport.shared.generated.resources.ic_location_pin
import io.github.keithlobo07.edinburghairport.data.GoogleWalletService
import edinburghairport.shared.generated.resources.ic_quick_lounge
import edinburghairport.shared.generated.resources.ic_quick_parking
import edinburghairport.shared.generated.resources.ic_quick_shopping
import edinburghairport.shared.generated.resources.ic_search
import edinburghairport.shared.generated.resources.ic_shield_check
import io.github.keithlobo07.edinburghairport.data.EdinburghData
import io.github.keithlobo07.edinburghairport.data.FlightRepository
import io.github.keithlobo07.edinburghairport.data.passIdentity
import io.github.keithlobo07.edinburghairport.model.FlightInfo
import io.github.keithlobo07.edinburghairport.model.FlightStatus
import io.github.keithlobo07.edinburghairport.model.AppProfile
import io.github.keithlobo07.edinburghairport.ui.components.pressClickEffect
import io.github.keithlobo07.edinburghairport.data.PurchasedPassStorage
import io.github.keithlobo07.edinburghairport.model.AirportDigitalPass
import io.github.keithlobo07.edinburghairport.model.PassType
import io.github.keithlobo07.edinburghairport.theme.BackgroundWhite
import io.github.keithlobo07.edinburghairport.theme.BorderLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumSurface
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusBlue
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.StatusRed
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.SurfaceMuted
import io.github.keithlobo07.edinburghairport.theme.TextMuted
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import org.jetbrains.compose.resources.painterResource

data class MyTripChecklistItem(val text: String, val isCompleted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    repository: FlightRepository,
    appProfile: AppProfile,
    onNavigateToMap: (placeId: String?) -> Unit,
    onOpenSearchSheet: () -> Unit,
    onOpenScanSheet: () -> Unit,
    onOpenFastTrackSheet: () -> Unit,
    onExploreShopping: () -> Unit = {},
    onExploreLounges: () -> Unit = {},
    onBuyParking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeFlight by repository.activeFlight.collectAsState()
    val storedPasses by repository.storedPasses.collectAsState()
    val liveOps by repository.liveOps.collectAsState()

    var showBoardingPassModal by remember { mutableStateOf(false) }
    var showCarParkNoteModal by remember { mutableStateOf(false) }
    var selectedDigitalPass by remember { mutableStateOf<AirportDigitalPass?>(null) }
    var digitalPasses by remember { mutableStateOf(PurchasedPassStorage.loadAllDigitalPasses()) }
    var savedCarParkSpot by remember { mutableStateOf("Terminal Parking Zone C, Bay 42") }

    androidx.compose.runtime.LaunchedEffect(showBoardingPassModal, selectedDigitalPass) {
        digitalPasses = PurchasedPassStorage.loadAllDigitalPasses()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        PurchasedPassStorage.ensureFastTrackDigitalPass()
        digitalPasses = PurchasedPassStorage.loadAllDigitalPasses()
    }

    val checklistItems = remember {
        val savedCompleted = PurchasedPassStorage.loadPrepChecklist()
        mutableStateListOf(
            MyTripChecklistItem("Check in online & receive boarding pass", isCompleted = "check-in" in savedCompleted),
            MyTripChecklistItem("Liquids up to 2L stay inside bags (EDI 3D CT Scanners)", isCompleted = "liquids" in savedCompleted),
            MyTripChecklistItem("Drop checked luggage at Airline Desks", isCompleted = "bag-drop" in savedCompleted),
            MyTripChecklistItem("Pass Security Hall (Current wait: ${liveOps.securityWaitMinutes} mins)", isCompleted = "security" in savedCompleted),
            MyTripChecklistItem("Explore World Duty Free & Scottish shops airside", isCompleted = "shopping" in savedCompleted),
            MyTripChecklistItem("Arrive at Gate before boarding closure", isCompleted = "gate" in savedCompleted)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            MyTripTopBar(
                hasActiveFlight = activeFlight != null,
                onSearchClick = onOpenSearchSheet,
                onScanClick = onOpenScanSheet
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeFlight != null) {
                    item {
                        ActiveTripHeroCard(
                            flight = activeFlight!!,
                            onViewBoardingPass = { showBoardingPassModal = true },
                            onNavigateToGate = {
                                val gateId = activeFlight?.gate?.lowercase()?.replace(" ", "-") ?: "gate-14"
                                onNavigateToMap(gateId)
                            },
                            onChangeFlight = if (appProfile == AppProfile.ADMIN) onOpenSearchSheet else null
                        )
                    }

                    item {
                        JourneyMilestonesCard(
                            flight = activeFlight!!,
                            securityWaitMinutes = liveOps.securityWaitMinutes,
                            onNavigateToGate = {
                                val gateId = activeFlight?.gate?.lowercase()?.replace(" ", "-") ?: "gate-14"
                                onNavigateToMap(gateId)
                            },
                            onOpenFastTrack = onOpenFastTrackSheet
                        )
                    }

                    item {
                        DigitalBoardingPassPill(
                            flight = activeFlight!!,
                            onClick = { showBoardingPassModal = true }
                        )
                    }

                    item {
                        MyPassesWalletCard(
                            storedPasses = storedPasses,
                            activeFlight = activeFlight,
                            digitalPasses = digitalPasses,
                            onSelectBoardingPass = { flight ->
                                repository.selectFlight(flight)
                                showBoardingPassModal = true
                            },
                            onDeleteBoardingPass = { flight -> repository.removeStoredPass(flight) },
                            onSelectDigitalPass = { pass -> selectedDigitalPass = pass },
                            onDeleteDigitalPass = { pass ->
                                PurchasedPassStorage.removeDigitalPass(pass.id)
                                digitalPasses = PurchasedPassStorage.loadAllDigitalPasses()
                            }
                        )
                    }
                } else {
                    item {
                        EmptyTripHeroCard(
                            onSearchClick = onOpenSearchSheet,
                            onScanClick = onOpenScanSheet,
                            showDemoOptions = appProfile == AppProfile.ADMIN,
                            onLoadSampleDeparture = {
                                repository.selectFlight(EdinburghData.sampleActiveDeparture)
                            },
                            onLoadSampleArrival = {
                                repository.selectFlight(EdinburghData.sampleActiveArrival)
                            }
                        )
                    }

                    item {
                        MyPassesWalletCard(
                            storedPasses = storedPasses,
                            activeFlight = activeFlight,
                            digitalPasses = digitalPasses,
                            onSelectBoardingPass = { flight ->
                                repository.selectFlight(flight)
                                showBoardingPassModal = true
                            },
                            onDeleteBoardingPass = { flight -> repository.removeStoredPass(flight) },
                            onSelectDigitalPass = { pass -> selectedDigitalPass = pass },
                            onDeleteDigitalPass = { pass ->
                                PurchasedPassStorage.removeDigitalPass(pass.id)
                                digitalPasses = PurchasedPassStorage.loadAllDigitalPasses()
                            }
                        )
                    }
                }

                item {
                    TripChecklistCard(
                        items = checklistItems,
                        onToggle = { item ->
                            val index = checklistItems.indexOf(item)
                            if (index >= 0) {
                                checklistItems[index] = item.copy(isCompleted = !item.isCompleted)
                                val completedIds = mutableSetOf<String>()
                                checklistItems.forEachIndexed { itemIndex, checklistItem ->
                                    if (checklistItem.isCompleted) {
                                        completedIds += when (itemIndex) {
                                            0 -> "check-in"
                                            1 -> "liquids"
                                            2 -> "bag-drop"
                                            3 -> "security"
                                            4 -> "shopping"
                                            else -> "gate"
                                        }
                                    }
                                }
                                PurchasedPassStorage.savePrepChecklist(completedIds)
                            }
                        }
                    )
                }

                item {
                    AirportSmartTipCard()
                }

                item {
                    TripServicesGrid(
                        onOpenFastTrack = onOpenFastTrackSheet,
                        onLoungeClick = onExploreLounges,
                        onShoppingClick = onExploreShopping,
                        onParkingClick = onBuyParking
                    )
                }

                item {
                    AirportHelpContactsCard()
                }
            }
        }

        if (showBoardingPassModal && activeFlight != null) {
            FullBoardingPassSheet(
                flight = activeFlight!!,
                onDismiss = { showBoardingPassModal = false },
                onNavigateToGate = {
                    showBoardingPassModal = false
                    val gateId = activeFlight?.gate?.lowercase()?.replace(" ", "-") ?: "gate-14"
                    onNavigateToMap(gateId)
                }
            )
        }

        if (showCarParkNoteModal) {
            CarParkLocatorSheet(
                currentLocation = savedCarParkSpot,
                onSave = { newSpot ->
                    savedCarParkSpot = newSpot
                    showCarParkNoteModal = false
                },
                onDismiss = { showCarParkNoteModal = false }
            )
        }

        selectedDigitalPass?.let { pass ->
            DigitalPassDetailSheet(
                pass = pass,
                onDismiss = { selectedDigitalPass = null }
            )
        }
    }
}

@Composable
private fun MyTripTopBar(
    hasActiveFlight: Boolean,
    onSearchClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Trip",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EdinburghNavy
            )
            Text(
                text = if (hasActiveFlight) "Live Travel Companion • EDI" else "Plan your airport experience",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceMuted)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = "Search Flight",
                    tint = EdinburghNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BorderLight, CircleShape)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_barcode_scan),
                    contentDescription = "Scan Boarding Pass",
                    tint = EdinburghNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveTripHeroCard(
    flight: FlightInfo,
    onViewBoardingPass: () -> Unit,
    onNavigateToGate: () -> Unit,
    onChangeFlight: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = EdinburghNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EdinburghNavy,
                            Color(0xFF0A2B56),
                            Color(0xFF103666)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = flight.airline.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = flight.flightNumber,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    val badgeBorderColor = when (flight.status) {
                        FlightStatus.ON_TIME -> StatusGreen
                        FlightStatus.BOARDING, FlightStatus.GATE_OPEN -> StatusAmber
                        FlightStatus.DELAYED, FlightStatus.GATE_CLOSING -> StatusRed
                        else -> EdinburghPlumLight
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (flight.status) {
                            FlightStatus.ON_TIME -> StatusGreen.copy(alpha = 0.2f)
                            FlightStatus.BOARDING, FlightStatus.GATE_OPEN -> StatusAmber.copy(alpha = 0.25f)
                            FlightStatus.DELAYED, FlightStatus.GATE_CLOSING -> StatusRed.copy(alpha = 0.25f)
                            else -> EdinburghPlum.copy(alpha = 0.3f)
                        },
                        border = BorderStroke(1.dp, badgeBorderColor)
                    ) {
                        Text(
                            text = flight.statusText.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (flight.status) {
                                FlightStatus.ON_TIME -> StatusGreen
                                FlightStatus.BOARDING, FlightStatus.GATE_OPEN -> StatusAmber
                                FlightStatus.DELAYED, FlightStatus.GATE_CLOSING -> StatusRed
                                else -> Color(0xFFF9A8D4)
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(flight.originCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        Text(flight.origin, fontSize = 13.sp, color = Color(0xFFCBD5E1))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (flight.isDeparture) "Direct Departure" else "Arrival",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EdinburghPlumLight))
                            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFF475569))
                            Text(text = "EDI", color = TextWhite, fontSize = 11.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFF475569))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusGreen))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(flight.destinationCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        Text(flight.destination, fontSize = 13.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF041933).copy(alpha = 0.6f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TripInfoTile(
                        label = if (flight.isDeparture) "SCHEDULED" else "ARRIVAL",
                        value = flight.scheduledTime,
                        subtext = "Est. ${flight.estimatedTime}"
                    )
                    TripInfoTile(
                        label = if (flight.isDeparture) "GATE" else "CAROUSEL",
                        value = flight.gate ?: flight.baggageCarousel ?: "TBA",
                        subtext = flight.walkingTime ?: "Main Terminal",
                        isHighlight = true
                    )
                    TripInfoTile(
                        label = if (flight.isDeparture) "CHECK-IN" else "TERMINAL",
                        value = if (flight.isDeparture) "Zone B" else "Arrivals",
                        subtext = flight.checkInDesk.substringBefore("(").trim()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onViewBoardingPass,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Boarding Pass",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (flight.gate != null) {
                        OutlinedButton(
                            onClick = onNavigateToGate,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_location_pin),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = StatusGreen
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Gate Map",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (onChangeFlight != null) IconButton(
                        onClick = onChangeFlight,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF041933).copy(alpha = 0.6f))
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = "Switch Flight",
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripInfoTile(label: String, value: String, subtext: String, isHighlight: Boolean = false) {
    Column {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) Color(0xFFFDE047) else TextWhite)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = subtext, fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyTripHeroCard(
    onSearchClick: () -> Unit,
    onScanClick: () -> Unit,
    showDemoOptions: Boolean,
    onLoadSampleDeparture: () -> Unit,
    onLoadSampleArrival: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMuted),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(EdinburghPlumSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_barcode_scan),
                    contentDescription = null,
                    tint = EdinburghPlum,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Track Your Flight", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Add your departure or arrival to get live gate directions, security wait times, and flight alerts.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(painter = painterResource(Res.drawable.ic_search), contentDescription = null, modifier = Modifier.size(16.dp), tint = TextWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .pressClickEffect(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = EdinburghNavy
                    ),
                    border = BorderStroke(1.dp, BorderLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_barcode_scan),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EdinburghNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Pass", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showDemoOptions) Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Try demo: ", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "BA 1445 (London)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EdinburghPlum,
                    modifier = Modifier.clickable(onClick = onLoadSampleDeparture).padding(4.dp)
                )
                Text(text = " • ", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = "FR 6621 (Dublin)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EdinburghNavy,
                    modifier = Modifier.clickable(onClick = onLoadSampleArrival).padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun JourneyMilestonesCard(
    flight: FlightInfo,
    securityWaitMinutes: Int,
    onNavigateToGate: () -> Unit,
    onOpenFastTrack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Airport Journey Timeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                Text("Terminal 1", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TimelineStepItem(
                stepNumber = "1",
                title = "Check-in & Bag Drop",
                subtitle = flight.checkInDesk,
                statusText = "Open",
                isPassed = true,
                isCurrent = false
            )

            TimelineStepItem(
                stepNumber = "2",
                title = "Security Screening Hall",
                subtitle = "Queue time: $securityWaitMinutes mins • 3D CT Scanners",
                statusText = "FastTrack Available",
                isPassed = false,
                isCurrent = true,
                actionLabel = "FastTrack",
                onActionClick = onOpenFastTrack
            )

            TimelineStepItem(
                stepNumber = "3",
                title = "World Duty Free & Departure Lounge",
                subtitle = "Whiskies, dining, and charging pods open",
                statusText = "Airside",
                isPassed = false,
                isCurrent = false
            )

            TimelineStepItem(
                stepNumber = "4",
                title = flight.gate ?: "Gate Announcement",
                subtitle = flight.walkingTime ?: "Check information screens",
                statusText = flight.statusText,
                isPassed = false,
                isCurrent = flight.status == FlightStatus.BOARDING || flight.status == FlightStatus.GATE_OPEN,
                isLast = true,
                actionLabel = "Gate Map",
                onActionClick = onNavigateToGate
            )
        }
    }
}

@Composable
private fun TimelineStepItem(
    stepNumber: String,
    title: String,
    subtitle: String,
    statusText: String,
    isPassed: Boolean,
    isCurrent: Boolean,
    isLast: Boolean = false,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPassed -> StatusGreen
                            isCurrent -> EdinburghPlum
                            else -> SurfaceMuted
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPassed) {
                    Text(text = "✓", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = stepNumber,
                        color = if (isCurrent) TextWhite else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(if (isPassed) StatusGreen.copy(alpha = 0.5f) else BorderLight)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrent) EdinburghNavy else TextPrimary
                )

                if (actionLabel != null && onActionClick != null) {
                    Text(
                        text = actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EdinburghPlum,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EdinburghPlumSurface)
                            .clickable(onClick = onActionClick)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun DigitalBoardingPassPill(
    flight: FlightInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EdinburghPlumSurface),
        border = BorderStroke(1.dp, EdinburghPlumLight.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(EdinburghPlum),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_barcode_scan),
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text("Digital Boarding Pass", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                    Text("Passenger: ${flight.passengerName} • Seat ${flight.seat} • ${flight.gate ?: "Gate 14"}", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = "Expand",
                tint = EdinburghPlum,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TripChecklistCard(
    items: List<MyTripChecklistItem>,
    onToggle: (MyTripChecklistItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Airport Preparation Checklist", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                val completedCount = items.count { it.isCompleted }
                Text("$completedCount of ${items.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghPlum)
            }

            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onToggle(item) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.isCompleted) StatusGreen else SurfaceMuted)
                            .border(1.dp, if (item.isCompleted) StatusGreen else BorderLight, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isCompleted) {
                            Text(text = "✓", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = item.text,
                        fontSize = 13.sp,
                        color = if (item.isCompleted) TextMuted else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AirportSmartTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = BorderStroke(1.dp, Color(0xFF86EFAC))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = StatusGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Edinburgh Airport Security Update", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "New 3D CT scanners mean liquids up to 2L and large electronics (laptops, iPads) can remain inside your hand luggage during security check.",
                    fontSize = 12.sp,
                    color = Color(0xFF14532D),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun TripServicesGrid(
    onOpenFastTrack: () -> Unit,
    onLoungeClick: () -> Unit,
    onShoppingClick: () -> Unit,
    onParkingClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Airport Services & Bookings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TripServiceTile(
                title = "Security FastTrack",
                subtitle = "Skip standard queue",
                iconRes = Res.drawable.ic_shield_check,
                tint = EdinburghPlum,
                modifier = Modifier.weight(1f),
                onClick = onOpenFastTrack
            )
            TripServiceTile(
                title = "Airport Lounges",
                subtitle = "Aspire & Plaza Premium",
                iconRes = Res.drawable.ic_quick_lounge,
                tint = EdinburghNavy,
                modifier = Modifier.weight(1f),
                onClick = onLoungeClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TripServiceTile(
                title = "World Duty Free",
                subtitle = "Shopping",
                iconRes = Res.drawable.ic_quick_shopping,
                tint = EdinburghPlum,
                modifier = Modifier.weight(1f),
                onClick = onShoppingClick
            )
            TripServiceTile(
                title = "Parking Spot Note",
                subtitle = "Terminal Car Park",
                iconRes = Res.drawable.ic_quick_parking,
                tint = EdinburghNavy,
                modifier = Modifier.weight(1f),
                onClick = onParkingClick
            )
        }
    }
}

@Composable
private fun TripServiceTile(
    title: String,
    subtitle: String,
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMuted),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(BackgroundWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun AirportHelpContactsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Edinburgh Airport Helplines", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Special Assistance", fontSize = 12.sp, color = TextSecondary)
                    Text("0131 357 6373", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                }
                Column {
                    Text("Lost Property", fontSize = 12.sp, color = TextSecondary)
                    Text("0131 344 3400", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                }
                Column {
                    Text("Airport Police", fontSize = 12.sp, color = TextSecondary)
                    Text("101 (Non-emerg)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullBoardingPassSheet(
    flight: FlightInfo,
    onDismiss: () -> Unit,
    onNavigateToGate: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Boarding Pass", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Text("Show this barcode at Security & Boarding Gate", fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EdinburghNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(flight.airline, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(flight.flightNumber, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFDE047), fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(flight.originCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            Text(flight.origin, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                        Text(">", color = TextWhite, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(flight.destinationCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            Text(flight.destination, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PassDetailCol("PASSENGER", flight.passengerName)
                        PassDetailCol("PNR / REF", flight.bookingReference)
                        PassDetailCol("CLASS", flight.cabinClass)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PassDetailCol("DATE", flight.flightDate)
                        PassDetailCol("SEAT", flight.seat)
                        PassDetailCol("GROUP", flight.boardingGroup)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PassDetailCol("GATE", flight.gate ?: "Gate 12", highlight = true)
                        PassDetailCol("BOARDING", flight.scheduledTime)
                        PassDetailCol("SEQ / INDEX", flight.boardingIndex)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundWhite)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(32) { i ->
                                val width = if (i % 3 == 0) 4.dp else if (i % 2 == 0) 2.dp else 1.dp
                                Box(modifier = Modifier.width(width).height(44.dp).background(Color.Black))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${flight.originCode}-${flight.destinationCode}-${flight.flightNumber.replace(" ", "")}-${flight.bookingReference}-${flight.seat}-SEQ${flight.boardingIndex}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var isAddedToWallet by remember { mutableStateOf(false) }

            // "ADD TO GOOGLE WALLET" Official Style Button
            Surface(
                onClick = {
                    GoogleWalletService.saveBoardingPassToWallet(flight)
                    isAddedToWallet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = if (isAddedToWallet) Color(0xFF1E293B) else Color(0xFF000000),
                border = BorderStroke(1.dp, if (isAddedToWallet) StatusGreen else Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAddedToWallet) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = "Added",
                            tint = StatusGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Added to Google Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    } else {
                        // Official 4-color Google "G" logo
                        Icon(
                            painter = painterResource(Res.drawable.ic_google_logo),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add to Google Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onNavigateToGate,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(painter = painterResource(Res.drawable.ic_location_pin), contentDescription = null, modifier = Modifier.size(18.dp), tint = TextWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate to Gate in Map", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@Composable
private fun PassDetailCol(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (highlight) Color(0xFFFDE047) else TextWhite)
    }
}

@Composable
private fun ParkingOptionGrid(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    options.chunked(2).forEach { rowOptions ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowOptions.forEach { option ->
                val selected = option == selectedOption
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) EdinburghPlum else BorderLight
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    color = if (selected) EdinburghPlumSurface else SurfaceLight
                ) {
                    Text(
                        text = option,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) EdinburghPlum else EdinburghNavy
                    )
                }
            }
            if (rowOptions.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarParkLocatorSheet(
    currentLocation: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val carParkOptions = listOf(
        "Terminal Multi-Storey",
        "Terminal Zone C",
        "Plane Parking",
        "Long Stay",
        "Valet Parking"
    )
    val areaOptions = listOf(
        "Level 1",
        "Level 2",
        "Level 3",
        "Zone A",
        "Zone B",
        "Zone C",
        "Row A",
        "Row B",
        "Row C"
    )

    var selectedCarPark by remember {
        mutableStateOf(carParkOptions.firstOrNull { currentLocation.contains(it) } ?: "Terminal Multi-Storey")
    }
    var selectedArea by remember {
        mutableStateOf(areaOptions.firstOrNull { currentLocation.contains(it) } ?: "Zone C")
    }
    var bayOrRow by remember {
        mutableStateOf(currentLocation.substringAfter("Bay ", "").ifBlank {
            currentLocation.substringAfter("Row ", "").ifBlank { "42" }
        })
    }
    val text = "$selectedCarPark, $selectedArea, Bay $bayOrRow"
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)
        ) {
            Text("Car Park Location Note", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Record where you parked at Edinburgh Airport so you can easily find your vehicle upon return.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Car park", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(8.dp))
            ParkingOptionGrid(
                options = carParkOptions,
                selectedOption = selectedCarPark,
                onOptionSelected = { selectedCarPark = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Level or zone", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(8.dp))
            ParkingOptionGrid(
                options = areaOptions,
                selectedOption = selectedArea,
                onOptionSelected = { selectedArea = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bayOrRow,
                onValueChange = { bayOrRow = it.filter { character -> character.isLetterOrDigit() || character == '-' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bay or row") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EdinburghPlum,
                    focusedLabelColor = EdinburghPlum,
                    cursorColor = EdinburghPlum
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceMuted).padding(14.dp)
            ) {
                Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EdinburghNavy)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(text) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Parking Spot", fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@Composable
private fun MyPassesWalletCard(
    storedPasses: List<FlightInfo>,
    activeFlight: FlightInfo?,
    digitalPasses: List<AirportDigitalPass>,
    onSelectBoardingPass: (FlightInfo) -> Unit,
    onDeleteBoardingPass: (FlightInfo) -> Unit,
    onSelectDigitalPass: (AirportDigitalPass) -> Unit,
    onDeleteDigitalPass: (AirportDigitalPass) -> Unit
) {
    val totalCount = storedPasses.size + digitalPasses.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMuted),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EdinburghPlum),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_barcode_scan),
                            contentDescription = "Wallet Passes",
                            tint = TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "My Passes & Digital Wallet ($totalCount)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EdinburghNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (totalCount == 0) {
                Text(
                    text = "No passes active yet. Book parking, buy transit tickets, or scan boarding passes to access them here.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // 1. Digital Passes (Parking, Transit, Fast-Track)
            digitalPasses.forEach { pass ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectDigitalPass(pass) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (pass.type) {
                                        PassType.PARKING -> EdinburghNavy
                                        PassType.TRANSIT -> EdinburghPlum
                                        PassType.FAST_TRACK -> Color(0xFF1E3A8A)
                                        PassType.FLIGHT -> EdinburghPlum
                                    }
                                ) {
                                    Text(
                                        text = pass.type.title.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = pass.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EdinburghNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pass.subtitle} • ${pass.priceFormatted} • #${pass.reference}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteDigitalPass(pass) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BorderLight)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "Remove pass",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 2. Flight Boarding Passes
            storedPasses.forEach { pass ->
                val isActive = activeFlight?.passIdentity() == pass.passIdentity()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive) EdinburghNavy else Color.White,
                    border = BorderStroke(1.dp, if (isActive) EdinburghNavy else BorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectBoardingPass(pass) }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EdinburghPlum
                                ) {
                                    Text(
                                        text = "BOARDING PASS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = pass.flightNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isActive) TextWhite else EdinburghNavy
                                )
                                if (isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusGreen
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${pass.airline} • ${if (pass.isDeparture) pass.destination else pass.origin} • Seat ${pass.seat}",
                                fontSize = 11.sp,
                                color = if (isActive) Color(0xFF93C5FD) else TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteBoardingPass(pass) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color.White.copy(alpha = 0.12f)
                                    else BorderLight
                                )
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "Remove pass",
                                tint = if (isActive) TextWhite else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DigitalPassDetailSheet(
    pass: AirportDigitalPass,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var isAddedToWallet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Close & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (pass.type) {
                        PassType.PARKING -> EdinburghNavy
                        PassType.TRANSIT -> EdinburghPlum
                        PassType.FAST_TRACK -> Color(0xFF1E3A8A)
                        PassType.FLIGHT -> EdinburghPlum
                    }
                ) {
                    Text(
                        text = pass.type.title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Close",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = pass.title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = pass.subtitle,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Active & Scannable Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = StatusGreen.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Text(
                        text = pass.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Authentic Scannable Barcode / Optical Pass Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Barcode Stripes representation
                    Row(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val barWidths = listOf(3, 1, 4, 2, 5, 2, 3, 1, 4, 3, 2, 5, 1, 3, 4, 2, 3, 1, 5, 2, 4, 1, 3, 2, 4)
                        barWidths.forEach { w ->
                            Box(
                                modifier = Modifier
                                    .width(w.dp)
                                    .height(56.dp)
                                    .background(Color(0xFF0F172A))
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = pass.barcodeValue,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Official Edinburgh Airport Digital Validator Code",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details 2x2 Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PassDetailCol(label = pass.primaryFieldLabel, value = pass.primaryFieldValue)
                        PassDetailCol(label = pass.secondaryFieldLabel, value = pass.secondaryFieldValue)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PassDetailCol(label = pass.auxiliaryFieldLabel, value = pass.auxiliaryFieldValue)
                        PassDetailCol(label = "AMOUNT PAID", value = pass.priceFormatted, highlight = true)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Validity: ${pass.validUntil} • Reference #${pass.reference}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "ADD TO GOOGLE WALLET" Official Style Button (launches Google Wallet Everything Else)
            Surface(
                onClick = {
                    when (pass.type) {
                        PassType.TRANSIT -> {
                            GoogleWalletService.saveTransitPassToWallet(
                                ticketRef = pass.reference,
                                serviceName = pass.title,
                                route = pass.subtitle,
                                priceFormatted = pass.priceFormatted,
                                passengerCount = 1,
                                isReturn = pass.primaryFieldValue.contains("Return", ignoreCase = true)
                            )
                        }
                        PassType.PARKING -> {
                            GoogleWalletService.saveParkingPassToWallet(
                                bookingRef = pass.reference,
                                carParkName = pass.title,
                                assignedSpot = pass.auxiliaryFieldValue,
                                vehicleReg = pass.primaryFieldValue,
                                days = 3,
                                totalPriceFormatted = pass.priceFormatted
                            )
                        }
                        PassType.FAST_TRACK -> {
                            GoogleWalletService.saveTransitPassToWallet(
                                ticketRef = pass.reference,
                                serviceName = "Security Fast-Track",
                                route = "Priority Lane 1 to X-Ray",
                                priceFormatted = pass.priceFormatted,
                                passengerCount = 1,
                                isReturn = false
                            )
                        }
                        PassType.FLIGHT -> {
                            GoogleWalletService.saveTransitPassToWallet(
                                ticketRef = pass.reference,
                                serviceName = pass.title,
                                route = pass.subtitle,
                                priceFormatted = pass.priceFormatted,
                                passengerCount = 1,
                                isReturn = false
                            )
                        }
                    }
                    isAddedToWallet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = if (isAddedToWallet) Color(0xFF1E293B) else Color(0xFF000000),
                border = BorderStroke(1.dp, if (isAddedToWallet) StatusGreen else Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAddedToWallet) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = "Added",
                            tint = StatusGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Added to Google Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_google_logo),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add to Google Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Text("Close Pass", color = TextWhite, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
