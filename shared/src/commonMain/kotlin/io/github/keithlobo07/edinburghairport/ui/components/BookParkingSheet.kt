package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edinburghairport.shared.generated.resources.Res
import edinburghairport.shared.generated.resources.ic_check_circle
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_google_logo
import edinburghairport.shared.generated.resources.ic_location_pin
import edinburghairport.shared.generated.resources.ic_quick_parking
import io.github.keithlobo07.edinburghairport.data.GoogleWalletService
import io.github.keithlobo07.edinburghairport.data.PurchasedPassStorage
import io.github.keithlobo07.edinburghairport.model.AirportDigitalPass
import io.github.keithlobo07.edinburghairport.model.PassType
import io.github.keithlobo07.edinburghairport.theme.BorderLight
import io.github.keithlobo07.edinburghairport.theme.EdinburghNavy
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlumSurface
import io.github.keithlobo07.edinburghairport.theme.StatusGreen
import io.github.keithlobo07.edinburghairport.theme.SurfaceLight
import io.github.keithlobo07.edinburghairport.theme.SurfaceMuted
import io.github.keithlobo07.edinburghairport.theme.TextMuted
import io.github.keithlobo07.edinburghairport.theme.TextPrimary
import io.github.keithlobo07.edinburghairport.theme.TextSecondary
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import org.jetbrains.compose.resources.painterResource

private enum class ParkingStep {
    SELECT_SLOT,
    CONFIRMED
}

data class ParkingProduct(
    val id: String,
    val name: String,
    val subtitle: String,
    val pricePerDay: Double,
    val walkTime: String,
    val mapPlaceId: String,
    val features: List<String>
)

