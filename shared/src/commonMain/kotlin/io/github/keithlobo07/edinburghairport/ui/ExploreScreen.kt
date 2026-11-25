package io.github.keithlobo07.edinburghairport.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_bus
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_location_pin
import edinburghairport.shared.generated.resources.ic_quick_lounge
import edinburghairport.shared.generated.resources.ic_quick_parking
import edinburghairport.shared.generated.resources.ic_quick_shopping
import edinburghairport.shared.generated.resources.ic_search
import edinburghairport.shared.generated.resources.ic_tram
import io.github.keithlobo07.edinburghairport.data.EdinburghData
import io.github.keithlobo07.edinburghairport.data.TerminalMapData
import io.github.keithlobo07.edinburghairport.model.PlaceCategory
import io.github.keithlobo07.edinburghairport.model.TerminalPlace
import io.github.keithlobo07.edinburghairport.model.VisitorAttraction
import io.github.keithlobo07.edinburghairport.theme.BackgroundWhite
import io.github.keithlobo07.edinburghairport.theme.BorderLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavyLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumSurface
import io.github.keithlobo07.edinburghairport.theme.StatusAmber
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.SurfaceMuted
import io.github.keithlobo07.edinburghairport.theme.TextMuted
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import org.jetbrains.compose.resources.painterResource

enum class ExploreFilter(val label: String) {
    ALL("All"),
    DINING("Dining & Bars"),
    SHOPPING("Shopping & Duty Free"),
    LOUNGES("Lounges & Relax"),
    VISIT_EDINBURGH("Visit Edinburgh"),
    TRANSPORT("City Transport")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateToMap: (placeId: String?) -> Unit,
    initialFilter: ExploreFilter = ExploreFilter.ALL,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    var selectedPlace by remember { mutableStateOf<TerminalPlace?>(null) }
    var selectedAttraction by remember { mutableStateOf<VisitorAttraction?>(null) }

    val allPlaces = remember {
        TerminalMapData.places.filter { it.category != PlaceCategory.GATES && it.category != PlaceCategory.SECURITY }
    }
    val attractions = remember { EdinburghData.visitorAttractions }

