package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import kotlinx.coroutines.delay

/**
 * Interactive tactile click effect modifier with spring physics.
 */
fun Modifier.pressClickEffect(): Modifier = this.composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonPressScale"
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun TowerLoadingIndicator(
    modifier: Modifier = Modifier,
    segmentColor: Color = EdinburghPlum
) {
    val segments = remember { List(4) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        while (true) {
            segments.forEach { it.snapTo(0f) }
            delay(80)
            for (index in 3 downTo 0) {
                segments[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                )
                delay(100)
            }
            delay(350)
        }
    }

    Column(
        modifier = modifier.width(42.dp).height(44.dp),
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
                    .background(if (index == 3) segmentColor else segmentColor.copy(alpha = 0.72f))
            )
        }
    }
}

@Composable
fun CenteredTowerLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TowerLoadingIndicator(
            modifier = Modifier.size(72.dp),
            segmentColor = EdinburghPlum
        )
    }
}
