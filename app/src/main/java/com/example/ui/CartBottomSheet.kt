package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductRepository
import com.example.model.CartItem
import com.example.model.CustomerInfo
import com.example.model.DeliveryConfig
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    sheetState: SheetState,
    items: List<CartItem>,
    customerInfo: CustomerInfo,
    deliveryConfig: DeliveryConfig = DeliveryConfig(),
    onDismiss: () -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearCart: () -> Unit,
    onUpdateCustomerInfo: (String, String, String, String) -> Unit,
    onDistanceChange: (Int) -> Unit = {},
    onSubmitOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember(customerInfo) { mutableStateOf(customerInfo.name) }
    var phone by remember(customerInfo) { mutableStateOf(customerInfo.phone) }
    var address by remember(customerInfo) { mutableStateOf(customerInfo.address) }
    var note by remember(customerInfo) { mutableStateOf(customerInfo.note) }
    var distanceKm by remember(customerInfo) { mutableIntStateOf(customerInfo.distanceKm.coerceAtLeast(1)) }
    var showLocationDialog by remember { mutableStateOf(false) }

    val subtotal = items.sumOf { it.subtotal }
    val deliveryFee = deliveryConfig.calculateFee(distanceKm)
    val grandTotal = subtotal + deliveryFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("cart_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আপনার কার্ট",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                        if (items.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GreenPrimary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${items.sumOf { it.quantity }} টি",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (items.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearCart,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_cart_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "খালি করুন", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Empty State
            if (items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛍️", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "আপনার কার্ট বর্তমানে খালি",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "পছন্দের পণ্য যোগ করে অর্ডার করুন",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("বাজার শুরু করুন")
                        }
                    }
                }
            } else {
                // Cart Items List
                items(items, key = { it.product.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.product.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = item.product.imageUrl,
                                            contentDescription = item.product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    } else {
                                        Text(text = item.product.icon, fontSize = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = item.product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "৳${item.product.price} × ${item.quantity} ${item.product.unit}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "মোট: ৳${item.subtotal}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onDecrement(item.product.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = { onIncrement(item.product.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onRemove(item.product.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Delivery Distance & Charge Calculation
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_charge_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ডেলিভারি দূরত্ব ও চার্জ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    color = WhatsAppGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "চার্জ: ৳$deliveryFee",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = GreenPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "৩ কি.মি. পর্যন্ত বেস চার্জ ৩০ টাকা, এরপর প্রতি অতিরিক্ত ১ কি.মি.-তে ১০ টাকা",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            // Interactive Distance Stepper
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F8F3), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "আপনার দূরত্ব:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = deliveryConfig.getFeeBreakdown(distanceKm),
                                        fontSize = 11.sp,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (distanceKm > 1) {
                                                distanceKm--
                                                onDistanceChange(distanceKm)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("distance_decrease_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease distance",
                                            tint = GreenPrimary
                                        )
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        shadowElevation = 1.dp
                                    ) {
                                        Text(
                                            text = "$distanceKm কি.মি.",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            distanceKm++
                                            onDistanceChange(distanceKm)
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("distance_increase_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase distance",
                                            tint = GreenPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Distance Quick Selection Chips
                            Text(
                                text = "দূরত্ব চয়ন করুন:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1 to "১ কি.মি.",
                                    2 to "২ কি.মি.",
                                    3 to "৩ কি.মি. (বাজার)",
                                    4 to "৪ কি.মি.",
                                    5 to "৫ কি.মি.",
                                    6 to "৬ কি.মি.",
                                    8 to "৮ কি.মি.",
                                    10 to "১০ কি.মি."
                                ).forEach { (km, label) ->
                                    val isSelected = distanceKm == km
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            distanceKm = km
                                            onDistanceChange(km)
                                        },
                                        label = {
                                            Text(
                                                text = "$label (৳${deliveryConfig.calculateFee(km)})",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = WhatsAppGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = GreenPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Map & Village Selector Button
                            OutlinedButton(
                                onClick = { showLocationDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_map_selector_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                                border = BorderStroke(1.dp, GreenPrimary)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🗺️ ম্যাপ ও এলাকা দেখে দূরত্ব নির্ধারণ (38VM+F8)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtotal & Grand Total Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "পণ্যের মোট মূল্য:", fontSize = 14.sp)
                                Text(
                                    text = "৳$subtotal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "ডেলিভারি চার্জ ($distanceKm কি.মি.):", fontSize = 14.sp)
                                Text(
                                    text = "৳$deliveryFee",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GreenPrimary
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = GreenPrimary.copy(alpha = 0.2f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বমোট প্রদেয়:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "৳$grandTotal",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Delivery details form
                item {
                    Text(
                        text = "📦 আপনার ডেলিভারি তথ্য",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "তথ্যগুলো সংরক্ষিত থাকবে, পরবর্তী অর্ডারে বারবার টাইপ করতে হবে না।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            onUpdateCustomerInfo(name, phone, address, note)
                        },
                        label = { Text("আপনার পূর্ণ নাম *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            onUpdateCustomerInfo(name, phone, address, note)
                        },
                        label = { Text("মোবাইল নম্বর (যেমন: 017XXXXXXXX) *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_phone"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            onUpdateCustomerInfo(name, phone, address, note)
                        },
                        label = { Text("ডেলিভারি ঠিকানা (গ্রাম/মহল্লা, বাড়ি/রোড নং) *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_address"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                            onUpdateCustomerInfo(name, phone, address, note)
                        },
                        label = { Text("বিশেষ নোট / অনুরোধ (ঐচ্ছিক)") },
                        leadingIcon = { Icon(Icons.Default.Note, contentDescription = null, tint = GreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_note"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // WhatsApp Order Button
                    Button(
                        onClick = {
                            onUpdateCustomerInfo(name, phone, address, note)
                            onSubmitOrder()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_whatsapp_order_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp-এ সরাসরি অর্ডার করুন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct Call Button
                    OutlinedButton(
                        onClick = { WhatsAppHelper.dialHelpline(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("call_store_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সরাসরি দোকানে কল করুন (${ProductRepository.HELPLINE_NUMBER})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showLocationDialog) {
        DeliveryLocationDialog(
            currentDistanceKm = distanceKm,
            deliveryConfig = deliveryConfig,
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { newKm, areaName ->
                distanceKm = newKm
                onDistanceChange(newKm)
                if (address.isBlank() || address.startsWith("এলাকা:") || address.contains("অবস্থান") || address.contains("কি.মি.")) {
                    address = areaName
                    onUpdateCustomerInfo(name, phone, areaName, note)
                }
            }
        )
    }
}
