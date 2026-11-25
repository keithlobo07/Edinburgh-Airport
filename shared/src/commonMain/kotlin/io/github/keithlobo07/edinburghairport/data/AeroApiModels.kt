package io.github.keithlobo07.edinburghairport.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AeroFlightsResponse(
    val departures: List<AeroFlight>? = null,
    val arrivals: List<AeroFlight>? = null,
    @SerialName("scheduled_departures") val scheduledDepartures: List<AeroFlight>? = null,
    @SerialName("scheduled_arrivals") val scheduledArrivals: List<AeroFlight>? = null,
    val links: AeroLinks? = null
)

@Serializable
data class AeroLinks(
    val next: String? = null
)

@Serializable
data class AeroFlight(
    val ident: String = "",
    @SerialName("ident_iata") val identIata: String? = null,
    @SerialName("operator_iata") val operatorIata: String? = null,
    val origin: AeroAirport? = null,
    val destination: AeroAirport? = null,
    @SerialName("scheduled_out") val scheduledOut: String? = null,
    @SerialName("scheduled_in") val scheduledIn: String? = null,
    @SerialName("estimated_out") val estimatedOut: String? = null,
    @SerialName("estimated_in") val estimatedIn: String? = null,
    @SerialName("actual_out") val actualOut: String? = null,
    @SerialName("actual_in") val actualIn: String? = null,
    val status: String? = null,
    @SerialName("gate_origin") val gateOrigin: String? = null,
    @SerialName("gate_destination") val gateDestination: String? = null,
    @SerialName("baggage_claim") val baggageClaim: String? = null,
    @SerialName("progress_percent") val progressPercent: Int? = null,
    val cancelled: Boolean? = null
)

@Serializable
data class AeroAirport(
    @SerialName("code_iata") val codeIata: String? = null,
    val city: String? = null,
    val name: String? = null
)
