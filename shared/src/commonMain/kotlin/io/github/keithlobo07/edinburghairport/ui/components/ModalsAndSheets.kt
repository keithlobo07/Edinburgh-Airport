package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_barcode_scan
import edinburghairport.shared.generated.resources.ic_bell
import edinburghairport.shared.generated.resources.ic_check_circle
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_lightning
import edinburghairport.shared.generated.resources.ic_search
import edinburghairport.shared.generated.resources.ic_shield_check
import io.github.keithlobo07.edinburghairport.data.EdinburghData
import io.github.keithlobo07.edinburghairport.data.PurchasedPassStorage
import io.github.keithlobo07.edinburghairport.model.AirportDigitalPass
import io.github.keithlobo07.edinburghairport.model.PassType
import io.github.keithlobo07.edinburghairport.model.FlightAlert
import io.github.keithlobo07.edinburghairport.model.FlightInfo
import io.github.keithlobo07.edinburghairport.theme.BorderLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.StatusRed
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.SurfaceMuted
import io.github.keithlobo07.edinburghairport.theme.TextMuted
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumSurface
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import io.github.keithlobo07.edinburghairport.model.FlightStatus
import io.github.keithlobo07.edinburghairport.model.AppProfile
import io.github.keithlobo07.edinburghairport.model.FlightStateMode
import kotlinx.coroutines.delay

