package io.github.keithlobo07.edinburghairport.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.keithlobo07.edinburghairport.getAeroApiTimeRange

private const val BASE_URL = "https://aeroapi.flightaware.com/aeroapi"
private const val API_ORIGIN = "https://aeroapi.flightaware.com"
private const val AERO_API_KEY = "4WS0W1QFFzsJzBzErIYmOWeFUr6quD0z"

/**
 * Thin wrapper around the FlightAware AeroAPI v4.
 * Rate-limited: fetch once on screen open + manual refresh only. No polling.
 */
object AeroApiService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /** Fetches today's departures from Edinburgh (EDI). Returns empty list on any failure. */
    suspend fun fetchDepartures(): List<AeroFlight> =
        fetchAllPages { response ->
            (response.departures.orEmpty() + response.scheduledDepartures.orEmpty())
        }

    /** Fetches today's arrivals into Edinburgh (EDI). Returns empty list on any failure. */
    suspend fun fetchArrivals(): List<AeroFlight> =
        fetchAllPages { response ->
            (response.arrivals.orEmpty() + response.scheduledArrivals.orEmpty())
        }

    private suspend fun fetchAllPages(
        flightsFrom: (AeroFlightsResponse) -> List<AeroFlight>
    ): List<AeroFlight> = try {
        val (start, end) = getAeroApiTimeRange()
        var url: String? = "$BASE_URL/airports/EDI/flights"
        var firstPage = true
        val flights = mutableListOf<AeroFlight>()
        var previousUrl: String? = null

        while (!url.isNullOrBlank() && url != previousUrl) {
            val currentUrl = url
            previousUrl = currentUrl
            val response: AeroFlightsResponse = client.get(currentUrl) {
                header("x-apikey", AERO_API_KEY)
                if (firstPage) {
                    parameter("type", "Airline")
                    parameter("start", start)
                    parameter("end", end)
                    parameter("max_pages", 100)
                }
            }
            .body()
            firstPage = false
            flights.addAll(flightsFrom(response))

            url = response.links?.next?.let { next ->
                when {
                    next.startsWith("http") -> next
                    next.startsWith("/aeroapi/") -> API_ORIGIN + next
                    next.startsWith("/") -> API_ORIGIN + "/aeroapi" + next
                    else -> "$BASE_URL/$next"
                }
            }
        }

        flights
    } catch (e: Exception) {
        emptyList()
    }
}
