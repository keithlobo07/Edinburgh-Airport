package io.github.keithlobo07.edinburghairport.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_arrow_forward
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_location_pin
import edinburghairport.shared.generated.resources.ic_search
import io.github.keithlobo07.edinburghairport.data.LiveGpsCoordinates
import io.github.keithlobo07.edinburghairport.data.PlatformLocationManager
import io.github.keithlobo07.edinburghairport.data.TerminalMapData
import io.github.keithlobo07.edinburghairport.model.NavigationStep
import io.github.keithlobo07.edinburghairport.model.PlaceCategory
import io.github.keithlobo07.edinburghairport.model.StepIcon
import io.github.keithlobo07.edinburghairport.model.TerminalLevel
import io.github.keithlobo07.edinburghairport.model.TerminalPlace
import io.github.keithlobo07.edinburghairport.model.WayfindingRoute
import io.github.keithlobo07.edinburghairport.theme.BackgroundWhite
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusBlue
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    initialTargetPlaceId: String? = null,
    testFeaturesEnabled: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    var selectedLevel by remember { mutableStateOf(TerminalLevel.LEVEL_1) }
    var selectedCategory by remember { mutableStateOf(PlaceCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedPlace by remember { mutableStateOf<TerminalPlace?>(null) }
    var activeRoute by remember { mutableStateOf<WayfindingRoute?>(null) }
    var isNavigatingMode by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Real Live GPS State & Reference Anchor Mode
    val locationManager = remember { PlatformLocationManager() }
    var liveGpsCoords by remember { mutableStateOf<LiveGpsCoordinates?>(null) }
    var gpsStatusBanner by remember { mutableStateOf("Initializing Live GPS...") }
    var isGpsLocked by remember { mutableStateOf(false) }
    var isReferenceAnchorMode by remember { mutableStateOf(false) }
    var anchorGpsLat by remember { mutableStateOf<Double?>(null) }
    var anchorGpsLon by remember { mutableStateOf<Double?>(null) }
    var referenceUserX by remember { mutableFloatStateOf(0.36f) }
    var referenceUserY by remember { mutableFloatStateOf(0.58f) }
    var referenceMetersMoved by remember { mutableStateOf(0.0) }

    // Spacious Pan & Zoom states - calibrated to showcase full airport footprint nicely
    var scale by remember { mutableFloatStateOf(0.92f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(160f) }

    // Start Real GPS updates on mount
    DisposableEffect(Unit) {
        fun startGps() {
            locationManager.startLocationUpdates(
                onUpdate = { coords ->
                    liveGpsCoords = coords
                    isGpsLocked = true

                    if (isReferenceAnchorMode) {
                        if (anchorGpsLat == null || anchorGpsLon == null) {
                            anchorGpsLat = coords.latitude
                            anchorGpsLon = coords.longitude
                        }
                        val aLat = anchorGpsLat!!
                        val aLon = anchorGpsLon!!

                        // Real-world displacement in meters from user's anchor position
                        val deltaLat = coords.latitude - aLat
                        val deltaLon = coords.longitude - aLon
                        val northMeters = deltaLat * 111320.0
                        val eastMeters = deltaLon * 111320.0 * kotlin.math.cos(aLat * (kotlin.math.PI / 180.0))
                        val distanceMeters = kotlin.math.sqrt(northMeters * northMeters + eastMeters * eastMeters)
                        referenceMetersMoved = distanceMeters

                        // Map calibration:
                        // Start point is Security Hall: X = 0.36f, Y = 0.58f
                        // Airport East-West terminal concourse is ~450m (approx 0.0018 normalized units per meter)
                        // Airport North-South terminal concourse is ~300m (approx 0.0023 normalized units per meter)
                        val startAirportX = 0.36f
                        val startAirportY = 0.58f
                        val mappedX = (startAirportX + (eastMeters * 0.0018).toFloat()).coerceIn(0.08f, 0.92f)
                        val mappedY = (startAirportY - (northMeters * 0.0023).toFloat()).coerceIn(0.12f, 0.88f)

                        referenceUserX = mappedX
                        referenceUserY = mappedY

                        gpsStatusBanner = if (distanceMeters < 1.0) {
                            "Anchor: Security Hall • Walk around to move live on map"
                        } else {
                            "Anchor: Security Hall • Moved ${distanceMeters.toInt()}m from start"
                        }

                        selectedPlace?.let { place ->
                            activeRoute = TerminalMapData.getRouteTo(place, Pair(mappedX, mappedY))
                        }
                    } else {
                        gpsStatusBanner = if (coords.isNearAirport) {
                            "Live GPS Active • Airside Departures (±${coords.accuracyMeters.toInt().coerceAtLeast(1)}m)"
                        } else {
                            "Live GPS Active • ${coords.distanceToAirportKm} km to Edinburgh Airport"
                        }
                    }
                },
                onError = { err ->
                    gpsStatusBanner = "GPS: $err"
                }
            )
        }

        if (locationManager.isPermissionGranted()) {
            startGps()
        } else {
            gpsStatusBanner = "Location Permission Needed • Tap Locate Me"
            locationManager.requestLocationPermission()
            startGps()
        }

        onDispose {
            locationManager.stopLocationUpdates()
        }
    }

    LaunchedEffect(initialTargetPlaceId) {
        if (initialTargetPlaceId != null) {
            val place = TerminalMapData.places.find { it.id == initialTargetPlaceId }
            if (place != null) {
                selectedLevel = place.level
                selectedPlace = place
                val startPos = if (isReferenceAnchorMode) {
                    Pair(referenceUserX, referenceUserY)
                } else if (liveGpsCoords?.isNearAirport == true) {
                    Pair(liveGpsCoords!!.normalizedAirportX, liveGpsCoords!!.normalizedAirportY)
                } else {
                    TerminalMapData.defaultUserLocation
                }
                activeRoute = TerminalMapData.getRouteTo(place, startPos)
            }
        }
    }

    val filteredPlaces = remember(selectedLevel, selectedCategory, searchQuery) {
        TerminalMapData.places.filter { place ->
            place.level == selectedLevel &&
            (selectedCategory == PlaceCategory.ALL || place.category == selectedCategory) &&
            (searchQuery.isBlank() ||
             place.name.contains(searchQuery, ignoreCase = true) ||
             place.subtitle.contains(searchQuery, ignoreCase = true) ||
             place.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBF1F6))
    ) {
        // === 1. Interactive Schematic Terminal Map Canvas ===
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            val topOffsetPx = with(density) { 180.dp.toPx() }

            // Wide, generous world coordinate dimensions matching real airport geometry
            val baseW = canvasWidthPx * 1.65f
            val baseH = canvasHeightPx * 1.35f
            val startX = (canvasWidthPx - baseW) / 2f
            val startY = (canvasHeightPx - baseH) / 2f + topOffsetPx

            fun calcScreenPos(normX: Float, normY: Float): Offset {
                val rawX = startX + normX * baseW
                val rawY = startY + normY * baseH
                val cx = canvasWidthPx / 2f + offsetX
                val cy = canvasHeightPx / 2f + offsetY
                val sx = cx + (rawX - canvasWidthPx / 2f) * scale
                val sy = cy + (rawY - canvasHeightPx / 2f) * scale
                return Offset(sx, sy)
            }

            // Function to center camera on a place and its route
            fun focusCameraOnPlace(place: TerminalPlace, isNavigating: Boolean = false) {
                selectedPlace = place
                val startPos = if (isReferenceAnchorMode) {
                    Pair(referenceUserX, referenceUserY)
                } else if (liveGpsCoords?.isNearAirport == true) {
                    Pair(liveGpsCoords!!.normalizedAirportX, liveGpsCoords!!.normalizedAirportY)
                } else {
                    TerminalMapData.defaultUserLocation
                }
                val route = TerminalMapData.getRouteTo(place, startPos)
                activeRoute = route
                currentStepIndex = 0

                scale = if (isNavigating) 1.60f else 1.45f

                // Midpoint between start and destination
                val midX = (startPos.first + place.x) / 2f
                val midY = (startPos.second + place.y) / 2f

                val rawX = startX + midX * baseW
                val rawY = startY + midY * baseH

                // Center in upper portion of screen (upper 32%) to clearly display above cards/HUD
                val targetScreenX = canvasWidthPx * 0.50f
                val targetScreenY = if (isNavigating) canvasHeightPx * 0.44f else canvasHeightPx * 0.32f

                offsetX = targetScreenX - (rawX - canvasWidthPx / 2f) * scale - canvasWidthPx / 2f
                offsetY = targetScreenY - (rawY - canvasHeightPx / 2f) * scale - canvasHeightPx / 2f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.50f, 4.0f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                // Background Terminal Schematic Canvas
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    fun toCanvas(normX: Float, normY: Float): Offset = calcScreenPos(normX, normY)

                    // Draw Official Terminal Architecture & Shop Units matching Airside_map_website.pdf
                    drawOfficialTerminalLayout(
                        level = selectedLevel,
                        toCanvas = ::toCanvas,
                        scale = scale
                    )

                    // Draw Active Indoor Corridor Navigation Line (True Turn-by-Turn Waypoints)
                    if (activeRoute != null) {
                        val waypoints = activeRoute!!.waypoints
                        if (waypoints.size >= 2) {
                            val path = Path()
                            val first = toCanvas(waypoints[0].first, waypoints[0].second)
                            path.moveTo(first.x, first.y)
                            for (i in 1 until waypoints.size) {
                                val pt = toCanvas(waypoints[i].first, waypoints[i].second)
                                path.lineTo(pt.x, pt.y)
                            }

                            // Glowing route shadow
                            drawPath(
                                path = path,
                                color = EdinburghPlum.copy(alpha = 0.28f),
                                style = Stroke(
                                    width = 16.dp.toPx() * scale.coerceIn(0.7f, 1.4f),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Main dashed walking route
                            drawPath(
                                path = path,
                                color = EdinburghPlum,
                                style = Stroke(
                                    width = 5.5.dp.toPx() * scale.coerceIn(0.7f, 1.4f),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Waypoint turn nodes
                            for (i in 1 until waypoints.size - 1) {
                                val pt = toCanvas(waypoints[i].first, waypoints[i].second)
                                drawCircle(
                                    color = EdinburghPlum,
                                    radius = 4.5.dp.toPx() * scale.coerceIn(0.7f, 1.2f),
                                    center = pt
                                )
                            }
                        }
                    }

                    // Draw Live GPS User Location Beacon
                    if (selectedLevel == TerminalLevel.LEVEL_1) {
                        val beaconX = if (isReferenceAnchorMode) {
                            referenceUserX
                        } else if (liveGpsCoords?.isNearAirport == true) {
                            liveGpsCoords!!.normalizedAirportX
                        } else {
                            TerminalMapData.defaultUserLocation.first
                        }
                        val beaconY = if (isReferenceAnchorMode) {
                            referenceUserY
                        } else if (liveGpsCoords?.isNearAirport == true) {
                            liveGpsCoords!!.normalizedAirportY
                        } else {
                            TerminalMapData.defaultUserLocation.second
                        }

                        val userPt = toCanvas(beaconX, beaconY)

                        // Radar pulse ring
                        drawCircle(
                            color = StatusBlue.copy(alpha = 0.22f),
                            radius = 28.dp.toPx() * scale.coerceIn(0.8f, 1.3f),
                            center = userPt
                        )
                        // Middle solid ring
                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx() * scale.coerceIn(0.8f, 1.3f),
                            center = userPt
                        )
                        // Core blue dot
                        drawCircle(
                            color = StatusBlue,
                            radius = 8.dp.toPx() * scale.coerceIn(0.8f, 1.3f),
                            center = userPt
                        )
                    }
                }

                // POI Interactive Badges Overlay (All Official Edinburgh Airport Places)
                filteredPlaces.forEach { place ->
                    val screenPos = calcScreenPos(place.x, place.y)
                    val isSelected = selectedPlace?.id == place.id
                    val showLabel = scale >= 0.95f || isSelected || selectedCategory != PlaceCategory.ALL

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(screenPos.x.roundToInt() - 40, screenPos.y.roundToInt() - 20) }
                            .clickable {
                                focusCameraOnPlace(place, isNavigating = false)
                            }
                    ) {
                        MapPinMarker(
                            place = place,
                            isSelected = isSelected,
                            showLabel = showLabel
                        )
                    }
                }
            }

            // === 2. Floating Top Controls (Hidden during Navigation Mode) ===
            if (!isNavigatingMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Level / Floor Switcher Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = BackgroundWhite
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LevelTabButton(
                                title = "Level 1: Departures",
                                subtitle = "Security & Gates 1-30",
                                isSelected = selectedLevel == TerminalLevel.LEVEL_1,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedLevel = TerminalLevel.LEVEL_1
                                    selectedPlace = null
                                    activeRoute = null
                                }
                            )
                            LevelTabButton(
                                title = "Ground: Arrivals",
                                subtitle = "Check-in & Transport",
                                isSelected = selectedLevel == TerminalLevel.LEVEL_0,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedLevel = TerminalLevel.LEVEL_0
                                    selectedPlace = null
                                    activeRoute = null
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundWhite
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_search),
                                contentDescription = "Search",
                                tint = EdinburghNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Search gates 1-30, dining, shops...",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_close),
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Search Suggestions Dropdown Overlay
                    if (searchQuery.isNotBlank() && filteredPlaces.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .shadow(8.dp, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = BackgroundWhite
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                filteredPlaces.take(5).forEach { place ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                searchQuery = ""
                                                focusCameraOnPlace(place, isNavigating = false)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = place.category.pinColor.copy(alpha = 0.15f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = place.name.firstOrNull()?.toString() ?: "•",
                                                    fontWeight = FontWeight.Bold,
                                                    color = place.category.pinColor,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = place.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EdinburghNavy
                                            )
                                            Text(
                                                text = place.subtitle,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = "${place.walkingTimeMin} min",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EdinburghPlum
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlaceCategory.entries.forEach { category ->
                            val isCatSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCategory = category },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isCatSelected) EdinburghNavy else BackgroundWhite,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = category.label,
                                    color = if (isCatSelected) TextWhite else EdinburghNavy,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Real Live GPS Status Banner
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isGpsLocked) StatusGreen.copy(alpha = 0.14f) else EdinburghPlum.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isGpsLocked) StatusGreen else EdinburghPlum)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = gpsStatusBanner,
                            color = EdinburghNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // === 3. Floating Map Controls (Zoom In, Zoom Out, Locate Me) ===
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Locate Me Button
                FloatingActionButton(
                    onClick = {
                        isReferenceAnchorMode = false
                        if (!locationManager.isPermissionGranted()) {
                            locationManager.requestLocationPermission()
                        }
                        locationManager.startLocationUpdates(
                            onUpdate = { coords ->
                                if (isReferenceAnchorMode) return@startLocationUpdates
                                liveGpsCoords = coords
                                isGpsLocked = true
                                gpsStatusBanner = if (coords.isNearAirport) {
                                    "Live GPS Active • Airside Departures (±${coords.accuracyMeters.toInt().coerceAtLeast(1)}m)"
                                } else {
                                    "Live GPS Active • ${coords.distanceToAirportKm} km to Edinburgh Airport"
                                }
                            },
                            onError = { err ->
                                gpsStatusBanner = "GPS: $err"
                            }
                        )
                        scale = 1.25f
                        offsetX = -30f
                        offsetY = 40f
                    },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    containerColor = if (isGpsLocked) EdinburghPlum else BackgroundWhite,
                    contentColor = if (isGpsLocked) TextWhite else EdinburghNavy
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location_pin),
                        contentDescription = "Locate Me",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reference Anchor Test Button: Uses current physical location as Security Hall reference
                if (testFeaturesEnabled) FloatingActionButton(
                    onClick = {
                        isReferenceAnchorMode = !isReferenceAnchorMode
                        if (isReferenceAnchorMode) {
                            val cur = liveGpsCoords
                            anchorGpsLat = cur?.latitude
                            anchorGpsLon = cur?.longitude
                            referenceUserX = 0.36f
                            referenceUserY = 0.58f
                            referenceMetersMoved = 0.0
                            gpsStatusBanner = "Anchor: Security Hall • Walk around to move live"
                            scale = 1.30f
                            offsetX = -30f
                            offsetY = 30f
                        } else {
                            anchorGpsLat = null
                            anchorGpsLon = null
                            gpsStatusBanner = "Standard GPS Active"
                        }
                        selectedPlace?.let { place ->
                            val startPos = if (isReferenceAnchorMode) {
                                Pair(0.36f, 0.58f)
                            } else if (liveGpsCoords?.isNearAirport == true) {
                                Pair(liveGpsCoords!!.normalizedAirportX, liveGpsCoords!!.normalizedAirportY)
                            } else {
                                TerminalMapData.defaultUserLocation
                            }
                            activeRoute = TerminalMapData.getRouteTo(place, startPos)
                            currentStepIndex = 0
                        }
                    },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    containerColor = if (isReferenceAnchorMode) StatusGreen else BackgroundWhite,
                    contentColor = if (isReferenceAnchorMode) TextWhite else EdinburghNavy
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isReferenceAnchorMode) "ON" else "REF",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Zoom In
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { scale = (scale + 0.35f).coerceAtMost(4.0f) },
                    shape = CircleShape,
                    color = BackgroundWhite,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                    }
                }

                // Zoom Out
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { scale = (scale - 0.35f).coerceAtLeast(0.50f) },
                    shape = CircleShape,
                    color = BackgroundWhite,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                    }
                }
            }

            // === 4. Selected Place Detail Drawer / Bottom Sheet ===
            AnimatedVisibility(
                visible = selectedPlace != null && !isNavigatingMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 78.dp, start = 16.dp, end = 16.dp),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (selectedPlace != null) {
                    PlaceDetailCard(
                        place = selectedPlace!!,
                        route = activeRoute,
                        onStartNavigation = {
                            isNavigatingMode = true
                            focusCameraOnPlace(selectedPlace!!, isNavigating = true)
                        },
                        onClose = {
                            selectedPlace = null
                            activeRoute = null
                        }
                    )
                }
            }

            // === 5. Dedicated Turn-by-Turn Navigation Screen (Full Navigation HUD) ===
            AnimatedVisibility(
                visible = isNavigatingMode && selectedPlace != null && activeRoute != null,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TurnByTurnNavigationOverlay(
                    place = selectedPlace!!,
                    route = activeRoute!!,
                    currentStepIndex = currentStepIndex,
                    onNextStep = {
                        if (currentStepIndex < activeRoute!!.steps.size - 1) {
                            currentStepIndex++
                        }
                    },
                    onPreviousStep = {
                        if (currentStepIndex > 0) {
                            currentStepIndex--
                        }
                    },
                    onEndNavigation = {
                        isNavigatingMode = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelTabButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) EdinburghPlum else Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextWhite else EdinburghNavy
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) TextWhite.copy(alpha = 0.85f) else TextSecondary
            )
        }
    }
}