fun parseBoardingPassBarcode(raw: String): FlightInfo {
    val clean = raw.trim().uppercase()
    if (clean.isBlank()) {
        throw IllegalArgumentException("Empty barcode data received")
    }

    val structuredMatch = Regex(
        """^M1([A-Z]+)/([A-Z]+)\s+([A-Z0-9]{5,7})\s+([A-Z]{3})([A-Z]{3})([A-Z0-9]{2,3})\s*(\d{1,4})\s+(\d{3})([A-Z])(\d{3}[A-K])(\d{4})"""
    ).find(clean)

    if (structuredMatch != null) {
        val surname = structuredMatch.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
        val firstname = structuredMatch.groupValues[2].trim().lowercase().replaceFirstChar { it.uppercase() }
        val bookingReference = structuredMatch.groupValues[3]
        val originCode = structuredMatch.groupValues[4]
        val destinationCode = structuredMatch.groupValues[5]
        val carrierCode = structuredMatch.groupValues[6]
        val flightDigits = structuredMatch.groupValues[7]
        val julianDay = structuredMatch.groupValues[8].toIntOrNull()?.coerceIn(1, 365) ?: 329
        val classCode = structuredMatch.groupValues[9]
        val seat = structuredMatch.groupValues[10].dropLast(1).toIntOrNull()?.let { row ->
            "${row}${structuredMatch.groupValues[10].last()}"
        } ?: structuredMatch.groupValues[10]
        val boardingIndex = structuredMatch.groupValues[11]

        val monthLengths = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var remainingDay = julianDay
        var month = 1
        for (daysInMonth in monthLengths) {
            if (remainingDay <= daysInMonth) break
            remainingDay -= daysInMonth
            month++
        }
        val flightDate = "${remainingDay.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/2026"

        val destinationName = when (destinationCode) {
            "PEK" -> "Beijing"
            "LHR" -> "London Heathrow"
            "LGW" -> "London Gatwick"
            "STN" -> "London Stansted"
            "LTN" -> "London Luton"
            "AMS" -> "Amsterdam"
            "CDG" -> "Paris"
            "FRA" -> "Frankfurt"
            "DUB" -> "Dublin"
            "DOH" -> "Doha"
            "DXB" -> "Dubai"
            "JFK" -> "New York"
            "MCO" -> "Orlando"
            "ALC" -> "Alicante"
            "AGP" -> "Malaga"
            "TFS" -> "Tenerife"
            else -> "$destinationCode Airport"
        }

        val airlineName = when (carrierCode) {
            "BA" -> "British Airways"
            "FR", "RYR" -> "Ryanair"
            "EZY", "U2" -> "easyJet"
            "KL" -> "KLM"
            "QR" -> "Qatar Airways"
            "UA" -> "United Airlines"
            "LM" -> "Loganair"
            "LH" -> "Lufthansa"
            "AF" -> "Air France"
            "DL" -> "Delta Air Lines"
            "AA" -> "American Airlines"
            else -> "$carrierCode Airlines"
        }

        val cabinClass = when (classCode) {
            "C", "J" -> "Business Class"
            "F", "A" -> "First Class"
            "W", "P" -> "Premium Economy"
            else -> "Economy Class"
        }

        val seatRow = seat.dropLast(1).toIntOrNull() ?: 32
        val boardingGroup = when {
            cabinClass != "Economy Class" || seatRow <= 5 -> "Group 1"
            seatRow <= 15 -> "Group 2"
            seatRow <= 25 -> "Group 3"
            else -> "Group 4"
        }

        val numericFlight = flightDigits.toIntOrNull() ?: 32
        val gateNumber = (numericFlight % 22) + 1
        val scheduledHour = (numericFlight % 12) + 10
        val scheduledMinute = (numericFlight * 5) % 60
        val scheduledTime = "${scheduledHour.toString().padStart(2, '0')}:${scheduledMinute.toString().padStart(2, '0')}"

        val (checkInDesk, zone) = when (carrierCode) {
            "BA" -> "Desks 12-18 (Zone B)" to "Zone B"
            "EZY" -> "Desks 24-29 (Zone C)" to "Zone C"
            "FR" -> "Desks 1-6 (Zone A)" to "Zone A"
            "KL" -> "Desks 8-11 (Zone A)" to "Zone A"
            "QR", "UA" -> "Desks 30-36 (Zone D)" to "Zone D"
            else -> "Desks 10-15 (Zone A)" to "Zone A"
        }

        return FlightInfo(
            flightNumber = "$carrierCode $flightDigits",
            airline = airlineName,
            destination = destinationName,
            destinationCode = destinationCode,
            origin = if (originCode == "EDI") "Edinburgh" else "$originCode Airport",
            originCode = originCode,
            scheduledTime = scheduledTime,
            estimatedTime = scheduledTime,
            status = FlightStatus.GATE_OPEN,
            statusText = "Gate Open",
            checkInDesk = checkInDesk,
            gate = "Gate $gateNumber",
            walkingTime = "${(gateNumber * 2 % 7) + 3} min walk",
            terminal = "Main Terminal",
            isDeparture = true,
            isCached = false,
            lastUpdated = "Scanned now",
            passengerName = "$firstname $surname",
            seat = seat,
            boardingGroup = boardingGroup,
            zone = zone,
            bookingReference = bookingReference,
            cabinClass = cabinClass,
            boardingIndex = boardingIndex,
            flightDate = flightDate
        )
    }

    throw IllegalArgumentException("Unrecognized or incomplete IATA BCBP barcode: $raw")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBoardingPassSheet(
    onDismiss: () -> Unit,
    onFlightScanned: (FlightInfo) -> Unit,
    showDemoOptions: Boolean = false
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var isTorchOn by remember { mutableStateOf(false) }
    var scannedFlight by remember { mutableStateOf<FlightInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition()
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(scannedFlight) {
        val flight = scannedFlight ?: return@LaunchedEffect
        delay(700)
        onFlightScanned(flight)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan Boarding Pass",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = EdinburghNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hold your phone over the barcode or QR code",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Camera Viewfinder Box with Live Preview + Scanner Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF071426)),
                contentAlignment = Alignment.Center
            ) {
                // 1. Live Camera Feed via CameraX
                CameraBarcodeScanner(
                    modifier = Modifier.fillMaxSize(),
                    isTorchOn = isTorchOn,
                    onBarcodeScanned = { rawCode ->
                        if (scannedFlight == null) {
                            try {
                                val parsed = parseBoardingPassBarcode(rawCode)
                                scannedFlight = parsed
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "Scan Error: ${e.message}"
                            }
                        }
                    }
                )

                // 2. High-Tech Viewfinder HUD Overlay (Brackets + Laser Line)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val marginX = w * 0.12f
                    val marginY = h * 0.12f
                    val left = marginX
                    val right = w - marginX
                    val top = marginY
                    val bottom = h - marginY
                    val cornerLen = 28.dp.toPx()
                    val strokeW = 3.5.dp.toPx()
                    val cornerColor = if (scannedFlight != null) Color(0xFF10B981) else Color(0xFF8E3780)

                    // Top-Left Corner
                    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeW, StrokeCap.Round)
                    drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeW, StrokeCap.Round)

                    // Top-Right Corner
                    drawLine(cornerColor, Offset(right, top), Offset(right - cornerLen, top), strokeW, StrokeCap.Round)
                    drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeW, StrokeCap.Round)

                    // Bottom-Left Corner
                    drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW, StrokeCap.Round)
                    drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW, StrokeCap.Round)

                    // Bottom-Right Corner
                    drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW, StrokeCap.Round)
                    drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW, StrokeCap.Round)

                    // Sweeping Laser Scan Line
                    if (scannedFlight == null) {
                        val laserY = top + (bottom - top) * laserYRatio
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(left, laserY),
                            end = Offset(right, laserY),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                }

                // 3. Torch Toggle Button (Top Right)
                Surface(
                    onClick = { isTorchOn = !isTorchOn },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isTorchOn) Color(0xFFFDE047) else Color.White,
                    border = if (isTorchOn) BorderStroke(2.dp, Color.White) else null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_lightning),
                            contentDescription = if (isTorchOn) "Turn torch off" else "Turn torch on",
                            tint = EdinburghNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isTorchOn) "ON" else "OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EdinburghNavy
                        )
                    }
                }

                // 4. Success Checkmark Overlay on detection
                if (scannedFlight != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF052145).copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("OK", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Boarding Pass Recognized!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${scannedFlight!!.flightNumber} • ${scannedFlight!!.destination}",
                                fontSize = 13.sp,
                                color = Color(0xFF93C5FD),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 5. Error State Overlay when barcode cannot be read / parsed
                if (errorMessage != null && scannedFlight == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF052145).copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Unable to Read Barcode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "Barcode format not recognized. Please try again or search manually.",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { errorMessage = null },
                                colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Try Again", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (errorMessage != null && scannedFlight == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Supports IATA BCBP standard barcodes (PDF417, Aztec, QR & Code 128)",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (showDemoOptions) {
                Text(
                    text = "Or simulate scan in 1-tap:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scannedFlight = EdinburghData.sampleActiveDeparture
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BA 1445 (LHR)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scannedFlight = EdinburghData.sampleActiveArrival
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("FR 6621 (DUB)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchSheet(
    onDismiss: () -> Unit,
    onFlightSelected: (FlightInfo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    val filteredFlights = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            EdinburghData.searchableFlights
        } else {
            EdinburghData.searchableFlights.filter {
                it.flightNumber.contains(searchQuery, ignoreCase = true) ||
                it.destination.contains(searchQuery, ignoreCase = true) ||
                it.airline.contains(searchQuery, ignoreCase = true) ||
                it.destinationCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Find Your Flight",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = EdinburghNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Flight #, airline, or city (e.g. BA1445, Amsterdam)", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = "Search",
                        tint = EdinburghNavy,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceMuted,
                    unfocusedContainerColor = SurfaceMuted,
                    focusedIndicatorColor = EdinburghPlum,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFlights) { flight ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            .clickable {
                                onFlightSelected(flight)
                                onDismiss()
                            },
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = flight.flightNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = EdinburghNavy
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (flight.isDeparture) "Departure" else "Arrival",
                                        fontSize = 11.sp,
                                        color = if (flight.isDeparture) EdinburghPlum else StatusGreen
                                    )
                                }
                                Text(
                                    text = "${flight.airline} • ${if (flight.isDeparture) flight.destination else flight.origin}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = flight.scheduledTime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = EdinburghNavy
                                )
                                Text(
                                    text = flight.statusText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = flight.status.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastTrackPassSheet(
    onDismiss: () -> Unit
) {
    var booked by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_shield_check),
                contentDescription = "Shield",
                tint = EdinburghPlum,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Security Fast-Track Pass",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = EdinburghNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bypass regular queues via dedicated lane directly to X-Ray",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!booked) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceLight
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1x Adult Fast-Track Pass", fontWeight = FontWeight.Medium)
                            Text("£10.00", fontWeight = FontWeight.Bold, color = EdinburghNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Valid today until last departure • Scan directly at Fast-Track e-gates",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        booked = true
                        PurchasedPassStorage.setFastTrackPurchased(true)
                        PurchasedPassStorage.saveDigitalPass(
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Pay £10.00 & Get Pass", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StatusGreen.copy(alpha = 0.12f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = "Confirmed",
                            tint = StatusGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Pass Active!", fontWeight = FontWeight.Bold, color = StatusGreen)
                        Text("Barcode #EDI-FT-94821 ready to scan at Lane 1", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy)
                ) {
                    Text("Done", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    selectedProfile: AppProfile,
    onSelectProfile: (AppProfile) -> Unit,
    onOpenMapTest: () -> Unit,
    onOpenPassDatabase: () -> Unit,
    onChooseBoardingPass: () -> Unit,
    onChangeHomeState: (FlightStateMode) -> Unit,
    onSimulateNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Passenger Profiles", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Text(
                "Select a profile for the app state.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            AppProfile.entries.forEach { profile ->
                val isSelected = profile == selectedProfile
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectProfile(profile)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) EdinburghPlum.copy(alpha = 0.12f) else SurfaceLight,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) EdinburghPlum else BorderLight
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EdinburghNavy)
                            Text(profile.subtitle, fontSize = 11.sp, color = TextSecondary)
                        }
                        if (isSelected) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check_circle),
                                contentDescription = "Selected",
                                tint = EdinburghPlum,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (selectedProfile == AppProfile.ADMIN) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Admin Test Tools", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)

                AdminTestButton("Test position on map", onOpenMapTest)
                AdminTestButton("View boarding pass database", onOpenPassDatabase)
                AdminTestButton("Choose boarding pass at scan", onChooseBoardingPass)
                AdminTestButton("Simulate notification", onSimulateNotification)

                Text("Change home state", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "No Flight" to FlightStateMode.NO_FLIGHT,
                        "Departure" to FlightStateMode.ACTIVE_DEPARTURE,
                        "Arrival" to FlightStateMode.ACTIVE_ARRIVAL
                    ).forEach { (label, mode) ->
                        OutlinedButton(
                            onClick = { onChangeHomeState(mode) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(label, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTestButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = EdinburghNavy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightAlertsSheet(
    alerts: List<FlightAlert>,
    onDismiss: () -> Unit,
    onDismissAlert: ((String) -> Unit)? = null,
    onTestPushNotification: (() -> Unit)? = null
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalised Flight Alerts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = EdinburghNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active flight alerts", fontSize = 14.sp, color = TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alerts) { alert ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    if (alert.isUrgent) StatusRed.copy(alpha = 0.5f) else BorderLight,
                                    RoundedCornerShape(14.dp)
                                ),
                            color = if (alert.isUrgent) Color(0xFFFEF2F2) else SurfaceLight
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (alert.isUrgent) StatusRed else EdinburghNavy,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = alert.timestamp,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                        if (onDismissAlert != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { onDismissAlert(alert.id) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.ic_close),
                                                    contentDescription = "Dismiss Alert",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.message,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (onTestPushNotification != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onTestPushNotification,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EdinburghPlum)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_bell),
                        contentDescription = "Test Notification",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulate Gate Change Push Notification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EdinburghPlum
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

