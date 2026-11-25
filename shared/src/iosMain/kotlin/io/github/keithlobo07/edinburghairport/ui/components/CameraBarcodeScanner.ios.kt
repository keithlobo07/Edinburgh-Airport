package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.hasTorch
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraBarcodeScanner(
    modifier: Modifier,
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    var hasCameraPermission by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
        )
    }

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        if (status == AVAuthorizationStatusNotDetermined) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                hasCameraPermission = granted
            }
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier.background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera permission required to scan boarding passes",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val captureDevice = remember { AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) }
    if (captureDevice == null) {
        Box(
            modifier = modifier.background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No camera hardware available on this device",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        }
        return
    }

    val session = remember {
        AVCaptureSession().apply {
            sessionPreset = AVCaptureSessionPresetHigh
        }
    }

    val metadataDelegate = remember(onBarcodeScanned) {
        BarcodeMetadataDelegate(onBarcodeScanned)
    }

    LaunchedEffect(isTorchOn, captureDevice) {
        if (captureDevice.hasTorch) {
            if (captureDevice.lockForConfiguration(null)) {
                captureDevice.torchMode = if (isTorchOn) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                captureDevice.unlockForConfiguration()
            }
        }
    }

    DisposableEffect(session, captureDevice, metadataDelegate) {
        val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, error = null)
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
        }

        val output = AVCaptureMetadataOutput()
        if (session.canAddOutput(output)) {
            session.addOutput(output)
            output.setMetadataObjectsDelegate(metadataDelegate, queue = dispatch_get_main_queue())
            output.metadataObjectTypes = listOf(
                AVMetadataObjectTypeQRCode,
                AVMetadataObjectTypePDF417Code,
                AVMetadataObjectTypeAztecCode,
                AVMetadataObjectTypeCode128Code
            )
        }

        if (!session.isRunning()) {
            session.startRunning()
        }

        onDispose {
            if (session.isRunning()) {
                session.stopRunning()
            }
        }
    }

    UIKitView(
        factory = {
            ScannerPreviewView(session)
        },
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalForeignApi::class)
private class ScannerPreviewView(
    session: AVCaptureSession
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.frame = bounds
    }
}

private class BarcodeMetadataDelegate(
    private val onBarcodeScanned: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    private var lastDelivered: String? = null

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val obj = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject ?: return
        val value = obj.stringValue ?: return
        if (value.isNotBlank() && value != lastDelivered) {
            lastDelivered = value
            onBarcodeScanned(value)
        }
    }
}
