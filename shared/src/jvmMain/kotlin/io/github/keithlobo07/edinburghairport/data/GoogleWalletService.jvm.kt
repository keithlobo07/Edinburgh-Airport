package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightInfo
import java.awt.Color
import java.awt.Desktop
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

actual object GoogleWalletService {

    actual fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean = exportPassCardPng(
        fileName = "EdinburghAirport_Transit_${ticketRef.replace("-", "_")}.png",
        title = serviceName,
        subtitle = if (isReturn) "Airport Open Return" else "Airport Single",
        headerColor = Color(0x75, 0x29, 0x69),
        barcodeValue = ticketRef,
        details = listOf(
            "ROUTE" to route,
            "PASSENGERS" to "$passengerCount Adult${if (passengerCount > 1) "s" else ""}",
            "FARE PAID" to priceFormatted,
            "REFERENCE" to ticketRef
        )
    )

    actual fun saveParkingPassToWallet(
        bookingRef: String,
        carParkName: String,
        assignedSpot: String,
        vehicleReg: String,
        days: Int,
        totalPriceFormatted: String
    ): Boolean = exportPassCardPng(
        fileName = "EdinburghAirport_Parking_${bookingRef.replace("-", "_")}.png",
        title = "Parking • $carParkName",
        subtitle = "Bay $assignedSpot • Reg: $vehicleReg",
        headerColor = Color(0x05, 0x21, 0x45),
        barcodeValue = bookingRef,
        details = listOf(
            "CAR PARK" to carParkName,
            "VEHICLE REG" to vehicleReg,
            "DURATION" to "$days Days Stay",
            "TOTAL PAID" to totalPriceFormatted
        )
    )

    actual fun saveBoardingPassToWallet(flight: FlightInfo): Boolean = exportPassCardPng(
        fileName = "EdinburghAirport_Boarding_${flight.flightNumber.replace(" ", "_")}.png",
        title = "Boarding Pass • ${flight.flightNumber}",
        subtitle = "${flight.airline} • Seat ${flight.seat}",
        headerColor = Color(0x75, 0x29, 0x69),
        barcodeValue = flight.bookingReference,
        details = listOf(
            "PASSENGER" to flight.passengerName,
            "ROUTE" to "${flight.originCode} -> ${flight.destinationCode}",
            "GATE" to (flight.gate ?: "TBA"),
            "GROUP" to flight.boardingGroup
        )
    )

    private fun exportPassCardPng(
        fileName: String,
        title: String,
        subtitle: String,
        headerColor: Color,
        barcodeValue: String,
        details: List<Pair<String, String>>
    ): Boolean {
        return try {
            val width = 760
            val height = 1000
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g: Graphics2D = image.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            g.color = Color.WHITE
            g.fillRect(0, 0, width, height)

            g.color = headerColor
            g.fillRect(0, 0, width, 210)

            g.color = Color.WHITE
            g.font = Font(Font.SANS_SERIF, Font.BOLD, 20)
            g.drawString("EDINBURGH AIRPORT", 40, 60)

            g.font = Font(Font.SANS_SERIF, Font.BOLD, 32)
            g.drawString(title.take(30), 40, 120)

            g.color = Color(0xCB, 0xD5, 0xE1)
            g.font = Font(Font.SANS_SERIF, Font.PLAIN, 20)
            g.drawString(subtitle, 40, 168)

            var y = 265
            details.forEach { (label, value) ->
                g.color = Color(0x64, 0x74, 0x8B)
                g.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
                g.drawString(label, 40, y)

                g.color = Color(0x0F, 0x17, 0x2A)
                g.font = Font(Font.SANS_SERIF, Font.BOLD, 24)
                g.drawString(value.take(38), 40, y + 30)
                y += 76
            }

            g.color = Color(0xE2, 0xE8, 0xF0)
            g.drawLine(40, y + 10, width - 40, y + 10)

            // Deterministic 1D barcode bars
            val barY = y + 40
            val barHeight = 130
            var barX = 90
            g.color = Color(0x0F, 0x17, 0x2A)
            barcodeValue.forEachIndexed { idx, ch ->
                val code = ch.code + idx * 7
                for (bit in 0 until 6) {
                    val w = if (((code shr bit) and 1) == 1) 5 else 2
                    if (bit % 2 == 0 && barX + w < width - 90) {
                        g.fillRect(barX, barY, w, barHeight)
                    }
                    barX += w + 3
                }
            }

            g.font = Font(Font.MONOSPACED, Font.BOLD, 24)
            g.drawString(barcodeValue, 40, barY + barHeight + 45)
            g.dispose()

            val downloadsDir = File(System.getProperty("user.home"), "Downloads").apply { mkdirs() }
            val outFile = File(downloadsDir, fileName)
            ImageIO.write(image, "png", outFile)
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(outFile)
                } catch (_: Exception) {}
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
