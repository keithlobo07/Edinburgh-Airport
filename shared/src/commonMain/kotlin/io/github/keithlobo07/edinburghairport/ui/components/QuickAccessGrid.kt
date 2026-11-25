package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_quick_lounge
import edinburghairport.shared.generated.resources.ic_quick_map
import edinburghairport.shared.generated.resources.ic_quick_parking
import edinburghairport.shared.generated.resources.ic_quick_shopping
import io.github.keithlobo07.edinburghairport.model.QuickAccessType
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuickAccessGrid(
    onItemClick: (QuickAccessType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Quick Access",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = EdinburghNavy,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // 2x2 Grid of Navy (#052145) Cards matching screenshots 2 & 3
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Row 1: Terminal Map & Parking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuickAccessCard(
                    title = "Terminal Map",
                    iconRes = Res.drawable.ic_quick_map,
                    onClick = { onItemClick(QuickAccessType.TERMINAL_MAP) },
                    modifier = Modifier.weight(1f)
                )

                QuickAccessCard(
                    title = "Parking",
                    iconRes = Res.drawable.ic_quick_parking,
                    onClick = { onItemClick(QuickAccessType.PARKING) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Shops & Dining & Lounge Access
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuickAccessCard(
                    title = "Shops & Dining",
                    iconRes = Res.drawable.ic_quick_shopping,
                    onClick = { onItemClick(QuickAccessType.SHOP_AND_DINE) },
                    modifier = Modifier.weight(1f)
                )

                QuickAccessCard(
                    title = "Lounge Access",
                    iconRes = Res.drawable.ic_quick_lounge,
                    onClick = { onItemClick(QuickAccessType.LOUNGE_ACCESS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    iconRes: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "QuickAccessCardScale"
    )

    Surface(
        modifier = modifier
            .aspectRatio(1.08f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = Color.Transparent,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A3366),
                            Color(0xFF031633)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
