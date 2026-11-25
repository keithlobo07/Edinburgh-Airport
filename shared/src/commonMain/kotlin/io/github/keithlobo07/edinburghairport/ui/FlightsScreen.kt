package io.github.keithlobo07.edinburghairport.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keithlobo07.edinburghairport.data.FlightBoardRepository
import io.github.keithlobo07.edinburghairport.model.FlightStatus
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.StatusRed
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// Colour palette for the retro board — blends Navy + warm flap
// ============================================================
private val BoardBg       = Color(0xFF07213E)   // Deep navy, slightly lighter than #052145
private val BoardStripe   = Color(0xFF061C33)   // Alternating row stripe
private val FlapBg        = Color(0xFF0D2D52)   // Individual flap cell background
private val FlapDivider   = Color(0xFF000000)   // Mid-line divider between top/bottom half
private val FlapText      = Color(0xFFF5B942)   // Classic split-flap amber
private val FlapDimText   = Color(0xFFA07020)   // Dimmer amber for departed / secondary
private val HeaderBg      = EdinburghNavy       // Top-bar uses brand navy
private val TabActive     = EdinburghPlum
private val TabInactive   = Color(0xFF0D2D52)
private val DividerLine   = Color(0xFF1A3A5C)

data class BoardFlight(
    val time: String,
    val flight: String,
    val destination: String,
    val gate: String,
    val status: FlightStatus,
    val isDeparture: Boolean
)

@Composable
fun FlightsScreen() {
    val repository = remember { FlightBoardRepository() }

    var showDepartures by remember { mutableStateOf(true) }
    var selectedFlight by remember { mutableStateOf<BoardFlight?>(null) }

    val liveDepartures by repository.departures.collectAsState()
    val liveArrivals   by repository.arrivals.collectAsState()
    val isLoading      by repository.isLoading.collectAsState()

    val boardFlights = if (showDepartures) {
        liveDepartures
    } else {
        liveArrivals
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BoardBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── 1. Top Header ───────────────────────────────────────
            FlightsBoardHeader(
                showDepartures = showDepartures,
                isLoading = isLoading,
                onToggle = {
                    showDepartures = !showDepartures
                    selectedFlight = null
                }
            )

            // ── 2. Column Headers ────────────────────────────────────
            BoardColumnHeader(isDeparture = showDepartures)

            // ── 3. The Board Rows ────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Loading banner while fetching live data
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EdinburghPlum.copy(alpha = 0.18f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TowerLoadingIndicator()
                            Text(
                                text = "FETCHING LIVE DATA...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = FlapText.copy(alpha = 0.6f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                if (!isLoading && boardFlights.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO FLIGHT DATA AVAILABLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = FlapText.copy(alpha = 0.5f),
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                itemsIndexed(boardFlights) { index, flight ->
                    FlightBoardRow(
                        flight = flight,
                        isEven = index % 2 == 0,
                        isSelected = selectedFlight == flight,
                        rowIndex = index,
                        onClick = {
                            selectedFlight = if (selectedFlight == flight) null else flight
                        }
                    )
                }
            }
        }

        // ── 4. Expanded Flight Detail Overlay ───────────────────────
        selectedFlight?.let { flight ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { selectedFlight = null },
                contentAlignment = Alignment.BottomCenter
            ) {
                FlightDetailSheet(
                    flight = flight,
                    onDismiss = { selectedFlight = null }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Header with EDI branding + Departures / Arrivals toggle
// ─────────────────────────────────────────────────────────────
@Composable
private fun TowerLoadingIndicator() {
    val segments = remember {
        List(4) { Animatable(0f) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            segments.forEach { it.snapTo(0f) }
            delay(80)
            for (index in 3 downTo 0) {
                segments[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                )
                delay(100)
            }
            delay(350)
        }
    }

    Column(
        modifier = Modifier.width(42.dp).height(44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom)
    ) {
        listOf(16.dp, 22.dp, 28.dp, 34.dp).forEachIndexed { index, width ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(9.dp)
                    .graphicsLayer {
                        scaleY = segments[index].value
                        alpha = segments[index].value
                    }
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index == 3) FlapText else FlapText.copy(alpha = 0.72f))
            )
        }
    }
}

@Composable
private fun FlightsBoardHeader(
    showDepartures: Boolean,
    isLoading: Boolean = false,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HeaderBg,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Edinburgh Airport",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "EDI · Live Flight Board",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.65f)
                    )
                }

                // Live indicator dot — amber while loading, green when live
                val dotColor = if (isLoading) StatusAmber else StatusGreen
                val liveLabel = if (isLoading) "UPDATING" else "LIVE"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(dotColor, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = liveLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dotColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Departures / Arrivals pill toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(4.dp)
            ) {
                ToggleTab(
                    label = "Departures",
                    isSelected = showDepartures,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!showDepartures) onToggle() }
                )
                ToggleTab(
                    label = "Arrivals",
                    isSelected = !showDepartures,
                    modifier = Modifier.weight(1f),
                    onClick = { if (showDepartures) onToggle() }
                )
            }
        }
    }
}

