package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_arrow_back
import edinburghairport.shared.generated.resources.ic_arrow_forward
import io.github.keithlobo07.edinburghairport.data.EdinburghData
import io.github.keithlobo07.edinburghairport.model.VisitorAttraction
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun VisitEdinburghSection(
    onAttractionClick: (VisitorAttraction) -> Unit = {},
    modifier: Modifier = Modifier,
    attractions: List<VisitorAttraction> = EdinburghData.visitorAttractions
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Title: "Visit Edinburgh" (bold text outside the banner)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Visit Edinburgh",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = EdinburghNavy
            )
            Text(
                text = "Official Guide >",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = EdinburghPlum,
                modifier = Modifier.clickable {
                    try {
                        uriHandler.openUri("https://www.visitscotland.com/places-to-go/edinburgh")
                    } catch (e: Exception) {
                        if (attractions.isNotEmpty()) onAttractionClick(attractions.first())
                    }
                }
            )
        }

        // Plum Banner Container (#752969) matching prototype screenshot 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EdinburghPlum)
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                attractions.forEach { attraction ->
                    AttractionCard(
                        attraction = attraction,
                        onClick = {
                            try {
                                uriHandler.openUri(attraction.websiteUrl)
                            } catch (e: Exception) {
                                onAttractionClick(attraction)
                            }
                        }
                    )
                }
            }

            if (scrollState.value > 8) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, Color.White, CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                scrollState.animateScrollTo((scrollState.value - 600).coerceAtLeast(0))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = "Scroll back to previous attractions",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (scrollState.value < scrollState.maxValue - 8) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, Color.White, CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                scrollState.animateScrollTo((scrollState.value + 600).coerceAtMost(scrollState.maxValue))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_forward),
                        contentDescription = "Scroll forward to more attractions",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttractionCard(
    attraction: VisitorAttraction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Card styling with darkish plum gradient background
    Surface(
        modifier = modifier
            .width(220.dp)
            .height(265.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color(0xFF3B1435)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Real Visitor Photography
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (attraction.imageResource != null) {
                    Image(
                        painter = painterResource(attraction.imageResource),
                        contentDescription = attraction.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(EdinburghPlum)
                    )
                }

                // Category Tag badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.50f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = attraction.category,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            // Content Section with Darkish Plum Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4A1842),
                                Color(0xFF2D0E29)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = attraction.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attraction.shortDescription,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = attraction.durationFromAirport,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "Website >",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFFDE047)
                        )
                    }
                }
            }
        }
    }
}