@Composable
private fun MapPinMarker(
    place: TerminalPlace,
    isSelected: Boolean,
    showLabel: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (place.category == PlaceCategory.GATES) {
            // Gate Badge
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = if (isSelected) EdinburghPlum else EdinburghNavy,
                shadowElevation = if (isSelected) 6.dp else 2.dp,
                modifier = Modifier
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(7.dp)
                    )
            ) {
                Text(
                    text = "${place.gateNumber ?: place.name.replace("Gate ", "")}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        } else {
            // Amenity Pin
            Surface(
                shape = CircleShape,
                color = if (isSelected) EdinburghPlum else place.category.pinColor,
                shadowElevation = if (isSelected) 6.dp else 2.dp,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = place.name.firstOrNull()?.toString() ?: "•"
                    Text(
                        text = initial,
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Name Label
        if (showLabel || isSelected) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = BackgroundWhite.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = EdinburghNavy,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailCard(
    place: TerminalPlace,
    route: WayfindingRoute?,
    onStartNavigation: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = place.category.pinColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = place.category.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = place.category.pinColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = place.level.label,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = place.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = EdinburghNavy
                    )

                    Text(
                        text = place.subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = EdinburghPlum
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = place.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Route Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLight)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From: ${route?.fromName ?: "Central Concourse (Duty Free Exit)"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${place.walkingTimeMin} min walk • ${place.walkingDistanceMeters}m",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EdinburghNavy
                    )
                }

                Button(
                    onClick = onStartNavigation,
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Navigate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Turn-by-Turn Navigation Screen Overlay
 */
@Composable
private fun TurnByTurnNavigationOverlay(
    place: TerminalPlace,
    route: WayfindingRoute,
    currentStepIndex: Int,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onEndNavigation: () -> Unit
) {
    var isStepsExpanded by remember { mutableStateOf(false) }
    val currentStep = route.steps.getOrNull(currentStepIndex) ?: route.steps.firstOrNull()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Top Turn-by-Turn Direction Guidance Card ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = EdinburghNavy
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Turn Icon
                    Surface(
                        shape = CircleShape,
                        color = EdinburghPlum,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val iconText = when (currentStep?.iconType) {
                                StepIcon.TURN_LEFT -> "←"
                                StepIcon.TURN_RIGHT -> "→"
                                StepIcon.DESTINATION -> "✓"
                                else -> "↑"
                            }
                            Text(
                                text = iconText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentStep != null) "In ${currentStep.distanceMeters}m" else "Starting navigation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusAmber
                        )
                        Text(
                            text = currentStep?.instruction ?: "Proceed along route",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            lineHeight = 20.sp
                        )
                    }

                    IconButton(
                        onClick = onEndNavigation,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "Exit Navigation",
                            tint = TextWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Next upcoming preview
                val nextStep = route.steps.getOrNull(currentStepIndex + 1)
                if (nextStep != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = nextStep.instruction,
                                fontSize = 11.sp,
                                color = TextWhite,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Bottom Navigation HUD Bar ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
                .shadow(12.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = BackgroundWhite
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${route.estimatedMinutes} min",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = EdinburghNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${route.distanceMeters} m)",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "Navigating to ${place.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EdinburghPlum
                        )
                    }

                    // End Navigation Button
                    Button(
                        onClick = onEndNavigation,
                        colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "End Route",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                // Step Progression Control
                if (route.steps.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step ${currentStepIndex + 1} of ${route.steps.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentStepIndex > 0) SurfaceLight else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = currentStepIndex > 0) { onPreviousStep() }
                            ) {
                                Text(
                                    text = "< Prev",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentStepIndex > 0) EdinburghNavy else Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentStepIndex < route.steps.size - 1) SurfaceLight else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = currentStepIndex < route.steps.size - 1) { onNextStep() }
                            ) {
                                Text(
                                    text = "Next >",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentStepIndex < route.steps.size - 1) EdinburghNavy else Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isStepsExpanded = !isStepsExpanded }
                            ) {
                                Text(
                                    text = if (isStepsExpanded) "Hide Steps" else "All Steps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EdinburghPlum,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Expandable Turn List
                    AnimatedVisibility(visible = isStepsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            route.steps.forEachIndexed { idx, step ->
                                val isCurrent = idx == currentStepIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) EdinburghPlum else SurfaceLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) TextWhite else EdinburghNavy
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = step.instruction,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) EdinburghNavy else TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${step.distanceMeters}m",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the accurate official architectural geometry and shop units
 * matching Edinburgh Airport's official Airside Map (Airside_map_website.pdf)
 */
private fun DrawScope.drawOfficialTerminalLayout(
    level: TerminalLevel,
    toCanvas: (Float, Float) -> Offset,
    scale: Float
) {
    val wallColor = Color(0xFF1E293B)
    val wallStroke = Stroke(
        width = 2.2.dp.toPx() * scale.coerceIn(0.7f, 1.4f),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    val concourseFloor = Color(0xFFFFFFFF)
    val hallwayFill = Color(0xFFF8FAFC)
    val unitFill = Color(0xFFF1F5F9)
    val securityFill = Color(0xFFFEF3C7)
    val dutyFreeFill = Color(0xFFFCE7F3)

    if (level == TerminalLevel.LEVEL_1) {
        // === OFFICIAL AIRSIDE DEPARTURES FLOOR PLAN ===

        // 1. Base Terminal Building Shell (The large outer envelope)
        val outerWall = Path().apply {
            val p0 = toCanvas(0.08f, 0.08f) // North-West corner (Gate 6)
            val p1 = toCanvas(0.64f, 0.08f) // North Concourse outer wall (Gate 11)
            val p2 = toCanvas(0.71f, 0.22f) // Forth & Main angled step
            val p3 = toCanvas(0.71f, 0.32f) // Burger King step
            val p4 = toCanvas(0.96f, 0.32f) // East Wing top (Gates 13-14)
            val p5 = toCanvas(0.96f, 0.68f) // East Wing bottom (Gates 15-16)
            val p6 = toCanvas(0.88f, 0.68f) // Pier link right
            val p7 = toCanvas(0.88f, 1.05f) // Gates 17-30 South Pier tip right
            val p8 = toCanvas(0.76f, 1.05f) // Gates 17-30 South Pier tip left
            val p9 = toCanvas(0.76f, 0.68f) // Pier link left
            val p10 = toCanvas(0.68f, 0.68f) // East hall southwest wall
            val p11 = toCanvas(0.68f, 0.44f) // Concourse south boundary
            val p12 = toCanvas(0.44f, 0.44f) // Duty Free right wall
            val p13 = toCanvas(0.44f, 0.64f) // Security screening right
            val p14 = toCanvas(0.28f, 0.64f) // Security screening left
            val p15 = toCanvas(0.28f, 0.44f) // Duty Free left exit
            val p16 = toCanvas(0.22f, 0.44f) // West concourse wall
            val p17 = toCanvas(0.03f, 0.30f) // West outer wall (Gate 5)
            val p18 = toCanvas(0.01f, 0.62f) // West pier tip left
            val p19 = toCanvas(0.05f, 0.62f) // West pier tip right
            val p20 = toCanvas(0.05f, 0.44f) // West pier top right

            moveTo(p0.x, p0.y)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            lineTo(p5.x, p5.y)
            lineTo(p6.x, p6.y)
            lineTo(p7.x, p7.y)
            lineTo(p8.x, p8.y)
            lineTo(p9.x, p9.y)
            lineTo(p10.x, p10.y)
            lineTo(p11.x, p11.y)
            lineTo(p12.x, p12.y)
            lineTo(p13.x, p13.y)
            lineTo(p14.x, p14.y)
            lineTo(p15.x, p15.y)
            lineTo(p16.x, p16.y)
            lineTo(p20.x, p20.y)
            lineTo(p19.x, p19.y)
            lineTo(p18.x, p18.y)
            lineTo(p17.x, p17.y)
            close()
        }

        // Draw concourse base
        drawPath(outerWall, concourseFloor)
        drawPath(outerWall, wallColor, style = wallStroke)

        // 2. Wide Pedestrian Circulation Hallways (The Main Street)
        val spineTopLeft = toCanvas(0.14f, 0.22f)
        val spineBottomRight = toCanvas(0.72f, 0.28f)
        drawRoundRect(
            color = hallwayFill,
            topLeft = spineTopLeft,
            size = Size(spineBottomRight.x - spineTopLeft.x, spineBottomRight.y - spineTopLeft.y),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // West circulation corridor
        val westCorridor = Path().apply {
            val w1 = toCanvas(0.14f, 0.22f)
            val w2 = toCanvas(0.22f, 0.22f)
            val w3 = toCanvas(0.22f, 0.44f)
            val w4 = toCanvas(0.14f, 0.44f)
            moveTo(w1.x, w1.y)
            lineTo(w2.x, w2.y)
            lineTo(w3.x, w3.y)
            lineTo(w4.x, w4.y)
            close()
        }
        drawPath(westCorridor, hallwayFill)

        // South-East Pier walkway
        val pierTopLeft = toCanvas(0.78f, 0.68f)
        val pierBottomRight = toCanvas(0.86f, 1.04f)
        drawRoundRect(
            color = hallwayFill,
            topLeft = pierTopLeft,
            size = Size(pierBottomRight.x - pierTopLeft.x, pierBottomRight.y - pierTopLeft.y),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // 3. Security Screening Hall
        val secTopLeft = toCanvas(0.28f, 0.54f)
        val secBottomRight = toCanvas(0.44f, 0.64f)
        drawRoundRect(
            color = securityFill,
            topLeft = secTopLeft,
            size = Size(secBottomRight.x - secTopLeft.x, secBottomRight.y - secTopLeft.y),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawRoundRect(
            color = wallColor.copy(alpha = 0.5f),
            topLeft = secTopLeft,
            size = Size(secBottomRight.x - secTopLeft.x, secBottomRight.y - secTopLeft.y),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = wallStroke
        )

        // 4. World Duty Free S-Curve Walkthrough
        val dfSnake = Path().apply {
            val p1 = toCanvas(0.28f, 0.44f)
            val p2 = toCanvas(0.44f, 0.44f)
            val p3 = toCanvas(0.44f, 0.54f)
            val p4 = toCanvas(0.38f, 0.54f)
            val p5 = toCanvas(0.38f, 0.36f)
            val p6 = toCanvas(0.28f, 0.36f)
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            lineTo(p5.x, p5.y)
            lineTo(p6.x, p6.y)
            close()
        }
        drawPath(dfSnake, dutyFreeFill)
        drawPath(dfSnake, wallColor.copy(alpha = 0.3f), style = wallStroke)

        // 5. Individual Architectural Shop & Amenity Units matching Airside_map_website.pdf
        fun drawUnitBox(left: Float, top: Float, right: Float, bottom: Float, color: Color = unitFill) {
            val tl = toCanvas(left, top)
            val br = toCanvas(right, bottom)
            drawRoundRect(
                color = color,
                topLeft = tl,
                size = Size(br.x - tl.x, br.y - tl.y),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFCBD5E1),
                topLeft = tl,
                size = Size(br.x - tl.x, br.y - tl.y),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(1.2.dp.toPx() * scale.coerceIn(0.7f, 1.2f))
            )
        }

        // --- NORTH SHOPPING & DINING ROW ---
        drawUnitBox(0.08f, 0.10f, 0.16f, 0.22f) // All Bar One West
        drawUnitBox(0.17f, 0.10f, 0.23f, 0.22f) // Pret A Manger West
        drawUnitBox(0.23f, 0.10f, 0.27f, 0.22f) // Caffè Nero
        drawUnitBox(0.27f, 0.12f, 0.35f, 0.22f) // Fever-Tree Bar
        drawUnitBox(0.35f, 0.10f, 0.40f, 0.22f) // WHSmith Central
        drawUnitBox(0.40f, 0.10f, 0.45f, 0.22f) // Boots Pharmacy
        drawUnitBox(0.45f, 0.10f, 0.49f, 0.22f) // JD Sports
        drawUnitBox(0.49f, 0.10f, 0.52f, 0.18f) // Accessorize
        drawUnitBox(0.49f, 0.18f, 0.54f, 0.25f) // Starbucks Central
        drawUnitBox(0.55f, 0.18f, 0.62f, 0.25f) // Wetherspoon (Sir Walter Scott)
        drawUnitBox(0.63f, 0.10f, 0.70f, 0.22f) // Forth & Main Diner
        drawUnitBox(0.66f, 0.22f, 0.70f, 0.30f) // Burger King

        // --- WEST WING UNITS ---
        drawUnitBox(0.15f, 0.30f, 0.22f, 0.40f) // Giraffe World Kitchen
        drawUnitBox(0.08f, 0.34f, 0.15f, 0.42f) // Escape & BA Lounges

        // --- CENTRAL ISLAND UNITS ---
        drawUnitBox(0.23f, 0.26f, 0.25f, 0.34f) // WHSmith Bookshop
        drawUnitBox(0.25f, 0.26f, 0.31f, 0.34f) // Edinburgh Gin & Brora
        drawUnitBox(0.31f, 0.26f, 0.36f, 0.34f) // Rituals & Tartan Weaving
        drawUnitBox(0.41f, 0.26f, 0.45f, 0.34f) // Haute Parfumerie
        drawUnitBox(0.45f, 0.26f, 0.51f, 0.34f) // FatFace & InMotion
        drawUnitBox(0.52f, 0.26f, 0.58f, 0.34f) // Wetherspoon Central
        drawUnitBox(0.58f, 0.26f, 0.63f, 0.34f) // Sunglass Hut & Duty Free Express
        drawUnitBox(0.63f, 0.26f, 0.68f, 0.34f) // Heritage of Scotland

        // --- EAST CONCOURSE UNITS ---
        drawUnitBox(0.72f, 0.44f, 0.77f, 0.52f) // Seoul Bird
        drawUnitBox(0.78f, 0.36f, 0.85f, 0.44f) // Pret A Manger East
        drawUnitBox(0.78f, 0.44f, 0.85f, 0.50f) // The Kin / Aspire Lounge
        drawUnitBox(0.85f, 0.44f, 0.89f, 0.50f) // WHSmith East
        drawUnitBox(0.78f, 0.50f, 0.85f, 0.58f) // Icons Sports Bar Main
        drawUnitBox(0.82f, 0.58f, 0.89f, 0.66f) // Icons Sports Bar Lower
        drawUnitBox(0.74f, 0.58f, 0.79f, 0.66f) // All Bar One East

        // --- SOUTH-EAST PIER UNITS ---
        drawUnitBox(0.79f, 0.71f, 0.85f, 0.75f) // WHSmith & Caffè Nero Pier

    } else {
        // === GROUND FLOOR (Check-in, Arrivals, Ground Transport) ===
        val gTopLeft = toCanvas(0.12f, 0.38f)
        val gBottomRight = toCanvas(0.82f, 0.64f)
        drawRoundRect(
            color = concourseFloor,
            topLeft = gTopLeft,
            size = Size(gBottomRight.x - gTopLeft.x, gBottomRight.y - gTopLeft.y),
            cornerRadius = CornerRadius(16.dp.toPx())
        )
        drawRoundRect(
            color = wallColor,
            topLeft = gTopLeft,
            size = Size(gBottomRight.x - gTopLeft.x, gBottomRight.y - gTopLeft.y),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = wallStroke
        )

        // Baggage Reclaim Hall (Arrivals)
        val bTopLeft = toCanvas(0.14f, 0.20f)
        val bBottomRight = toCanvas(0.80f, 0.38f)
        drawRoundRect(
            color = Color(0xFFEFF6FF),
            topLeft = bTopLeft,
            size = Size(bBottomRight.x - bTopLeft.x, bBottomRight.y - bTopLeft.y),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        drawRoundRect(
            color = wallColor,
            topLeft = bTopLeft,
            size = Size(bBottomRight.x - bTopLeft.x, bBottomRight.y - bTopLeft.y),
            cornerRadius = CornerRadius(14.dp.toPx()),
            style = wallStroke
        )

        // Ground Transport Forecourt (Tram & Bus Stances)
        val tTopLeft = toCanvas(0.25f, 0.68f)
        val tBottomRight = toCanvas(0.85f, 0.88f)
        drawRoundRect(
            color = Color(0xFFF1F5F9),
            topLeft = tTopLeft,
            size = Size(tBottomRight.x - tTopLeft.x, tBottomRight.y - tTopLeft.y),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        drawRoundRect(
            color = wallColor,
            topLeft = tTopLeft,
            size = Size(tBottomRight.x - tTopLeft.x, tBottomRight.y - tTopLeft.y),
            cornerRadius = CornerRadius(14.dp.toPx()),
            style = wallStroke
        )
    }
}
