package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Camera Barcode Scanner component with platform-specific implementations.
 * Real CameraX + ZXing on Android; simulated viewfinder on desktop/web/iOS.
 */
@Composable
expect fun CameraBarcodeScanner(
    modifier: Modifier = Modifier,
    isTorchOn: Boolean = false,
    onBarcodeScanned: (String) -> Unit
)
