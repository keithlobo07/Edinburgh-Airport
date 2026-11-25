package io.github.keithlobo07.edinburghairport.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.keithlobo07.edinburghairport.data.FlightRepository
import io.github.keithlobo07.edinburghairport.data.WeatherAndTimeService
import io.github.keithlobo07.edinburghairport.model.FlightStateMode
import io.github.keithlobo07.edinburghairport.model.AppProfile
import io.github.keithlobo07.edinburghairport.model.HomeNavigationTab
import io.github.keithlobo07.edinburghairport.model.QuickAccessType
import io.github.keithlobo07.edinburghairport.model.VisitorAttraction
import io.github.keithlobo07.edinburghairport.theme.BackgroundWhite
import io.github.keithlobo07.edinburghairport.ui.components.AirportBottomNavBar
import io.github.keithlobo07.edinburghairport.ui.components.AirportHeader
import io.github.keithlobo07.edinburghairport.ui.components.InAppNotificationToast
import io.github.keithlobo07.edinburghairport.ui.FlightsScreen
import io.github.keithlobo07.edinburghairport.ui.MyTripScreen
import io.github.keithlobo07.edinburghairport.ui.ExploreScreen
import io.github.keithlobo07.edinburghairport.ui.ExploreFilter
import io.github.keithlobo07.edinburghairport.ui.components.BookParkingSheet
import io.github.keithlobo07.edinburghairport.ui.components.FastTrackPassSheet
import io.github.keithlobo07.edinburghairport.ui.components.FlightAlertsSheet
import io.github.keithlobo07.edinburghairport.ui.components.FlightSearchSheet
import io.github.keithlobo07.edinburghairport.ui.components.LiveOpsSection
import io.github.keithlobo07.edinburghairport.ui.components.PrimaryHeroCard
import io.github.keithlobo07.edinburghairport.ui.components.ProfileSheet
import io.github.keithlobo07.edinburghairport.ui.components.QuickAccessGrid
import io.github.keithlobo07.edinburghairport.ui.components.ScanBoardingPassSheet
import io.github.keithlobo07.edinburghairport.ui.components.TransportTicketSheet
import io.github.keithlobo07.edinburghairport.ui.components.TransportType
import io.github.keithlobo07.edinburghairport.ui.components.VisitEdinburghSection
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: FlightRepository = remember { FlightRepository() },
    weatherService: WeatherAndTimeService = remember { WeatherAndTimeService() }
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val currentMode by repository.currentFlightMode.collectAsState()
    val activeFlight by repository.activeFlight.collectAsState()
    val liveOps by repository.liveOps.collectAsState()
    val alerts by repository.alerts.collectAsState()
    val latestToast by repository.latestToast.collectAsState()
    val appProfile by repository.appProfile.collectAsState()

    // Live Time & Weather
    val liveTime by weatherService.currentTime.collectAsState()
    val liveWeather by weatherService.currentWeather.collectAsState()

    // Minute-by-minute live ops countdown (tram/bus -> "Due" -> interval) & periodic security randomisation
    LaunchedEffect(repository) {
        var minutesElapsed = 0
        while (true) {
            delay(60_000L)
            minutesElapsed++
            repository.tickLiveOps(minutesElapsed)
        }
    }

    var selectedTab by remember { mutableStateOf(HomeNavigationTab.HOME) }
    var targetMapPlaceId by remember { mutableStateOf<String?>(null) }
    var exploreInitialFilter by remember { mutableStateOf(ExploreFilter.ALL) }

    // Sheets visibility state
    var showScanSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showFastTrackSheet by remember { mutableStateOf(false) }
    var showAlertsSheet by remember { mutableStateOf(false) }
    var showParkingSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var selectedTransportTicket by remember { mutableStateOf<TransportType?>(null) }

    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedTab == HomeNavigationTab.HOME) {
                AirportHeader(
                    modifier = Modifier.statusBarsPadding(),
                    localTime = liveTime,
                    weatherSummary = liveWeather.weatherSummary,
                    weatherIcon = liveWeather.iconResource,
                    unreadAlertCount = alerts.count { !it.isRead },
                    onNotificationsClick = {
                        repository.markAllAlertsAsRead()
                        showAlertsSheet = true
                    },
                    onProfileClick = {
                        showProfileSheet = true
                    }
                )
            }
        },
        bottomBar = {
            AirportBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == HomeNavigationTab.EXPLORE) {
                        exploreInitialFilter = ExploreFilter.ALL
                    }
                    selectedTab = tab
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    when (tab) {
                        HomeNavigationTab.MAP -> {
                            MapScreen(
                                initialTargetPlaceId = targetMapPlaceId,
                                testFeaturesEnabled = appProfile == AppProfile.ADMIN,
                                onNavigateBack = { selectedTab = HomeNavigationTab.HOME }
                            )
                        }
                        HomeNavigationTab.FLIGHTS -> {
                            FlightsScreen()
                        }
                        HomeNavigationTab.MY_TRIP -> {
                            MyTripScreen(
                                repository = repository,
                                appProfile = appProfile,
                                onNavigateToMap = { placeId ->
                                    targetMapPlaceId = placeId
                                    selectedTab = HomeNavigationTab.MAP
                                },
                                onOpenSearchSheet = { showSearchSheet = true },
                                onOpenScanSheet = { showScanSheet = true },
                                onOpenFastTrackSheet = { showFastTrackSheet = true },
                                onExploreShopping = {
                                    exploreInitialFilter = ExploreFilter.SHOPPING
                                    selectedTab = HomeNavigationTab.EXPLORE
                                },
                                onExploreLounges = {
                                    exploreInitialFilter = ExploreFilter.LOUNGES
                                    selectedTab = HomeNavigationTab.EXPLORE
                                },
                                onBuyParking = {
                                    showParkingSheet = true
                                }
                            )
                        }
                        HomeNavigationTab.EXPLORE -> {
                            ExploreScreen(
                                onNavigateToMap = { placeId ->
                                    targetMapPlaceId = placeId
                                    selectedTab = HomeNavigationTab.MAP
                                },
                                initialFilter = exploreInitialFilter
                            )
                        }
                        HomeNavigationTab.HOME -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = innerPadding.calculateTopPadding())
                                    .verticalScroll(scrollState)
                                    .background(BackgroundWhite)
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // 1. Primary Hero Card (State A, State B, State C)
                                PrimaryHeroCard(
                                    flightMode = currentMode,
                                    appProfile = appProfile,
                                    activeFlight = activeFlight,
                    onModeSelect = { newMode -> repository.setMode(newMode) },
                    onSearchClick = { showSearchSheet = true },
                    onScanBoardingPassClick = { showScanSheet = true },
                    onClearFlight = { repository.clearFlight() },
                    onWayfindToGateClick = {
                        val gateId = activeFlight?.gate?.lowercase()?.replace(" ", "-") ?: "gate-14"
                        targetMapPlaceId = gateId
                        selectedTab = HomeNavigationTab.MAP
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Visit Edinburgh Carousel (Plum banner #752969 with visitor cards)
                VisitEdinburghSection(
                    onAttractionClick = { attraction ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Attraction: ${attraction.title} • ${attraction.durationFromAirport}",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Live Ops: Security Queue Wait Time + Edinburgh Tram + Airlink 100 Bus
                LiveOpsSection(
                    liveOps = liveOps,
                    onBookFastTrackClick = { showFastTrackSheet = true },
                    onBuyTramClick = { selectedTransportTicket = TransportType.TRAM },
                    onBuyBusClick = { selectedTransportTicket = TransportType.AIRLINK_BUS }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Quick Access 2x2 Grid (Terminal Map, Parking, Shops & Dining, Lounge Access)
                QuickAccessGrid(
                    onItemClick = { type ->
                        when (type) {
                            QuickAccessType.TERMINAL_MAP -> {
                                targetMapPlaceId = null
                                selectedTab = HomeNavigationTab.MAP
                            }
                            QuickAccessType.SHOP_AND_DINE -> {
                                exploreInitialFilter = ExploreFilter.SHOPPING
                                selectedTab = HomeNavigationTab.EXPLORE
                            }
                            QuickAccessType.LOUNGE_ACCESS -> {
                                exploreInitialFilter = ExploreFilter.LOUNGES
                                selectedTab = HomeNavigationTab.EXPLORE
                            }
                            QuickAccessType.PARKING -> {
                                showParkingSheet = true
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }

        // Modals & Bottom Sheets
        if (showScanSheet) {
            ScanBoardingPassSheet(
                onDismiss = { showScanSheet = false },
                showDemoOptions = appProfile == AppProfile.ADMIN,
                onFlightScanned = { scannedFlight ->
                    repository.selectFlight(scannedFlight)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Boarding Pass loaded: ${scannedFlight.flightNumber} to ${scannedFlight.destination}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        if (showSearchSheet) {
            FlightSearchSheet(
                onDismiss = { showSearchSheet = false },
                onFlightSelected = { selectedFlight ->
                    repository.selectFlight(selectedFlight)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Flight added: ${selectedFlight.flightNumber}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        if (showFastTrackSheet) {
            FastTrackPassSheet(
                onDismiss = { showFastTrackSheet = false }
            )
        }

        if (showAlertsSheet) {
            FlightAlertsSheet(
                alerts = alerts,
                onDismiss = { showAlertsSheet = false },
                onDismissAlert = { alertId -> repository.dismissAlert(alertId) },
                onTestPushNotification = if (appProfile == AppProfile.ADMIN) {{
                    repository.triggerPushNotification(
                        title = "Gate Announcement: Gate 21",
                        message = "Flight BA 1445 boarding gate is now Gate 21 (5 min walk via Central Concourse). Final call in 15 mins.",
                        isUrgent = true
                    )
                }} else null
            )
        }

        selectedTransportTicket?.let { transportType ->
            TransportTicketSheet(
                transportType = transportType,
                onDismiss = { selectedTransportTicket = null },
                onNavigateToMap = { placeId ->
                    selectedTransportTicket = null
                    targetMapPlaceId = placeId
                    selectedTab = HomeNavigationTab.MAP
                }
            )
        }

        if (showParkingSheet) {
            BookParkingSheet(
                onDismiss = { showParkingSheet = false },
                onParkingBooked = { carParkName, spaceRef ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Parking Booked: $spaceRef (Saved to Google Wallet)",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onNavigateToMap = { placeId ->
                    showParkingSheet = false
                    targetMapPlaceId = placeId
                    selectedTab = HomeNavigationTab.MAP
                }
            )
        }

        if (showProfileSheet) {
            ProfileSheet(
                selectedProfile = appProfile,
                onSelectProfile = { repository.setAppProfile(it) },
                onOpenMapTest = {
                    showProfileSheet = false
                    targetMapPlaceId = "sec-central"
                    selectedTab = HomeNavigationTab.MAP
                },
                onOpenPassDatabase = {
                    showProfileSheet = false
                    selectedTab = HomeNavigationTab.MY_TRIP
                },
                onChooseBoardingPass = {
                    showProfileSheet = false
                    showScanSheet = true
                },
                onChangeHomeState = { mode ->
                    repository.setMode(mode)
                    showProfileSheet = false
                    selectedTab = HomeNavigationTab.HOME
                },
                onSimulateNotification = {
                    showProfileSheet = false
                    repository.triggerPushNotification(
                        title = "Test Notification",
                        message = "This is a simulated flight alert.",
                        isUrgent = true
                    )
                },
                onDismiss = { showProfileSheet = false }
            )
        }

        latestToast?.let { toastAlert ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 82.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                InAppNotificationToast(
                    alert = toastAlert,
                    onDismiss = { repository.clearToast() }
                )
            }
        }
    }
}
