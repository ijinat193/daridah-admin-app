package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.ProductRepository
import com.example.model.DeliveryConfig
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppHelper
import kotlin.math.*

@Composable
fun DeliveryLocationDialog(
    currentDistanceKm: Int,
    deliveryConfig: DeliveryConfig = DeliveryConfig(),
    onDismiss: () -> Unit,
    onLocationSelected: (distanceKm: Int, areaName: String) -> Unit
) {
    val context = LocalContext.current
    var selectedKm by remember { mutableIntStateOf(currentDistanceKm.coerceIn(1, 25)) }
    var selectedAreaName by remember { mutableStateOf("") }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    // Delivery calculation
    val calculatedFee = deliveryConfig.calculateFee(selectedKm)

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchGpsDistance(
                context = context,
                onSuccess = { distanceKm, text ->
                    isLocating = false
                    selectedKm = distanceKm
                    selectedAreaName = "আমার বর্তমান অবস্থান (GPS)"
                    gpsStatusMessage = text
                },
                onError = { err ->
                    isLocating = false
                    gpsStatusMessage = err
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            isLocating = false
            gpsStatusMessage = "লোকেশন পারমিশন পাওয়া যায়নি। তালিকা থেকে এলাকা নির্বাচন করুন।"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("delivery_location_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ডেলিভারি লোকেশন ও চার্জ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "দাড়িদহ বাজার কেন্দ্র (38VM+F8)",
                                fontSize = 12.sp,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Daridaha Store Center Badge Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "🏪 শপ লোকেশন কোড:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = ProductRepository.STORE_PLUS_CODE + " Daridaha",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GreenPrimary
                                        )
                                        Text(
                                            text = "দাড়িদহ, শিবগঞ্জ, বগুড়া",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { WhatsAppHelper.openDaridahaMap(context) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("গুগল ম্যাপ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = GreenPrimary.copy(alpha = 0.15f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("বেস জোন (০ - ১ কি.মি.)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("মাত্র ৩০ টাকা", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("অতিরিক্ত দূরত্ব (১ কি.মি.+)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("+২৫ টাকা / কি.মি.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // GPS Auto-detect button
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📍 জিপিএস দিয়ে দূরত্ব মাপুন",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "আপনার বর্তমান অবস্থান ও দাড়িদহ বাজারের দূরত্ব অটো বের করুন",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            isLocating = true
                                            gpsStatusMessage = "অবস্থান শনাক্ত করা হচ্ছে..."
                                            val finePerm = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            ) == PackageManager.PERMISSION_GRANTED
                                            val coarsePerm = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (finePerm || coarsePerm) {
                                                fetchGpsDistance(
                                                    context = context,
                                                    onSuccess = { distanceKm, text ->
                                                        isLocating = false
                                                        selectedKm = distanceKm
                                                        selectedAreaName = "আমার বর্তমান অবস্থান (GPS)"
                                                        gpsStatusMessage = text
                                                    },
                                                    onError = { err ->
                                                        isLocating = false
                                                        gpsStatusMessage = err
                                                    }
                                                )
                                            } else {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("শনাক্ত করুন", fontSize = 12.sp)
                                    }
                                }

                                if (gpsStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = gpsStatusMessage!!,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (gpsStatusMessage!!.contains("সফল") || gpsStatusMessage!!.contains("দূরত্ব")) GreenPrimary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Section: Area quick pick
                    item {
                        Text(
                            text = "দাড়িদহ ও পার্শ্ববর্তী এলাকা নির্বাচন করুন:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(ProductRepository.deliveryAreas) { area ->
                        val isSelected = (selectedAreaName == area.name) || (selectedAreaName.isEmpty() && selectedKm == area.distanceKm)
                        val areaFee = deliveryConfig.calculateFee(area.distanceKm)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedKm = area.distanceKm
                                    selectedAreaName = area.name
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = area.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${area.description} • ${area.distanceKm} কি.মি.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "৳$areaFee",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Custom distance slider
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ম্যানুয়াল দূরত্ব নির্ধারণ:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$selectedKm কি.মি.",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenPrimary
                                    )
                                }

                                Slider(
                                    value = selectedKm.toFloat(),
                                    onValueChange = {
                                        selectedKm = it.roundToInt().coerceIn(1, 25)
                                        selectedAreaName = "কাস্টম দূরত্ব ($selectedKm কি.মি.)"
                                    },
                                    valueRange = 1f..25f,
                                    steps = 23,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GreenPrimary,
                                        activeTrackColor = GreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom summary & Confirmation
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "নির্ধারিত দূরত্ব: $selectedKm কি.মি.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ডেলিভারি ফি: ৳$calculatedFee",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )
                        }

                        Button(
                            onClick = {
                                val area = if (selectedAreaName.isNotBlank()) selectedAreaName else "$selectedKm কি.মি. এলাকা"
                                onLocationSelected(selectedKm, area)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("confirm_location_btn")
                        ) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun fetchGpsDistance(
    context: Context,
    onSuccess: (distanceKm: Int, statusText: String) -> Unit,
    onError: (errorText: String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("লোকেশন সার্ভিস পাওয়া যায়নি।")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetEnabled) {
            onError("মোবাইলের লোকেশন (GPS) চালু করুন।")
            return
        }

        var bestLocation: Location? = null

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isGpsEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (bestLocation == null && isNetEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        }

        if (bestLocation != null) {
            val storeLat = ProductRepository.STORE_LATITUDE
            val storeLon = ProductRepository.STORE_LONGITUDE

            val results = FloatArray(1)
            Location.distanceBetween(
                bestLocation.latitude,
                bestLocation.longitude,
                storeLat,
                storeLon,
                results
            )

            val distanceMeters = results[0]
            val distanceKm = max(1, (distanceMeters / 1000f).roundToInt())

            onSuccess(
                distanceKm,
                "✅ অবস্থান সফলভাবে শনাক্ত! দাড়িদহ বাজার থেকে আনুমানিক দূরত্ব: $distanceKm কি.মি."
            )
        } else {
            // When emulator or fresh boot doesn't have last known GPS position
            onError("বর্তমান জিপিএস লোকেশন পাওয়া যায়নি। নিচের তালিকা থেকে এলাকা বেছে নিন।")
        }
    } catch (e: Exception) {
        onError("লোকেশন গণনায় ত্রুটি: ${e.localizedMessage}")
    }
}
