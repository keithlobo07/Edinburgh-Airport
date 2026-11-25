package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightInfo
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement

actual object GoogleWalletService {

    actual fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean = downloadPassCard(
        fileName = "EdinburghAirport_Transit_${ticketRef.replace("-", "_")}.png",
        title = serviceName,
        subtitle = "$route • $priceFormatted",
        headerColor = "#752969",
        barcodeValue = ticketRef
    )

    actual fun saveParkingPassToWallet(
        bookingRef: String,
        carParkName: String,
        assignedSpot: String,
        vehicleReg: String,
        days: Int,
        totalPriceFormatted: String
    ): Boolean = downloadPassCard(
        fileName = "EdinburghAirport_Parking_${bookingRef.replace("-", "_")}.png",
        title = "Parking • $carParkName",
        subtitle = "Bay $assignedSpot • Reg: $vehicleReg • $totalPriceFormatted",
        headerColor = "#052145",
        barcodeValue = bookingRef
    )

    actual fun saveBoardingPassToWallet(flight: FlightInfo): Boolean = downloadPassCard(
        fileName = "EdinburghAirport_Boarding_${flight.flightNumber.replace(" ", "_")}.png",
        title = "Boarding Pass • ${flight.flightNumber}",
        subtitle = "${flight.originCode} -> ${flight.destinationCode} • Seat ${flight.seat}",
        headerColor = "#752969",
        barcodeValue = flight.bookingReference
    )

    private fun downloadPassCard(
        fileName: String,
        title: String,
        subtitle: String,
        headerColor: String,
        barcodeValue: String
    ): Boolean {
        return try {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = 720
            canvas.height = 460
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

            ctx.fillStyle = "#FFFFFF"
            ctx.fillRect(0.0, 0.0, 720.0, 460.0)

            ctx.fillStyle = headerColor
            ctx.fillRect(0.0, 0.0, 720.0, 150.0)

            ctx.fillStyle = "#FFFFFF"
            ctx.font = "bold 18px sans-serif"
            ctx.fillText("EDINBURGH AIRPORT", 32.0, 44.0)
            ctx.font = "bold 28px sans-serif"
            ctx.fillText(title, 32.0, 92.0)
            ctx.fillStyle = "#CBD5E1"
            ctx.font = "18px sans-serif"
            ctx.fillText(subtitle, 32.0, 128.0)

            ctx.fillStyle = "#0F172A"
            var x = 60.0
            barcodeValue.forEachIndexed { idx, ch ->
                val code = ch.code + idx * 7
                for (bit in 0 until 6) {
                    val w = if (((code shr bit) and 1) == 1) 6.0 else 2.5
                    if (bit % 2 == 0 && x + w < 660.0) {
                        ctx.fillRect(x, 200.0, w, 120.0)
                    }
                    x += w + 3.0
                }
            }
            ctx.font = "bold 22px monospace"
            ctx.fillText(barcodeValue, 32.0, 375.0)

            val link = document.createElement("a") as HTMLAnchorElement
            link.href = canvas.toDataURL("image/png")
            link.download = fileName
            link.click()
            true
        } catch (_: Throwable) {
            false
        }
    }
}
