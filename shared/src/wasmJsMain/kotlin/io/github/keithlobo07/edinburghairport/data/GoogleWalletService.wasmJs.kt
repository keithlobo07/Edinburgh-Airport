@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightInfo

@JsFun("""(fileName, title, subtitle, headerColor, barcodeValue) => {
    const canvas = document.createElement('canvas');
    canvas.width = 720;
    canvas.height = 460;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, 720, 460);
    ctx.fillStyle = headerColor;
    ctx.fillRect(0, 0, 720, 150);
    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 18px sans-serif';
    ctx.fillText('EDINBURGH AIRPORT', 32, 44);
    ctx.font = 'bold 28px sans-serif';
    ctx.fillText(title, 32, 92);
    ctx.fillStyle = '#CBD5E1';
    ctx.font = '18px sans-serif';
    ctx.fillText(subtitle, 32, 128);
    ctx.fillStyle = '#0F172A';
    let x = 60;
    for (let i = 0; i < barcodeValue.length; i++) {
        const code = barcodeValue.charCodeAt(i) + i * 7;
        for (let b = 0; b < 6; b++) {
            const w = ((code >> b) & 1) ? 6 : 2.5;
            if (b % 2 === 0 && x + w < 660) ctx.fillRect(x, 200, w, 120);
            x += w + 3;
        }
    }
    ctx.font = 'bold 22px monospace';
    ctx.fillText(barcodeValue, 32, 375);
    const a = document.createElement('a');
    a.href = canvas.toDataURL('image/png');
    a.download = fileName;
    a.click();
    return true;
}""")
private external fun wasmDownloadPassCard(
    fileName: String,
    title: String,
    subtitle: String,
    headerColor: String,
    barcodeValue: String
): Boolean

actual object GoogleWalletService {

    actual fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean = wasmDownloadPassCard(
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
    ): Boolean = wasmDownloadPassCard(
        fileName = "EdinburghAirport_Parking_${bookingRef.replace("-", "_")}.png",
        title = "Parking • $carParkName",
        subtitle = "Bay $assignedSpot • Reg: $vehicleReg • $totalPriceFormatted",
        headerColor = "#052145",
        barcodeValue = bookingRef
    )

    actual fun saveBoardingPassToWallet(flight: FlightInfo): Boolean = wasmDownloadPassCard(
        fileName = "EdinburghAirport_Boarding_${flight.flightNumber.replace(" ", "_")}.png",
        title = "Boarding Pass • ${flight.flightNumber}",
        subtitle = "${flight.originCode} -> ${flight.destinationCode} • Seat ${flight.seat}",
        headerColor = "#752969",
        barcodeValue = flight.bookingReference
    )
}