@Composable
private fun ToggleTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) TabActive else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextWhite else TextWhite.copy(alpha = 0.5f)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Column header row (Time / Flight / Destination / Gate/Status)
// ─────────────────────────────────────────────────────────────
@Composable
private fun BoardColumnHeader(isDeparture: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF061C33))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TIME",
            modifier = Modifier.width(52.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = FlapDimText, letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "FLIGHT",
            modifier = Modifier.width(76.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = FlapDimText, letterSpacing = 1.sp
        )
        Text(
            text = if (isDeparture) "DESTINATION" else "ORIGIN",
            modifier = Modifier.weight(1f),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = FlapDimText, letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "STATUS",
            modifier = Modifier.width(66.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = FlapDimText, letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isDeparture) "GATE" else "CAROUSEL",
            modifier = Modifier.width(70.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = FlapDimText, letterSpacing = 1.sp,
            textAlign = TextAlign.End
        )
    }
    // hairline separator
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerLine))
}

// ─────────────────────────────────────────────────────────────
// Individual flight board row — split-flap cells + flip anim
// ─────────────────────────────────────────────────────────────
@Composable
private fun FlightBoardRow(
    flight: BoardFlight,
    isEven: Boolean,
    isSelected: Boolean,
    rowIndex: Int,
    onClick: () -> Unit
) {
    val rowBg = when {
        isSelected -> EdinburghPlum.copy(alpha = 0.18f)
        isEven     -> BoardBg
        else       -> BoardStripe
    }

    val isDeparted = flight.status == FlightStatus.DEPARTED
    val textAlpha  = if (isDeparted) 0.45f else 1f

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Time cell (flap-animated) ──────────────
            AnimatedFlapText(
                text = flight.time,
                modifier = Modifier.width(52.dp).padding(vertical = 10.dp),
                fontSize = 17.sp,
                color = FlapText.copy(alpha = textAlpha),
                delayMs = rowIndex * 60L
            )

            Spacer(modifier = Modifier.width(8.dp))

            // ── Flight number cell ─────────────────────
            AnimatedFlapText(
                text = flight.flight,
                modifier = Modifier.width(76.dp).padding(vertical = 10.dp),
                fontSize = 13.sp,
                color = FlapText.copy(alpha = textAlpha),
                delayMs = rowIndex * 60L + 80L
            )

            // ── Destination cell ───────────────────────
            AnimatedFlapText(
                text = flight.destination,
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                fontSize = 14.sp,
                color = FlapText.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                delayMs = rowIndex * 60L + 160L
            )

            Spacer(modifier = Modifier.width(8.dp))

            // ── Status badge ───────────────────────────
            StatusChip(flight.status)

            Spacer(modifier = Modifier.width(6.dp))

            // ── Gate / Carousel cell ───────────────────
            AnimatedFlapText(
                text = flight.gate,
                modifier = Modifier.width(70.dp).padding(vertical = 10.dp),
                fontSize = 12.sp,
                color = if (flight.status == FlightStatus.BOARDING || flight.status == FlightStatus.GATE_OPEN)
                    StatusAmber else FlapText.copy(alpha = textAlpha),
                fontWeight = if (flight.status == FlightStatus.BOARDING) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.End,
                delayMs = rowIndex * 60L + 240L
            )
        }

        // hairline row divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerLine))
    }
}

