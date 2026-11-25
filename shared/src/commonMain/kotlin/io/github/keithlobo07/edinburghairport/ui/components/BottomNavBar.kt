package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_nav_explore
import edinburghairport.shared.generated.resources.ic_nav_flights
import edinburghairport.shared.generated.resources.ic_nav_map
import edinburghairport.shared.generated.resources.ic_nav_my_trip
import edinburghairport.shared.generated.resources.ic_tower
import io.github.keithlobo07.edinburghairport.model.HomeNavigationTab
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AirportBottomNavBar(
    selectedTab: HomeNavigationTab,
    onTabSelected: (HomeNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(EdinburghPlum)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. My Trip
                NavTabItem(
                    isSelected = selectedTab == HomeNavigationTab.MY_TRIP,
                    label = HomeNavigationTab.MY_TRIP.label,
                    iconRes = Res.drawable.ic_nav_my_trip,
                    onClick = { onTabSelected(HomeNavigationTab.MY_TRIP) }
                )

                // 2. Flights
                NavTabItem(
                    isSelected = selectedTab == HomeNavigationTab.FLIGHTS,
                    label = HomeNavigationTab.FLIGHTS.label,
                    iconRes = Res.drawable.ic_nav_flights,
                    onClick = { onTabSelected(HomeNavigationTab.FLIGHTS) }
                )

                // 3. Home (Edinburgh Airport Control Tower icon)
                NavTabItem(
                    isSelected = selectedTab == HomeNavigationTab.HOME,
                    label = HomeNavigationTab.HOME.label,
                    iconRes = Res.drawable.ic_tower,
                    onClick = { onTabSelected(HomeNavigationTab.HOME) }
                )

                // 4. Explore
                NavTabItem(
                    isSelected = selectedTab == HomeNavigationTab.EXPLORE,
                    label = HomeNavigationTab.EXPLORE.label,
                    iconRes = Res.drawable.ic_nav_explore,
                    onClick = { onTabSelected(HomeNavigationTab.EXPLORE) }
                )

                // 5. Map
                NavTabItem(
                    isSelected = selectedTab == HomeNavigationTab.MAP,
                    label = HomeNavigationTab.MAP.label,
                    iconRes = Res.drawable.ic_nav_map,
                    onClick = { onTabSelected(HomeNavigationTab.MAP) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    isSelected: Boolean,
    label: String,
    iconRes: DrawableResource,
    onClick: () -> Unit
) {
    val tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = if (isSelected) {
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f))
            } else {
                Modifier.size(36.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
