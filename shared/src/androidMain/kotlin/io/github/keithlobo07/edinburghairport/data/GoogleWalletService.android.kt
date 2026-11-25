package io.github.keithlobo07.edinburghairport.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.github.keithlobo07.edinburghairport.model.FlightInfo
import java.io.File
import java.io.FileOutputStream

actual object GoogleWalletService {

    private const val WALLET_PACKAGE = "com.google.android.apps.walletnfcrel"

    actual fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean {
        val details = listOf(
            "SERVICE" to serviceName,
            "ROUTE" to route,
            "TICKET" to if (isReturn) "Open Return (Valid 90 Days)" else "Single (Valid Today)",
            "PASSENGERS" to "$passengerCount Adult${if (passengerCount > 1) "s" else ""}",
            "FARE PAID" to priceFormatted,
            "REFERENCE" to ticketRef
        )

        return createAndShareEverythingElsePass(
            title = serviceName,
            subtitle = if (isReturn) "Airport Open Return" else "Airport Single",
            barcodeValue = ticketRef,
            barcodeFormat = BarcodeFormat.QR_CODE,
            headerBgColor = 0xFF752969.toInt(), // EdinburghPlum
            details = details
        )
    }

    actual fun saveParkingPassToWallet(
        bookingRef: String,
        carParkName: String,
        assignedSpot: String,
        vehicleReg: String,
        days: Int,
        totalPriceFormatted: String
    ): Boolean {
        val details = listOf(
            "CAR PARK" to carParkName,
            "ASSIGNED BAY" to assignedSpot,
            "VEHICLE REG" to vehicleReg,
            "DURATION" to "$days Days Stay",
            "BARRIER ACCESS" to "Automatic ANPR Plate Recognition",
            "TOTAL PAID" to totalPriceFormatted,
            "BOOKING REF" to bookingRef
        )

        return createAndShareEverythingElsePass(
            title = "Airport Parking • $carParkName",
            subtitle = "Bay $assignedSpot • Reg: $vehicleReg",
            barcodeValue = bookingRef,
            barcodeFormat = BarcodeFormat.CODE_128,
            headerBgColor = 0xFF052145.toInt(), // EdinburghNavy
            details = details
        )
    }

    actual fun saveBoardingPassToWallet(flight: FlightInfo): Boolean {
        val details = listOf(
            "PASSENGER" to flight.passengerName,
            "FLIGHT" to flight.flightNumber,
            "ROUTE" to "${flight.originCode} → ${flight.destinationCode}",
            "SEAT" to flight.seat,
            "GROUP" to flight.boardingGroup,
            "GATE" to (flight.gate ?: "Gate TBA"),
            "BOOKING REF" to flight.bookingReference
        )

        val bcbpRaw = "M1${flight.passengerName.uppercase().take(20).padEnd(20, ' ')}E${flight.bookingReference} ${flight.originCode}${flight.destinationCode}${flight.flightNumber.replace(" ", "")} ${flight.seat}"

        return createAndShareEverythingElsePass(
            title = "Boarding Pass • ${flight.flightNumber}",
            subtitle = "${flight.airline} • Seat ${flight.seat}",
            barcodeValue = bcbpRaw,
            barcodeFormat = BarcodeFormat.PDF_417,
            headerBgColor = 0xFF752969.toInt(), // EdinburghPlum
            details = details
        )
    }

    /**
     * Creates an authentic, high-resolution Digital Pass card with the scannable barcode
     * and dispatches it directly to Google Wallet's native "Everything else" / Photo pass digitizer.
     */
    private fun createAndShareEverythingElsePass(
        title: String,
        subtitle: String,
        barcodeValue: String,
        barcodeFormat: BarcodeFormat,
        headerBgColor: Int,
        details: List<Pair<String, String>>
    ): Boolean {
        val activity = AndroidContextHolder.activity
        val context = AndroidContextHolder.context ?: activity ?: return false

        try {
            val passBitmap = renderPassCardBitmap(
                title = title,
                subtitle = subtitle,
                barcodeValue = barcodeValue,
                barcodeFormat = barcodeFormat,
                headerBgColor = headerBgColor,
                details = details
            )

            // Save bitmap to cache directory
            val cacheFolder = File(context.cacheDir, "passes").apply { mkdirs() }
            val sanitizedRef = barcodeValue.take(12).replace(Regex("[^A-Za-z0-9]"), "_")
            val passFile = File(cacheFolder, "pass_$sanitizedRef.png")
            FileOutputStream(passFile).use { out ->
                passBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Secure URI via FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                passFile
            )

            // Direct Intent to Google Wallet's "Everything else" / Photo receiver
            val walletIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nReference: $barcodeValue\nEdinburgh Airport")
                setPackage(WALLET_PACKAGE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            return try {
                context.startActivity(walletIntent)
                true
            } catch (_: ActivityNotFoundException) {
                // If direct package dispatch fails, open standard chooser targeting Wallet
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_TITLE, title)
                        putExtra(Intent.EXTRA_TEXT, "$title\nReference: $barcodeValue\nEdinburgh Airport")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Add Barcode Pass to Google Wallet"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            }
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Renders a clean, high-contrast Pass Card containing Edinburgh Airport branding,
     * pass details, and a scannable ZXing barcode/QR code.
     */
    private fun renderPassCardBitmap(
        title: String,
        subtitle: String,
        barcodeValue: String,
        barcodeFormat: BarcodeFormat,
        headerBgColor: Int,
        details: List<Pair<String, String>>
    ): Bitmap {
        val width = 760
        val height = 1140
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background
        paint.color = headerBgColor
        paint.style = Paint.Style.FILL
        val headerRect = RectF(0f, 0f, width.toFloat(), 220f)
        canvas.drawRect(headerRect, paint)

        // Header Small Caps: EDINBURGH AIRPORT
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EDINBURGH AIRPORT", 40f, 65f, paint)

        // Pass Title
        paint.textSize = 34f
        val displayTitle = if (title.length > 28) title.take(28) + "..." else title
        canvas.drawText(displayTitle, 40f, 125f, paint)

        // Pass Subtitle
        paint.textSize = 22f
        paint.color = 0xFFCBD5E1.toInt()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(subtitle, 40f, 175f, paint)

        // Pass Details Grid (Rows of Label & Value)
        var currentY = 270f
        val visibleDetails = details.take(4)
        for ((label, value) in visibleDetails) {
            paint.color = 0xFF64748B.toInt()
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(label, 40f, currentY, paint)

            paint.color = 0xFF0F172A.toInt()
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val displayValue = if (value.length > 36) value.take(36) + "..." else value
            canvas.drawText(displayValue, 40f, currentY + 32f, paint)

            currentY += 75f
        }

        // Dashed Divider Line above Barcode
        paint.color = 0xFFE2E8F0.toInt()
        paint.strokeWidth = 3f
        canvas.drawLine(40f, currentY + 10f, width - 40f, currentY + 10f, paint)

        // Render Barcode
        val barcodeStartY = currentY + 30f
        val barcodeWidth = if (barcodeFormat == BarcodeFormat.QR_CODE) 380 else 580
        val barcodeHeight = if (barcodeFormat == BarcodeFormat.QR_CODE) 380 else 160

        try {
            val barcodeBitmap = renderBarcode(barcodeValue, barcodeFormat, barcodeWidth, barcodeHeight)
            val barcodeX = (width - barcodeWidth) / 2f
            canvas.drawBitmap(barcodeBitmap, barcodeX, barcodeStartY, null)
        } catch (_: Exception) {
            // Fallback placeholder if format encode fails
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(80f, barcodeStartY, width - 80f, barcodeStartY + 140f, paint)
        }

        // Barcode Value Text below
        val textY = barcodeStartY + barcodeHeight + 40f
        paint.style = Paint.Style.FILL
        paint.color = 0xFF0F172A.toInt()
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val displayText = if (barcodeValue.length > 28) barcodeValue.take(28) + "..." else barcodeValue
        canvas.drawText(displayText, width / 2f, textY, paint)

        // Verification Footer
        paint.color = 0xFF94A3B8.toInt()
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Digital Pass • Valid at Airport E-Gates & Validators", width / 2f, textY + 36f, paint)

        paint.textAlign = Paint.Align.LEFT
        return bitmap
    }

    /**
     * Converts a barcode string into a ZXing black-and-white Bitmap.
     */
    private fun renderBarcode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int
    ): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(contents, format, width, height)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}