// ─────────────────────────────────────────────────────────────
// Split-flap animated text — flips in from above on first draw
// ─────────────────────────────────────────────────────────────
@Composable
private fun AnimatedFlapText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    color: Color = FlapText,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    delayMs: Long = 0L
) {
    val flipProgress = remember { Animatable(0f) }

    LaunchedEffect(text) {
        delay(delayMs)
        flipProgress.snapTo(0f)
        flipProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )
    }

    // Simulate flap reveal: scale Y from 0 → 1, slight overshoot in alpha
    val flapScaleY = flipProgress.value
    val flapAlpha = flipProgress.value.coerceIn(0f, 1f)

    Text(
        text = text,
        modifier = modifier.graphicsLayer {
            scaleY = flapScaleY
            alpha = flapAlpha
        },
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        fontFamily = FontFamily.Monospace   // Monospace = authentic flap feel
    )
}

// ─────────────────────────────────────────────────────────────
// Status badge chip
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatusChip(status: FlightStatus) {
    val (bg, textColor, label) = when (status) {
        FlightStatus.ON_TIME       -> Triple(StatusGreen.copy(alpha = 0.18f),  StatusGreen,  "ON TIME")
        FlightStatus.BOARDING      -> Triple(StatusAmber.copy(alpha = 0.22f),  StatusAmber,  "BOARDING")
        FlightStatus.GATE_OPEN     -> Triple(StatusAmber.copy(alpha = 0.18f),  StatusAmber,  "GATE OPEN")
        FlightStatus.GATE_CLOSING  -> Triple(StatusRed.copy(alpha = 0.22f),    StatusRed,    "CLOSING")
        FlightStatus.DELAYED       -> Triple(StatusRed.copy(alpha = 0.18f),    StatusRed,    "DELAYED")
        FlightStatus.DEPARTED      -> Triple(Color(0xFF1A3A5C),                FlapDimText,  "DEPARTED")
        FlightStatus.LANDED        -> Triple(StatusGreen.copy(alpha = 0.18f),  StatusGreen,  "LANDED")
        FlightStatus.BAGGAGE_READY -> Triple(StatusGreen.copy(alpha = 0.18f),  StatusGreen,  "BAGGAGE")
    }

    Box(
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 3.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Expanded Flight Detail Sheet (tapped row)
// ─────────────────────────────────────────────────────────────
@Composable
private fun FlightDetailSheet(
    flight: BoardFlight,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clickable { /* consume clicks */ },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color(0xFF0D2543)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextWhite.copy(alpha = 0.25f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large destination
            Text(
                text = flight.destination,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FlapText,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${flight.flight}  •  ${if (flight.isDeparture) "Departure" else "Arrival"}",
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailInfoTile(
                    label = "SCHEDULED",
                    value = flight.time,
                    modifier = Modifier.weight(1f)
                )
                DetailInfoTile(
                    label = if (flight.isDeparture) "GATE" else "CAROUSEL",
                    value = flight.gate,
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                DetailInfoTile(
                    label = "STATUS",
                    value = flight.status.label,
                    statusColor = statusColor(flight.status),
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Additional tip based on status
            val tip = when (flight.status) {
                FlightStatus.BOARDING    -> "Head to ${flight.gate} now — boarding is underway."
                FlightStatus.GATE_OPEN   -> "Your gate is open. Walk time approx. 6–8 minutes."
                FlightStatus.GATE_CLOSING-> "Gate closing soon — go to ${flight.gate} immediately."
                FlightStatus.DELAYED     -> "Flight delayed. Check the board for updates."
                FlightStatus.DEPARTED    -> "This flight has departed."
                FlightStatus.LANDED      -> "Flight has landed. Proceed to arrivals."
                FlightStatus.BAGGAGE_READY -> "Your baggage is ready at ${flight.gate}."
                else -> "Up to date. Enjoy your journey."
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EdinburghPlum.copy(alpha = 0.18f))
                    .border(1.dp, EdinburghPlum.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tip,
                    fontSize = 13.sp,
                    color = TextWhite.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dismiss hint
            Text(
                text = "Tap anywhere outside to close",
                fontSize = 11.sp,
                color = TextWhite.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DetailInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    statusColor: Color? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (highlight) EdinburghPlum.copy(alpha = 0.18f)
                else Color.Black.copy(alpha = 0.25f)
            )
            .padding(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor ?: if (highlight) FlapText else TextWhite,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun statusColor(status: FlightStatus): Color = when (status) {
    FlightStatus.ON_TIME, FlightStatus.LANDED, FlightStatus.BAGGAGE_READY -> StatusGreen
    FlightStatus.BOARDING, FlightStatus.GATE_OPEN -> StatusAmber
    FlightStatus.GATE_CLOSING, FlightStatus.DELAYED -> StatusRed
    FlightStatus.DEPARTED -> FlapDimText
}
