package io.github.keithlobo07.edinburghairport.data

import io.github.keithlobo07.edinburghairport.model.FlightInfo

expect object GoogleWalletService {
    /**
     * Generates a scannable Transit Ticket pass card image and shares it to Google Wallet ("Everything else").
     */
    fun saveTransitPassToWallet(
        ticketRef: String,
        serviceName: String,
        route: String,
        priceFormatted: String,
        passengerCount: Int,
        isReturn: Boolean
    ): Boolean

    /**
     * Generates a scannable Edinburgh Airport Parking pass card image and shares it to Google Wallet ("Everything else").
     */
    fun saveParkingPassToWallet(
        bookingRef: String,
        carParkName: String,
        assignedSpot: String,
        vehicleReg: String,
        days: Int,
        totalPriceFormatted: String
    ): Boolean

    /**
     * Adds an airline Boarding Pass to Google Wallet.
     */
    fun saveBoardingPassToWallet(
        flight: FlightInfo
    ): Boolean
}
