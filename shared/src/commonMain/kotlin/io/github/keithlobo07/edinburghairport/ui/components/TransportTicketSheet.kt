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
import edinburghairport.shared.generated.resources.ic_bus
import edinburghairport.shared.generated.resources.ic_check_circle
import edinburghairport.shared.generated.resources.ic_close
import edinburghairport.shared.generated.resources.ic_google_logo
import edinburghairport.shared.generated.resources.ic_location_pin
import edinburghairport.shared.generated.resources.ic_tram
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

private enum class TicketStep {
    SELECT,
    PURCHASED
}

enum class TransportType {
    TRAM,
    AIRLINK_BUS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportTicketSheet(
    transportType: TransportType,
    onDismiss: () -> Unit,
    onNavigateToMap: (placeId: String?) -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val storedReference: String? = null
    var step by remember { mutableStateOf(TicketStep.SELECT) }
    var isReturnTicket by remember { mutableStateOf(false) }
    var passengerCount by remember { mutableIntStateOf(1) }
    var isAddedToWallet by remember { mutableStateOf(false) }

    val isTram = transportType == TransportType.TRAM
    val serviceTitle = if (isTram) "Edinburgh Trams" else "Airlink 100 Express Bus"
    val singlePrice = if (isTram) 7.90 else 6.00
    val returnPrice = if (isTram) 11.80 else 9.50
    val unitPrice = if (isReturnTicket) returnPrice else singlePrice
    val totalPrice = unitPrice * passengerCount

    val ticketRef = remember(transportType) {
        val prefix = if (isTram) "ET" else "AL"
        val rand = (100000..999999).random()
        "$prefix-EDI-$rand"
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
                            .background(if (isTram) EdinburghNavy else EdinburghPlum),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(if (isTram) Res.drawable.ic_tram else Res.drawable.ic_bus),
                            contentDescription = serviceTitle,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = serviceTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = EdinburghNavy
                        )
                        Text(
                            text = if (isTram) "Airport ⇄ City Centre (St Andrew Sq)" else "Airport ⇄ Waverley Bridge Express",
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

            if (step == TicketStep.SELECT) {
                // Route info card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTram) "Frequency: Every 7 mins" else "Frequency: Every 10 mins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = EdinburghPlum
                            )
                            Text(
                                text = if (isTram) "Journey: ~30 mins" else "Journey: ~25 mins",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTram)
                                "Departs from Edinburgh Airport Tram Stop (directly outside Main Terminal forecourt). Arrives at St Andrew Square, Princes Street & Newhaven."
                            else
                                "Departs from Stance A outside UK Arrivals. Express service direct to Edinburgh Waverley railway station with free 4G Wi-Fi & USB charging.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Ticket Type Toggle (Single vs Return)
                Text(
                    text = "Select Ticket Option",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EdinburghNavy,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = { isReturnTicket = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (!isReturnTicket) EdinburghNavy else SurfaceLight,
                        border = BorderStroke(1.dp, if (!isReturnTicket) EdinburghNavy else BorderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Airport Single",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (!isReturnTicket) TextWhite else EdinburghNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "£${formatGbp(singlePrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (!isReturnTicket) Color(0xFF93C5FD) else EdinburghPlum
                            )
                            Text(
                                text = "One-way travel",
                                fontSize = 10.sp,
                                color = if (!isReturnTicket) Color(0xFFCBD5E1) else TextMuted
                            )
                        }
                    }

                    Surface(
                        onClick = { isReturnTicket = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isReturnTicket) EdinburghNavy else SurfaceLight,
                        border = BorderStroke(1.dp, if (isReturnTicket) EdinburghNavy else BorderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Open Return",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isReturnTicket) TextWhite else EdinburghNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "£${formatGbp(returnPrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isReturnTicket) Color(0xFF93C5FD) else EdinburghPlum
                            )
                            Text(
                                text = "Valid for 90 days",
                                fontSize = 10.sp,
                                color = if (isReturnTicket) Color(0xFFCBD5E1) else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Passenger count stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceLight)
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Passengers", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EdinburghNavy)
                        Text("Adult standard fare", fontSize = 11.sp, color = TextSecondary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = { if (passengerCount > 1) passengerCount-- },
                            shape = CircleShape,
                            color = if (passengerCount > 1) EdinburghPlumSurface else SurfaceMuted,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EdinburghPlum)
                            }
                        }

                        Text(
                            text = "$passengerCount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EdinburghNavy
                        )

                        Surface(
                            onClick = { if (passengerCount < 8) passengerCount++ },
                            shape = CircleShape,
                            color = EdinburghPlumSurface,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EdinburghPlum)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Total and Buy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Price", fontSize = 13.sp, color = TextSecondary)
                    Text("£${formatGbp(totalPrice)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = EdinburghNavy)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        PurchasedPassStorage.saveTransportReference(
                            if (isTram) "TRAM" else "BUS",
                            ticketRef
                        )
                        PurchasedPassStorage.saveDigitalPass(
                            AirportDigitalPass(
                                id = "TRANSIT_$ticketRef",
                                type = PassType.TRANSIT,
                                title = if (isTram) "Edinburgh Trams Pass" else "Airlink 100 Express Bus",
                                subtitle = if (isTram) "Edinburgh Airport ⇄ Newhaven" else "Edinburgh Airport ⇄ Waverley Bridge",
                                reference = ticketRef,
                                barcodeValue = ticketRef,
                                primaryFieldLabel = "TICKET TYPE",
                                primaryFieldValue = if (isReturnTicket) "Airport Open Return (90 Days)" else "Airport Single",
                                secondaryFieldLabel = "PASSENGERS",
                                secondaryFieldValue = "$passengerCount Adult${if (passengerCount > 1) "s" else ""}",
                                auxiliaryFieldLabel = "VALIDATOR SCAN",
                                auxiliaryFieldValue = "Optical Reader Ready",
                                priceFormatted = "£${formatGbp(totalPrice)}",
                                validUntil = if (isReturnTicket) "Valid 90 Days" else "Valid Today",
                                status = "ACTIVE & READY TO BOARD"
                            )
                        )
                        step = TicketStep.PURCHASED
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Buy Ticket • £${formatGbp(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                }
            } else {
                // PURCHASED STEP: Confirmed Ticket + Add to Google Wallet
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
                    text = "You Bought the Ticket!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = EdinburghNavy
                )
                Text(
                    text = "Ready to board • Scannable on tram platforms & bus readers",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Digital Transit Pass Card
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
                                text = serviceTitle.uppercase(),
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
                                    text = "ACTIVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isReturnTicket) "Open Return ($passengerCount Passenger${if (passengerCount > 1) "s" else ""})"
                                   else "Airport Single ($passengerCount Passenger${if (passengerCount > 1) "s" else ""})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                        Text(
                            text = "Edinburgh Airport ⇄ City Centre Zone",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR / Barcode Display
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
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
                                        text = ticketRef,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EdinburghNavy
                                    )
                                    Text(
                                        text = "Scan at validator",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                // Visual barcode bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val barHeights = listOf(36, 42, 28, 48, 30, 44, 48, 26, 40, 48, 32, 44, 38, 48, 28, 46, 34)
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
                            text = "Valid for travel today • £${formatGbp(totalPrice)} paid",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "ADD TO GOOGLE WALLET" Official Style Button
                Surface(
                    onClick = {
                        GoogleWalletService.saveTransitPassToWallet(
                            ticketRef = ticketRef,
                            serviceName = serviceTitle,
                            route = if (isTram) "Edinburgh Airport ⇄ City Centre (St Andrew Sq)" else "Edinburgh Airport ⇄ Waverley Bridge Express",
                            priceFormatted = "£${formatGbp(totalPrice)}",
                            passengerCount = passengerCount,
                            isReturn = isReturnTicket
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

                // Secondary Navigation to Platform
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        val stopPlaceId = if (isTram) "tram-station" else "bus-stance-a"
                        onNavigateToMap(stopPlaceId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location_pin),
                        contentDescription = "Stop location",
                        tint = EdinburghPlum,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTram) "View Tram Station on Map" else "View Bus Stance A on Map",
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
