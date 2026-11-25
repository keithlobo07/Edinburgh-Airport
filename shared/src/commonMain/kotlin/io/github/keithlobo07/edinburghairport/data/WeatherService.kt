package io.github.keithlobo07.edinburghairport.data

import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_weather_clear_night
import edinburghairport.shared.generated.resources.ic_weather_cloudy
import edinburghairport.shared.generated.resources.ic_weather_fog
import edinburghairport.shared.generated.resources.ic_weather_heavy_rain
import edinburghairport.shared.generated.resources.ic_weather_light_rain
import edinburghairport.shared.generated.resources.ic_weather_night_rain_shower
import edinburghairport.shared.generated.resources.ic_weather_partly_cloudy
import edinburghairport.shared.generated.resources.ic_weather_partly_cloudy_night
import edinburghairport.shared.generated.resources.ic_weather_rain_shower
import edinburghairport.shared.generated.resources.ic_weather_sleet
import edinburghairport.shared.generated.resources.ic_weather_snow
import edinburghairport.shared.generated.resources.ic_weather_sunny
import edinburghairport.shared.generated.resources.ic_weather_thunderstorm
import io.github.keithlobo07.edinburghairport.fetchHttpUrl
import io.github.keithlobo07.edinburghairport.getEdinburghCurrentTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.roundToInt

data class LiveWeatherData(
    val temperatureCelsius: Int = 10,
    val conditionText: String = "Partly Cloudy",
    val iconResource: DrawableResource = Res.drawable.ic_weather_partly_cloudy,
    val weatherSummary: String = "10°C • Partly Cloudy"
)

class WeatherAndTimeService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _currentTime = MutableStateFlow(getEdinburghCurrentTime())
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentWeather = MutableStateFlow(
        LiveWeatherData(
            temperatureCelsius = 9,
            conditionText = "Light Rain",
            iconResource = Res.drawable.ic_weather_light_rain,
            weatherSummary = "9°C • Light Rain"
        )
    )
    val currentWeather: StateFlow<LiveWeatherData> = _currentWeather.asStateFlow()

    init {
        startTimeTicker()
        startWeatherSync()
    }

    private fun startTimeTicker() {
        scope.launch {
            while (isActive) {
                _currentTime.value = getEdinburghCurrentTime()
                delay(5_000) // update every 5 seconds
            }
        }
    }

    private fun startWeatherSync() {
        scope.launch {
            while (isActive) {
                fetchLiveEdinburghWeather()
                delay(600_000) // update every 10 minutes
            }
        }
    }

    suspend fun refreshNow() {
        fetchLiveEdinburghWeather()
    }

    private suspend fun fetchLiveEdinburghWeather() {
        // Open-Meteo Edinburgh Airport coordinates (Lat 55.95, Lon -3.37)
        val url = "https://api.open-meteo.com/v1/forecast?latitude=55.95&longitude=-3.37&current=temperature_2m,weather_code,is_day"
        try {
            val json = fetchHttpUrl(url) ?: return
            
            val tempMatch = Regex(""""temperature_2m"\s*:\s*(-?[\d.]+)""").find(json)
            val codeMatch = Regex(""""weather_code"\s*:\s*(\d+)""").find(json)
            val isDayMatch = Regex(""""is_day"\s*:\s*(\d+)""").find(json)

            if (tempMatch != null && codeMatch != null) {
                val tempFloat = tempMatch.groupValues[1].toFloatOrNull() ?: 10f
                val tempInt = tempFloat.roundToInt()
                val code = codeMatch.groupValues[1].toIntOrNull() ?: 3
                val isDay = isDayMatch?.groupValues?.get(1)?.toIntOrNull() != 0

                val (condition, icon) = mapWmoWeather(code, isDay)

                _currentWeather.value = LiveWeatherData(
                    temperatureCelsius = tempInt,
                    conditionText = condition,
                    iconResource = icon,
                    weatherSummary = "${tempInt}°C • $condition"
                )
            }
        } catch (e: Exception) {
            // Keep existing/cached weather gracefully
        }
    }

    private fun mapWmoWeather(code: Int, isDay: Boolean): Pair<String, DrawableResource> {
        return when (code) {
            0 -> if (isDay) "Sunny" to Res.drawable.ic_weather_sunny else "Clear" to Res.drawable.ic_weather_clear_night
            1, 2 -> if (isDay) "Partly Cloudy" to Res.drawable.ic_weather_partly_cloudy else "Partly Cloudy" to Res.drawable.ic_weather_partly_cloudy_night
            3 -> "Overcast" to Res.drawable.ic_weather_cloudy
            45, 48 -> "Mist & Fog" to Res.drawable.ic_weather_fog
            51, 53, 55 -> "Light Drizzle" to Res.drawable.ic_weather_light_rain
            56, 57 -> "Freezing Drizzle" to Res.drawable.ic_weather_sleet
            61, 63 -> "Light Rain" to Res.drawable.ic_weather_light_rain
            65 -> "Heavy Rain" to Res.drawable.ic_weather_heavy_rain
            66, 67 -> "Sleet" to Res.drawable.ic_weather_sleet
            71, 73, 75, 77 -> "Snow" to Res.drawable.ic_weather_snow
            80, 81 -> if (isDay) "Rain Showers" to Res.drawable.ic_weather_rain_shower else "Rain Showers" to Res.drawable.ic_weather_night_rain_shower
            82 -> "Heavy Showers" to Res.drawable.ic_weather_heavy_rain
            85, 86 -> "Snow Showers" to Res.drawable.ic_weather_snow
            95, 96, 99 -> "Thunderstorm" to Res.drawable.ic_weather_thunderstorm
            else -> if (isDay) "Partly Cloudy" to Res.drawable.ic_weather_partly_cloudy else "Clear" to Res.drawable.ic_weather_clear_night
        }
    }
}
