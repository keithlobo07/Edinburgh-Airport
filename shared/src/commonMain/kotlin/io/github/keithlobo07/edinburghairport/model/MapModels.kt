package io.github.keithlobo07.edinburghairport.model

import androidx.compose.ui.graphics.Color
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusBlue
import io.github.keithlobo07.edinburghairport.theme.StatusGreen

enum class TerminalLevel(val label: String, val shortLabel: String, val subtitle: String) {
    LEVEL_0("Ground Floor", "L0", "Check-in, Arrivals & Transport"),
    LEVEL_1("First Floor", "L1", "Departures, Security & Gates")
}

enum class PlaceCategory(val label: String, val pinColor: Color) {
    ALL("All", EdinburghNavy),
    GATES("Gates", EdinburghPlum),
    SECURITY("Security", StatusAmber),
    DINING("Dining", Color(0xFFD97706)),
    SHOPPING("Shops", Color(0xFF0284C7)),
    LOUNGES("Lounges", Color(0xFF7C3AED)),
    SERVICES("Services", StatusGreen),
    TRANSPORT("Transport", StatusBlue)
}

data class TerminalPlace(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val level: TerminalLevel,
    val x: Float, // Normalized 0.0 .. 1.0 on terminal map coordinate space
    val y: Float, // Normalized 0.0 .. 1.0
    val subtitle: String,
    val description: String,
    val operatingHours: String = "Open daily",
    val walkingTimeMin: Int = 3,
    val walkingDistanceMeters: Int = 180,
    val gateNumber: Int? = null,
    val currentFlight: String? = null
)

enum class StepIcon {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    DESTINATION
}

data class NavigationStep(
    val instruction: String,
    val distanceMeters: Int,
    val iconType: StepIcon = StepIcon.STRAIGHT
)

data class WayfindingRoute(
    val fromName: String,
    val toPlace: TerminalPlace,
    val estimatedMinutes: Int,
    val distanceMeters: Int,
    val waypoints: List<Pair<Float, Float>>,
    val steps: List<NavigationStep> = emptyList()
)
