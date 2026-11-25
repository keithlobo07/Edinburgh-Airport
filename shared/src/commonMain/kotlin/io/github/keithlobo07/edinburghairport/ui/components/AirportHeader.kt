package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_bell
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_logo_alt
import edinburghairport.shared.generated.resources.ic_profile
import edinburghairport.shared.generated.resources.ic_weather_light_rain
import io.github.keithlobo07.edinburghairport.model.FlightAlert
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusRed
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AirportHeader(
    modifier: Modifier = Modifier,
    localTime: String = "16:13",
    weatherSummary: String = "14°C • Light Rain",
    weatherIcon: DrawableResource = Res.drawable.ic_weather_light_rain,
    unreadAlertCount: Int = 3,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Edinburgh Airport Logo vector
                Image(
                    painter = painterResource(Res.drawable.ic_logo_alt),
                    contentDescription = "Edinburgh Airport",
                    modifier = Modifier.height(38.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.weight(1f))

                // Notification Bell
                Box {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_bell),
                            contentDescription = "Flight Alerts",
                            tint = EdinburghNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (unreadAlertCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(StatusRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Profile/Settings icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EdinburghPlum.copy(alpha = 0.12f))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_profile),
                        contentDescription = "Profile & Settings",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle: Edinburgh dynamic local time and live weather (enlarged & clear)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Icon(
                    painter = painterResource(weatherIcon),
                    contentDescription = "Weather",
                    tint = EdinburghPlum,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edinburgh $localTime",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = weatherSummary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun InAppNotificationToast(
    alert: FlightAlert,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 4000L
) {
    val shimmerTransition = rememberInfiniteTransition(label = "ToastProgress")
    val shimmerPosition by shimmerTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "ToastProgressShimmer"
    )

    LaunchedEffect(alert.id) {
        delay(durationMs)
        onDismiss()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pressClickEffect(),
        shape = RoundedCornerShape(12.dp),
        color = EdinburghNavy,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EdinburghPlum),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_bell),
                                contentDescription = "Notification",
                                tint = TextWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (alert.isUrgent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(StatusRed)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = alert.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextWhite,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alert.message,
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Smooth moving highlight along a fixed progress track
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            ) {
                val baseColor = if (alert.isUrgent) StatusRed else EdinburghPlum
                drawRect(baseColor.copy(alpha = 0.22f))

                val highlightWidth = size.width * 0.28f
                val centerX = size.width * shimmerPosition
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0f),
                            baseColor.copy(alpha = 0.7f),
                            baseColor,
                            baseColor.copy(alpha = 0f)
                        ),
                        startX = centerX - highlightWidth,
                        endX = centerX + highlightWidth
                    )
                )
            }
        }
    }
}
