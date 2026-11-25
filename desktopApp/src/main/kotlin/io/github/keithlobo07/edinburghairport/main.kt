package io.github.keithlobo07.edinburghairport

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "EdinburghAirport",
    ) {
        App()
    }
}