val officialParkingProducts = listOf(
    ParkingProduct(
        id = "multi-storey",
        name = "Terminal Multi-Storey",
        subtitle = "Closest covered parking directly next to check-in",
        pricePerDay = 42.00,
        walkTime = "2 min walk",
        mapPlaceId = "parking-terminal",
        features = listOf("Covered sky-bridge", "EV fast charging", "ANPR barrier lift")
    ),
    ParkingProduct(
        id = "terminal-surface",
        name = "Terminal Surface Parking",
        subtitle = "Convenient outdoor bays next to the terminal building",
        pricePerDay = 32.00,
        walkTime = "3 min walk",
        mapPlaceId = "parking-terminal",
        features = listOf("Self-park & keep keys", "24/7 CCTV security", "Wide spaces")
    ),
    ParkingProduct(
        id = "plane-parking",
        name = "Plane Parking (Budget)",
        subtitle = "Our best value official parking with frequent shuttle buses",
        pricePerDay = 18.00,
        walkTime = "5 min shuttle",
        mapPlaceId = "parking-longstay",
        features = listOf("Free continuous shuttle", "Budget guaranteed", "ParkMark accredited")
    ),
    ParkingProduct(
        id = "valet-fastpark",
        name = "FastPark Valet",
        subtitle = "Drop your vehicle at terminal forecourt; we park for you",
        pricePerDay = 55.00,
        walkTime = "1 min walk",
        mapPlaceId = "parking-terminal",
        features = listOf("Drop keys & fly", "Heated return bay", "Car inspected on entry")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookParkingSheet(
    onDismiss: () -> Unit,
    onParkingBooked: (carParkName: String, spaceRef: String) -> Unit = { _, _ -> },
    onNavigateToMap: (placeId: String?) -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var step by remember { mutableStateOf(ParkingStep.SELECT_SLOT) }

    var selectedProduct by remember { mutableStateOf(officialParkingProducts[0]) }
    var selectedDays by remember { mutableIntStateOf(2) }
    var vehicleReg by remember { mutableStateOf("SD72 XKL") }
    var isAddedToWallet by remember { mutableStateOf(false) }

    val totalPrice = selectedProduct.pricePerDay * selectedDays

    val bookingRef = remember {
        val rand = (10000..99999).random()
        "EDI-PRK-$rand"
    }

    val assignedSpot = remember(selectedProduct) {
        when (selectedProduct.id) {
            "multi-storey" -> "Level 2, Zone B, Bay 44"
            "terminal-surface" -> "Zone C, Bay 18"
            "plane-parking" -> "Shuttle Bay S-08"
            else -> "Valet Forecourt Bay V-03"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EdinburghNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_quick_parking),
                            contentDescription = "Parking",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (step == ParkingStep.SELECT_SLOT) "Book Official Parking" else "Parking Confirmed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = EdinburghNavy
                        )
                        Text(
                            text = "Guaranteed space • Official Edinburgh Airport",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (step == ParkingStep.SELECT_SLOT) {
                // Car Park Option Picker
                Text(
                    text = "Choose Car Park",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EdinburghNavy,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    officialParkingProducts.forEach { product ->
                        val isSelected = product.id == selectedProduct.id
                        Surface(
                            onClick = { selectedProduct = product },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EdinburghNavy else SurfaceLight,
                            border = BorderStroke(1.dp, if (isSelected) EdinburghNavy else BorderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) TextWhite else EdinburghNavy
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSelected) EdinburghPlum else Color(0xFFE2E8F0)
                                        ) {
                                            Text(
                                                text = product.walkTime,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) TextWhite else EdinburghNavy,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = product.subtitle,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFFCBD5E1) else TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "£${formatGbp(product.pricePerDay)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isSelected) Color(0xFF93C5FD) else EdinburghPlum
                                    )
                                    Text(
                                        text = "/ day",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFF94A3B8) else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration Selector
                Text(
                    text = "Stay Duration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EdinburghNavy,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val daysOptions = listOf(1, 2, 3, 5, 7)
                    daysOptions.forEach { d ->
                        val isSelected = d == selectedDays
                        Surface(
                            onClick = { selectedDays = d },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EdinburghPlum else SurfaceLight,
                            border = BorderStroke(1.dp, if (isSelected) EdinburghPlum else BorderLight)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (d == 1) "1 Day" else "${d} Days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextWhite else EdinburghNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle Registration Plate Input
                Text(
                    text = "Vehicle Registration (Number Plate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EdinburghNavy,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Barriers open automatically via ANPR on arrival",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = vehicleReg,
                    onValueChange = { vehicleReg = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EdinburghPlum,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp,
                        color = EdinburghNavy
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Total Summary & Buy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payable", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${selectedDays} days • ${selectedProduct.name}",
                            fontSize = 11.sp,
                            color = EdinburghPlum,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "£${formatGbp(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = EdinburghNavy
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        step = ParkingStep.CONFIRMED
                        PurchasedPassStorage.saveParkingReference(
                            "$bookingRef|${selectedProduct.name}|$assignedSpot"
                        )
                        PurchasedPassStorage.saveDigitalPass(
                            AirportDigitalPass(
                                id = "PARK_$bookingRef",
                                type = PassType.PARKING,
                                title = selectedProduct.name,
                                subtitle = "Bay $assignedSpot • Barrier Access Active",
                                reference = bookingRef,
                                barcodeValue = bookingRef,
                                primaryFieldLabel = "VEHICLE REG",
                                primaryFieldValue = vehicleReg,
                                secondaryFieldLabel = "DURATION",
                                secondaryFieldValue = "$selectedDays Days Stay",
                                auxiliaryFieldLabel = "ASSIGNED BAY",
                                auxiliaryFieldValue = assignedSpot,
                                priceFormatted = "£${formatGbp(totalPrice)}",
                                validUntil = "Valid for $selectedDays days",
                                status = "ACTIVE • ANPR VERIFIED"
                            )
                        )
                        onParkingBooked(selectedProduct.name, "${selectedProduct.name} - $assignedSpot")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Book Parking Slot • £${formatGbp(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                }
            } else {
                // CONFIRMED STEP: Confirmed Parking Pass + Add to Google Wallet
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check_circle),
                        contentDescription = "Success",
                        tint = StatusGreen,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Parking Slot Booked!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = EdinburghNavy
                )
                Text(
                    text = "Your space is reserved • Barrier will open automatically for $vehicleReg",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Parking Pass Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EdinburghNavy),
                    border = BorderStroke(1.dp, Color(0xFF1E3A8A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedProduct.name.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF93C5FD),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusGreen
                            ) {
                                Text(
                                    text = "RESERVED",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = assignedSpot,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextWhite
                        )
                        Text(
                            text = "Vehicle: $vehicleReg • $selectedDays Days Stay",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // QR / Barcode Display
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = bookingRef,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EdinburghNavy
                                    )
                                    Text(
                                        text = "ANPR / Scanner Backup",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(32, 40, 24, 44, 28, 42, 44, 26, 38, 44, 30, 40, 36, 44, 26, 42, 32)
                                    barHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(h.dp)
                                                .background(EdinburghNavy)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Paid £${formatGbp(totalPrice)} • Barrier opens on plate detection",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "ADD TO GOOGLE WALLET" Official Style Button
                Surface(
                    onClick = {
                        GoogleWalletService.saveParkingPassToWallet(
                            bookingRef = bookingRef,
                            carParkName = selectedProduct.name,
                            assignedSpot = assignedSpot,
                            vehicleReg = vehicleReg,
                            days = selectedDays,
                            totalPriceFormatted = "£${formatGbp(totalPrice)}"
                        )
                        isAddedToWallet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    color = if (isAddedToWallet) Color(0xFF1E293B) else Color(0xFF000000),
                    border = BorderStroke(1.dp, if (isAddedToWallet) StatusGreen else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAddedToWallet) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check_circle),
                                contentDescription = "Added",
                                tint = StatusGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Added to Google Wallet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextWhite
                            )
                        } else {
                            // Official 4-color Google "G" logo
                            Icon(
                                painter = painterResource(Res.drawable.ic_google_logo),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Add to Google Wallet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigate to Car Park Entrance
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onNavigateToMap(selectedProduct.mapPlaceId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location_pin),
                        contentDescription = "Car Park location",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Navigate to Car Park Entrance",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = EdinburghNavy
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceMuted),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

internal fun formatGbp(amount: Double): String {
    val pence = kotlin.math.round(amount * 100.0).toLong()
    val whole = pence / 100L
    val frac = kotlin.math.abs(pence % 100L).toString().padStart(2, '0')
    return "$whole.$frac"
}
