package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightInfo
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreImage.CIFilter
import platform.CoreImage.filterWithName
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setValue
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.drawInRect
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual object GoogleWalletService {

    actual fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean {
        val payload = "EDI-TRANSIT|$ticketRef|$serviceName|$route|$passengerCount"
        val image = renderPassCardImage(
            category = "TRANSIT PASS",
            title = serviceName,
            subtitle = route,
            fields = listOf(
                "REF" to ticketRef,
                "TYPE" to if (isReturn) "Return" else "Single",
                "PAX" to passengerCount.toString(),
                "PRICE" to priceFormatted
            ),
            barcodePayload = payload
        )
        return presentShareSheet(image, payload)
    }

    actual fun saveParkingPassToWallet(
        bookingRef: String,
        carParkName: String,
        assignedSpot: String,
        vehicleReg: String,
        days: Int,
        totalPriceFormatted: String
    ): Boolean {
        val payload = "EDI-PARK|$bookingRef|$carParkName|$assignedSpot|$vehicleReg"
        val image = renderPassCardImage(
            category = "PARKING PASS",
            title = carParkName,
            subtitle = "Edinburgh Airport Official Parking",
            fields = listOf(
                "REF" to bookingRef,
                "BAY" to assignedSpot,
                "REG" to vehicleReg,
                "STAY" to "$days Day${if (days > 1) "s" else ""}",
                "TOTAL" to totalPriceFormatted
            ),
            barcodePayload = payload
        )
        return presentShareSheet(image, payload)
    }

    actual fun saveBoardingPassToWallet(flight: FlightInfo): Boolean {
        val payload = "M1PASSENGER/EDI       E${flight.flightNumber} EDI${flight.airportCode} ${flight.scheduledTime}"
        val image = renderPassCardImage(
            category = "BOARDING PASS",
            title = "${flight.airline} ${flight.flightNumber}",
            subtitle = "${flight.airportCode} - ${flight.destinationOrOrigin}",
            fields = listOf(
                "TIME" to flight.scheduledTime,
                "GATE" to (flight.gate ?: "TBA"),
                "STATUS" to flight.status.name
            ),
            barcodePayload = payload
        )
        return presentShareSheet(image, payload)
    }

    private fun renderPassCardImage(
        category: String,
        title: String,
        subtitle: String,
        fields: List<Pair<String, String>>,
        barcodePayload: String
    ): UIImage {
        val width = 880.0
        val height = 1140.0
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width, height))

        val plum = UIColor(red = 0.29, green = 0.08, blue = 0.29, alpha = 1.0)
        val white = UIColor.whiteColor
        val darkInk = UIColor(red = 0.06, green = 0.09, blue = 0.16, alpha = 1.0)
        val mutedInk = UIColor(red = 0.39, green = 0.45, blue = 0.55, alpha = 1.0)
        val cardBg = UIColor(red = 0.97, green = 0.98, blue = 0.99, alpha = 1.0)

        return renderer.imageWithActions {
            cardBg.setFill()
            UIBezierPath.bezierPathWithRect(CGRectMake(0.0, 0.0, width, height)).fill()

            // Header Banner
            plum.setFill()
            UIBezierPath.bezierPathWithRect(CGRectMake(0.0, 0.0, width, 160.0)).fill()

            drawText("EDINBURGH AIRPORT", 48.0, 38.0, width - 96.0, 32.0, 22.0, true, white)
            drawText(category, 48.0, 82.0, width - 96.0, 44.0, 32.0, true, white)

            // Title & Subtitle
            drawText(title, 48.0, 200.0, width - 96.0, 48.0, 36.0, true, darkInk)
            drawText(subtitle, 48.0, 256.0, width - 96.0, 34.0, 22.0, false, mutedInk)

            // Fields Grid
            var y = 324.0
            fields.forEach { (label, value) ->
                drawText(label, 48.0, y, 220.0, 34.0, 20.0, true, mutedInk)
                drawText(value, 280.0, y, width - 328.0, 36.0, 24.0, true, darkInk)
                y += 48.0
            }

            // QR Code Card Container
            val qrSize = 360.0
            val qrX = (width - qrSize) / 2.0
            val qrY = 620.0
            white.setFill()
            UIBezierPath.bezierPathWithRoundedRect(
                CGRectMake(qrX - 24.0, qrY - 24.0, qrSize + 48.0, qrSize + 48.0),
                cornerRadius = 24.0
            ).fill()

            generateQrImage(barcodePayload, qrSize)?.drawInRect(CGRectMake(qrX, qrY, qrSize, qrSize))

            drawText(barcodePayload, 48.0, 1045.0, width - 96.0, 36.0, 18.0, false, mutedInk)
        }
    }

    private fun drawText(
        text: String,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        size: Double,
        bold: Boolean,
        color: UIColor
    ) {
        val font = if (bold) UIFont.boldSystemFontOfSize(size) else UIFont.systemFontOfSize(size)
        val attrs = mapOf<Any?, Any?>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color
        )
        val nsStr = NSString.create(string = text)
        nsStr.drawInRect(CGRectMake(x, y, w, h), withAttributes = attrs)
    }

    private fun generateQrImage(payload: String, size: Double): UIImage? {
        val data: NSData = NSString.create(string = payload)
            .dataUsingEncoding(NSUTF8StringEncoding) ?: return null
        val filter = CIFilter.filterWithName("CIQRCodeGenerator") ?: return null
        filter.setValue(data, forKey = "inputMessage")
        filter.setValue("M", forKey = "inputCorrectionLevel")
        val ciImage = filter.outputImage ?: return null
        return UIImage.imageWithCIImage(ciImage)
    }

    private fun presentShareSheet(image: UIImage, payload: String): Boolean {
        dispatch_async(dispatch_get_main_queue()) {
            val rootVc = findTopViewController() ?: return@dispatch_async
            val activityVc = UIActivityViewController(
                activityItems = listOf(image, payload),
                applicationActivities = null
            )
            rootVc.presentViewController(activityVc, animated = true, completion = null)
        }
        return true
    }

    private fun findTopViewController(): UIViewController? {
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull { it is UIWindowScene } as? UIWindowScene
        val keyWindow = (windowScene?.windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow == true } as? UIWindow)
            ?: UIApplication.sharedApplication.keyWindow
        var vc = keyWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}
