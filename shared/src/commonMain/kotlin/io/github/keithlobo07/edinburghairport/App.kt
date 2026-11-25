package io.github.keithlobo07.edinburghairport

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.keithlobo07.edinburghairport.theme.EdinburghAirportTheme
import io.github.keithlobo07.edinburghairport.ui.HomeScreen

@Composable
@Preview
fun App() {
    EdinburghAirportTheme {
        HomeScreen()
    }
}
