package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keithlobo07.edinburghairport.model.LiveOpsData
import io.github.keithlobo07.edinburghairport.model.StatusLevel
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumDark

@Composable
fun LiveOpsSection(
    liveOps: LiveOpsData,
    onBookFastTrackClick: () -> Unit,
    onBuyTramClick: () -> Unit,
    onBuyBusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Security Wait Time Split Pill Card (matches screenshot 1)
        Text(
            text = "Security Hall",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = EdinburghNavy
        )
        SplitPillCard(
            liveStatusLevel = liveOps.securityStatusLevel,
            timeValue = "${liveOps.securityWaitMinutes} mins",
            subtitle = liveOps.securityLabel,
            actionTitle = "Book\nFast-Track\nPass",
            price = liveOps.fastTrackPrice,
            onActionClick = onBookFastTrackClick
        )

        // 2. Transport Header
        Text(
            text = "Transport",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = EdinburghNavy,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 3. Edinburgh Tram Split Pill Card (matches screenshot 2)
        SplitPillCard(
            liveStatusLevel = liveOps.tramStatusLevel,
            timeValue = formatTransportWait(liveOps.tramNextMinutes),
            subtitle = liveOps.tramLabel,
            actionTitle = "Buy Tram\nAirport Single",
            price = liveOps.tramPrice,
            onActionClick = onBuyTramClick
        )

        // 4. Airlink 100 Bus Split Pill Card (matches screenshot 2 & 3)
        SplitPillCard(
            liveStatusLevel = liveOps.busStatusLevel,
            timeValue = formatTransportWait(liveOps.busNextMinutes),
            subtitle = liveOps.busLabel,
            actionTitle = "Buy Airlink\n100 ticket",
            price = liveOps.busPrice,
            onActionClick = onBuyBusClick
        )
    }
}

private fun formatTransportWait(minutes: Int): String {
    return when {
        minutes <= 0 -> "Due"
        minutes == 1 -> "1 min"
        else -> "$minutes mins"
    }
}

/**
 * Split Pill Card Component matching prototype screenshots 1, 2, and 3:
 * Left side: Plum (#752969) with live dot, minute counter, and label.
 * Right side: Darker plum (#5A1F51) with action description and price.
 */
@Composable
fun SplitPillCard(
    liveStatusLevel: StatusLevel,
    timeValue: String,
    subtitle: String,
    actionTitle: String,
    price: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(36.dp)),
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: Live Ops Time (Rounded left half)
            Box(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 36.dp,
                            bottomStart = 36.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(EdinburghPlum)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // "● Live" pill badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(liveStatusLevel.color)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Live",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Large time number
                    Text(
                        text = timeValue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subtitle (e.g. security hall time / Next tram / Next bus)
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Right Side: Action & Price (Rounded right half) with subtle gradient
            Box(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight()
                    .pressClickEffect()
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 36.dp,
                            bottomEnd = 36.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EdinburghPlumDark,
                                Color(0xFF48163F)
                            )
                        )
                    )
                    .clickable { onActionClick() }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = actionTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = price,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
