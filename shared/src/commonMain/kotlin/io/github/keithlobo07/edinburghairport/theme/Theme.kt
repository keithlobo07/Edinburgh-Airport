package io.github.keithlobo07.edinburghairport.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EdinburghColorScheme = lightColorScheme(
    primary = EdinburghNavy,
    onPrimary = TextWhite,
    primaryContainer = EdinburghPlumSurface,
    onPrimaryContainer = EdinburghNavy,
    secondary = EdinburghPlum,
    onSecondary = TextWhite,
    secondaryContainer = EdinburghPlumSurface,
    onSecondaryContainer = EdinburghPlum,
    tertiary = EdinburghPlumDark,
    onTertiary = TextWhite,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun EdinburghAirportTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EdinburghColorScheme,
        typography = getEdinburghTypography(),
        content = content
    )
}