    val filteredPlaces = remember(searchQuery, selectedFilter) {
        allPlaces.filter { place ->
            val matchesFilter = when (selectedFilter) {
                ExploreFilter.ALL -> true
                ExploreFilter.DINING -> place.category == PlaceCategory.DINING
                ExploreFilter.SHOPPING -> place.category == PlaceCategory.SHOPPING
                ExploreFilter.LOUNGES -> place.category == PlaceCategory.LOUNGES || place.category == PlaceCategory.SERVICES
                ExploreFilter.TRANSPORT -> place.category == PlaceCategory.TRANSPORT
                ExploreFilter.VISIT_EDINBURGH -> false
            }
            val matchesQuery = searchQuery.isBlank() ||
                place.name.contains(searchQuery, ignoreCase = true) ||
                place.subtitle.contains(searchQuery, ignoreCase = true) ||
                place.description.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val filteredAttractions = remember(searchQuery, selectedFilter) {
        if (selectedFilter != ExploreFilter.ALL && selectedFilter != ExploreFilter.VISIT_EDINBURGH) {
            emptyList()
        } else {
            attractions.filter { attraction ->
                searchQuery.isBlank() ||
                    attraction.title.contains(searchQuery, ignoreCase = true) ||
                    attraction.category.contains(searchQuery, ignoreCase = true) ||
                    attraction.shortDescription.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ExploreHeader(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onClearQuery = { searchQuery = "" }
            )

            ExploreFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (searchQuery.isBlank() && (selectedFilter == ExploreFilter.ALL || selectedFilter == ExploreFilter.VISIT_EDINBURGH)) {
                    item {
                        ScottishHeritageBanner(
                            onExploreAttractions = { selectedFilter = ExploreFilter.VISIT_EDINBURGH },
                            onShopScottish = { onNavigateToMap("duty-free-main") }
                        )
                    }
                }

                if (searchQuery.isBlank() && (selectedFilter == ExploreFilter.ALL || selectedFilter == ExploreFilter.TRANSPORT)) {
                    item {
                        TransportConnectionCard(
                            onViewTramMap = { onNavigateToMap("trans-tram") },
                            onViewBusMap = { onNavigateToMap("trans-airlink") }
                        )
                    }
                }

                if (filteredAttractions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Discover Edinburgh & Scotland",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = EdinburghNavy
                            )
                            Text(
                                text = "${filteredAttractions.size} highlights",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    items(filteredAttractions) { attraction ->
                        AttractionExploreCard(
                            attraction = attraction,
                            onClick = { selectedAttraction = attraction }
                        )
                    }
                }

                if (filteredPlaces.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedFilter) {
                                    ExploreFilter.DINING -> "Terminal Dining & Bars"
                                    ExploreFilter.SHOPPING -> "Shops & World Duty Free"
                                    ExploreFilter.LOUNGES -> "Lounges & Wellness"
                                    ExploreFilter.TRANSPORT -> "Ground Transport & Parking"
                                    else -> "Airport Directory & Services"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = EdinburghNavy
                            )
                            Text(
                                text = "${filteredPlaces.size} venues",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    items(filteredPlaces) { place ->
                        TerminalPlaceCard(
                            place = place,
                            onClick = { selectedPlace = place },
                            onNavigateToMap = { onNavigateToMap(place.id) }
                        )
                    }
                }

                if (filteredPlaces.isEmpty() && filteredAttractions.isEmpty()) {
                    item {
                        EmptySearchCard(
                            query = searchQuery,
                            onClear = {
                                searchQuery = ""
                                selectedFilter = ExploreFilter.ALL
                            }
                        )
                    }
                }
            }
        }

        if (selectedPlace != null) {
            PlaceDetailSheet(
                place = selectedPlace!!,
                onDismiss = { selectedPlace = null },
                onNavigateToMap = {
                    val id = selectedPlace?.id
                    selectedPlace = null
                    onNavigateToMap(id)
                }
            )
        }

        if (selectedAttraction != null) {
            AttractionDetailSheet(
                attraction = selectedAttraction!!,
                onDismiss = { selectedAttraction = null }
            )
        }
    }
}

@Composable
private fun ExploreHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text("Explore", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
        Text("Terminal shops, Scottish dining & city highlights", fontSize = 13.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search BrewDog, Duty Free, Castles, Lounges...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = null,
                    tint = EdinburghNavy,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceMuted,
                unfocusedContainerColor = SurfaceMuted,
                focusedBorderColor = EdinburghPlum,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ExploreFilterRow(
    selectedFilter: ExploreFilter,
    onFilterSelected: (ExploreFilter) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExploreFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) EdinburghPlum else SurfaceMuted
            ) {
                Text(
                    text = filter.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextWhite else EdinburghNavy,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ScottishHeritageBanner(
    onExploreAttractions: () -> Unit,
    onShopScottish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EdinburghNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            EdinburghNavy,
                            EdinburghNavyLight,
                            Color(0xFF1E3A8A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = EdinburghPlum) {
                        Text(
                            text = "AUTHENTIC SCOTLAND",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text("EDI Gateway", fontSize = 11.sp, color = Color(0xFF93C5FD))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Single Malt Whiskies & Scottish Cashmere",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Visit World Duty Free for rare Highland & Speyside malts, or Brora and Tartan Weaving Mill for heritage Scottish textiles before your flight.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onShopScottish,
                        colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Find Duty Free", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    OutlinedButton(
                        onClick = onExploreAttractions,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = BorderStroke(1.dp, Color(0xFF64748B)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("City Sights", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportConnectionCard(
    onViewTramMap: () -> Unit,
    onViewBusMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EdinburghPlumSurface),
        border = BorderStroke(1.dp, EdinburghPlumLight.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        ) {
            Text("Fast Connections to City Centre", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Board directly from the airport terminal plaza. No transfers required.", fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundWhite)
                        .clickable(onClick = onViewTramMap)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(Res.drawable.ic_tram), contentDescription = null, tint = EdinburghPlum, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edinburgh Tram", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("30 mins to Princes St", fontSize = 11.sp, color = TextSecondary)
                    Text("Every 7 mins • £7.90", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundWhite)
                        .clickable(onClick = onViewBusMap)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(Res.drawable.ic_bus), contentDescription = null, tint = StatusAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Airlink 100", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("25 mins to Waverley", fontSize = 11.sp, color = TextSecondary)
                    Text("24/7 service • £6.00", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)
                }
            }
        }
    }
}

@Composable
private fun AttractionExploreCard(
    attraction: VisitorAttraction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (attraction.imageResource != null) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    Image(
                        painter = painterResource(attraction.imageResource),
                        contentDescription = attraction.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = attraction.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = EdinburghPlum.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = attraction.durationFromAirport,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = attraction.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = attraction.shortDescription,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun TerminalPlaceCard(
    place: TerminalPlace,
    onClick: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (place.category) {
                            PlaceCategory.DINING -> Color(0xFFFEF3C7)
                            PlaceCategory.SHOPPING -> EdinburghPlumSurface
                            PlaceCategory.LOUNGES -> Color(0xFFE0E7FF)
                            PlaceCategory.TRANSPORT -> Color(0xFFDCFCE7)
                            else -> SurfaceMuted
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        when (place.category) {
                            PlaceCategory.DINING -> Res.drawable.ic_quick_shopping
                            PlaceCategory.SHOPPING -> Res.drawable.ic_quick_shopping
                            PlaceCategory.LOUNGES -> Res.drawable.ic_quick_lounge
                            PlaceCategory.TRANSPORT -> Res.drawable.ic_tram
                            PlaceCategory.SERVICES -> Res.drawable.ic_quick_parking
                            else -> Res.drawable.ic_location_pin
                        }
                    ),
                    contentDescription = null,
                    tint = when (place.category) {
                        PlaceCategory.DINING -> StatusAmber
                        PlaceCategory.SHOPPING -> EdinburghPlum
                        PlaceCategory.LOUNGES -> EdinburghNavy
                        PlaceCategory.TRANSPORT -> StatusGreen
                        else -> EdinburghNavy
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EdinburghNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = place.subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(place.level.label, fontSize = 11.sp, color = TextMuted)
                    Text("•", fontSize = 10.sp, color = TextMuted)
                    Text(place.operatingHours, fontSize = 11.sp, color = StatusGreen)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onNavigateToMap,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceMuted)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_location_pin),
                    contentDescription = "Show on Map",
                    tint = EdinburghNavy,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySearchCard(
    query: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMuted)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No results found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text("No venues or sights matching \"$query\"", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = EdinburghNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset Filters", color = TextWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceDetailSheet(
    place: TerminalPlace,
    onDismiss: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (place.category) {
                        PlaceCategory.DINING -> Color(0xFFFEF3C7)
                        PlaceCategory.SHOPPING -> EdinburghPlumSurface
                        PlaceCategory.LOUNGES -> Color(0xFFE0E7FF)
                        else -> SurfaceMuted
                    }
                ) {
                    Text(
                        text = place.category.label.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (place.category) {
                            PlaceCategory.DINING -> StatusAmber
                            PlaceCategory.SHOPPING -> EdinburghPlum
                            PlaceCategory.LOUNGES -> EdinburghNavy
                            else -> EdinburghNavy
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(place.level.label, fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(place.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)

            if (place.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(place.subtitle, fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            if (place.description.isNotBlank()) {
                Text(place.description, fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceMuted).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("OPENING HOURS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(place.operatingHours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
                Column {
                    Text("DISTANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${place.walkingDistanceMeters}m (${place.walkingTimeMin} min)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(painter = painterResource(Res.drawable.ic_location_pin), contentDescription = null, modifier = Modifier.size(24.dp), tint = TextWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Locate on Terminal Map", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttractionDetailSheet(
    attraction: VisitorAttraction,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)
        ) {
            if (attraction.imageResource != null) {
                Image(
                    painter = painterResource(attraction.imageResource),
                    contentDescription = attraction.title,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(shape = RoundedCornerShape(8.dp), color = EdinburghPlumSurface) {
                Text(
                    text = attraction.category.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EdinburghPlum,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(attraction.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EdinburghNavy)

            Spacer(modifier = Modifier.height(6.dp))

            Text("Transit: ${attraction.durationFromAirport}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StatusGreen)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            Text(attraction.shortDescription, fontSize = 14.sp, color = TextPrimary, lineHeight = 21.sp)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EdinburghNavy),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
