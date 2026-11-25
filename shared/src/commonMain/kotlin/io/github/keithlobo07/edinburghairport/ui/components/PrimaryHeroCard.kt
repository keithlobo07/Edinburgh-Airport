package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_arrow_forward
import edinburghairport.shared.generated.resources.ic_barcode_scan
import edinburghairport.shared.generated.resources.ic_check_circle
import edinburghairport.shared.generated.resources.ic_search
import io.github.keithlobo07.edinburghairport.model.FlightInfo
import io.github.keithlobo07.edinburghairport.model.FlightStateMode
import io.github.keithlobo07.edinburghairport.model.AppProfile
import io.github.keithlobo07.edinburghairport.theme.BorderLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.SurfaceMuted
import io.github.keithlobo07.edinburghairport.theme.TextMuted
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource

@Composable
fun PrimaryHeroCard(
    flightMode: FlightStateMode,
    appProfile: AppProfile,
    activeFlight: FlightInfo?,
    onModeSelect: (FlightStateMode) -> Unit,
    onSearchClick: () -> Unit,
    onScanBoardingPassClick: () -> Unit,
    onClearFlight: () -> Unit,
    onWayfindToGateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        if (appProfile == AppProfile.ADMIN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeSelectorChip(
                    label = "No Flight (A)",
                    selected = flightMode == FlightStateMode.NO_FLIGHT,
                    onClick = { onModeSelect(FlightStateMode.NO_FLIGHT) }
                )
                ModeSelectorChip(
                    label = "Departure (B)",
                    selected = flightMode == FlightStateMode.ACTIVE_DEPARTURE,
                    onClick = { onModeSelect(FlightStateMode.ACTIVE_DEPARTURE) }
                )
                ModeSelectorChip(
                    label = "Arrival (C)",
                    selected = flightMode == FlightStateMode.ACTIVE_ARRIVAL,
                    onClick = { onModeSelect(FlightStateMode.ACTIVE_ARRIVAL) }
                )
            }
        }

        // Hero Card Animated Container
        AnimatedContent(
            targetState = flightMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "HeroCardStateAnimation"
        ) { mode ->
            when (mode) {
                FlightStateMode.NO_FLIGHT -> {
                    StateANoFlight(
                        onSearchClick = onSearchClick,
                        onScanClick = onScanBoardingPassClick
                    )
                }
                FlightStateMode.ACTIVE_DEPARTURE -> {
                    StateBActiveDeparture(
                        flight = activeFlight,
                        onScanClick = onScanBoardingPassClick,
                        onClearClick = onClearFlight,
                        onWayfindClick = onWayfindToGateClick
                    )
                }
                FlightStateMode.ACTIVE_ARRIVAL -> {
                    StateCActiveArrival(
                        flight = activeFlight,
                        onClearClick = onClearFlight
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) EdinburghPlum else SurfaceMuted
    val textColor = if (selected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

/**
 * STATE A — No Flight Added
 * Matches Prototype Screenshot 1 exactly
 */
@Composable
private fun StateANoFlight(
    onSearchClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Add your flight",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = EdinburghNavy,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Bar with Scan Boarding Pass icon inside (matches prototype screenshot 1)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(26.dp))
                .clickable { onSearchClick() },
            color = SurfaceMuted
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search for your flight...",
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onScanClick() }
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_barcode_scan),
                        contentDescription = "Scan Boarding Pass",
                        tint = EdinburghNavy,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tap to search or scan your boarding pass",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "Scan Pass >",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = EdinburghPlum,
                modifier = Modifier.clickable { onScanClick() }
            )
        }
    }
}

/**
 * STATE B — Active Departure
 * High-contrast Live Flight Card with zero-tap critical gate info & offline badge
 */
@Composable
private fun StateBActiveDeparture(
    flight: FlightInfo?,
    onScanClick: () -> Unit,
    onClearClick: () -> Unit,
    onWayfindClick: () -> Unit
) {
    val currentFlight = flight ?: return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, EdinburghPlum.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header row: Flight # and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentFlight.flightNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = EdinburghNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentFlight.airline,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                // High Contrast Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentFlight.status.color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = currentFlight.statusText.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = currentFlight.status.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route & Times Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentFlight.originCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EdinburghNavy
                    )
                    Text(
                        text = currentFlight.origin,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_forward),
                            contentDescription = "To",
                            tint = EdinburghNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentFlight.destinationCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EdinburghNavy
                    )
                    Text(
                        text = currentFlight.destination,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Critical Times: Sched vs Est
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceLight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Scheduled: ${currentFlight.scheduledTime}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Estimated Departure: ${currentFlight.estimatedTime}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EdinburghNavy
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Zero-Tap Critical Info Grid: Check-in, Gate, Walk Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTile(
                    title = "CHECK-IN",
                    value = currentFlight.checkInDesk,
                    modifier = Modifier.weight(1.3f)
                )
                InfoTile(
                    title = "GATE",
                    value = currentFlight.gate ?: "TBA",
                    isHighlight = true,
                    subtitle = currentFlight.walkingTime,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline Resilience Pill & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check_circle),
                        contentDescription = "Cached",
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cached locally • Works offline",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Wayfind >",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EdinburghPlum,
                        modifier = Modifier.clickable { onWayfindClick() }
                    )
                    Text(
                        text = "Change",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.clickable { onClearClick() }
                    )
                }
            }
        }
    }
}

/**
 * STATE C — Arrival
 * High-contrast Live Arrival Card with Baggage Reclaim & Carousel details
 */
@Composable
private fun StateCActiveArrival(
    flight: FlightInfo?,
    onClearClick: () -> Unit
) {
    val currentFlight = flight ?: return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, StatusGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Flight & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentFlight.flightNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = EdinburghNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentFlight.airline,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = currentFlight.statusText.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Origin -> Edinburgh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentFlight.originCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EdinburghNavy
                    )
                    Text(
                        text = currentFlight.origin,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_forward),
                        contentDescription = "To",
                        tint = EdinburghNavy,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EDI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EdinburghNavy
                    )
                    Text(
                        text = "Edinburgh",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Zero-Tap Critical Arrival Info: Baggage Carousel & Estimated Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTile(
                    title = "BAGGAGE RECLAIM",
                    value = currentFlight.baggageCarousel ?: "Carousel 3",
                    isHighlight = true,
                    modifier = Modifier.weight(1.2f)
                )
                InfoTile(
                    title = "BAG DELIVERY",
                    value = currentFlight.baggageEstimatedTime ?: "~16:22",
                    subtitle = "Domestic / CTA",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transport Reminder Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EdinburghPlum.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome to Scotland! Trams & buses depart right outside.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = EdinburghPlum,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Change",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.clickable { onClearClick() }
                )
            }
        }
    }
}

@Composable
private fun InfoTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    subtitle: String? = null
) {
    val bg = if (isHighlight) EdinburghPlum.copy(alpha = 0.08f) else SurfaceLight
    val valueColor = if (isHighlight) EdinburghPlum else EdinburghNavy

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = valueColor
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
