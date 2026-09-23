package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductRepository
import com.example.model.DeliveryConfig
import com.example.model.Product
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSheet(
    sheetState: SheetState,
    products: List<Product>,
    deliveryConfig: DeliveryConfig = DeliveryConfig(),
    cloudSyncUrl: String = "",
    lastSyncTime: Long = 0L,
    isSyncing: Boolean = false,
    onDismiss: () -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onAddProduct: (Product) -> Unit,
    onUpdateDeliveryConfig: (DeliveryConfig) -> Unit = {},
    onSaveCloudUrl: (String) -> Unit = {},
    onSyncNow: () -> Unit = {},
    onResetDefaults: () -> Unit = {},
    exportJsonString: () -> String = { "" },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("admin_sheet")
    ) {
        if (!isAuthenticated) {
            // PIN Entry for Store Owner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GreenPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "দোকানদার অ্যাডমিন প্যানেল",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Text(
                    text = "শুধুমাত্র দোকানদারের জন্য সংরক্ষিত",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        pinInput = it
                        pinError = false
                    },
                    label = { Text("অ্যাডমিন পিন কোড দিন (ডিফল্ট: 1234)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = pinError,
                    supportingText = {
                        if (pinError) {
                            Text("ভুল পিন কোড! সঠিক পিন: 1234", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("দোকানদার পাসওয়ার্ড: 1234 অথবা মোবাইল নম্বর")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pinInput.trim() == "1234" || pinInput.trim() == ProductRepository.HELPLINE_NUMBER) {
                            isAuthenticated = true
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগইন করুন", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Simplified and Focused Admin Panel: Product Name, Price, and Photo Edit
            AdminProductManagerView(
                products = products,
                onUpdateProduct = onUpdateProduct,
                onAddProduct = onAddProduct,
                onClose = onDismiss
            )
        }
    }
}

@Composable
fun AdminProductManagerView(
    products: List<Product>,
    onUpdateProduct: (Product) -> Unit,
    onAddProduct: (Product) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    val filtered = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚙️ দোকানদার অ্যাডমিন",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Text(
                    text = "পণ্যের নাম, মূল্য ও ছবি (Photo) পরিবর্তন করুন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("পণ্য খুঁজুন (যেমন: চাল, আলু, তেল)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { isAddingNew = true },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("নতুন", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "মোট ${filtered.size} টি পণ্য তালিকায় আছে (যেকোনোটিতে চাপ দিয়ে নাম, দাম বা ছবি এডিট করুন)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Products List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            items(filtered, key = { it.id }) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { editingProduct = product },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Photo / Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (product.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = product.imageUrl,
                                            contentDescription = product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        Text(text = product.icon, fontSize = 24.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${product.category} • প্রতি ${product.unit}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Price and Edit Action
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GreenPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "৳${product.price}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = GreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = { editingProduct = product }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = GreenPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Edit Product Dialog (Name, Price, and Photo Edit)
    editingProduct?.let { prod ->
        ProductEditDialog(
            title = "পণ্য এডিট করুন",
            initialName = prod.name,
            initialPrice = prod.price.toString(),
            initialUnit = prod.unit,
            initialIcon = prod.icon,
            initialImageUrl = prod.imageUrl,
            onSave = { updatedName, updatedPrice, updatedUnit, updatedIcon, updatedImageUrl ->
                val newPrice = updatedPrice.toIntOrNull() ?: prod.price
                onUpdateProduct(
                    prod.copy(
                        name = updatedName.trim(),
                        price = newPrice,
                        unit = updatedUnit.trim(),
                        icon = updatedIcon,
                        imageUrl = updatedImageUrl.trim()
                    )
                )
                Toast.makeText(context, "${updatedName} সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                editingProduct = null
            },
            onDismiss = { editingProduct = null }
        )
    }

    // Add New Product Dialog
    if (isAddingNew) {
        ProductEditDialog(
            title = "নতুন পণ্য যোগ করুন",
            initialName = "",
            initialPrice = "",
            initialUnit = "কেজি",
            initialIcon = "🥬",
            initialImageUrl = "",
            onSave = { newName, newPriceStr, newUnit, newIcon, newImageUrl ->
                val priceVal = newPriceStr.toIntOrNull() ?: 50
                val newId = (products.maxOfOrNull { it.id } ?: 100) + 1
                onAddProduct(
                    Product(
                        id = newId,
                        name = newName.trim(),
                        price = priceVal,
                        unit = if (newUnit.isNotBlank()) newUnit.trim() else "কেজি",
                        category = "সাধারণ",
                        icon = newIcon,
                        imageUrl = newImageUrl.trim(),
                        inStock = true
                    )
                )
                Toast.makeText(context, "${newName} যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                isAddingNew = false
            },
            onDismiss = { isAddingNew = false }
        )
    }
}

@Composable
fun ProductEditDialog(
    title: String,
    initialName: String,
    initialPrice: String,
    initialUnit: String,
    initialIcon: String,
    initialImageUrl: String,
    onSave: (name: String, price: String, unit: String, icon: String, imageUrl: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var unit by remember { mutableStateOf(initialUnit) }
    var icon by remember { mutableStateOf(initialIcon) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var showUrlInput by remember { mutableStateOf(imageUrl.isNotBlank()) }

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    // Common grocery icons for fast selection
    val commonIcons = listOf(
        "🍚", "🥔", "🧅", "🥦", "🥕", "🍅", "🌶️", "🥬",
        "🐟", "🥩", "🍗", "🥚", "🥛", "🍌", "🍎", "🥭",
        "🫗", "🧂", "🍬", "🍫", "🍪", "🧼", "🛍️", "📦"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Photo Preview & Selection Section
                item {
                    Text(
                        text = "১. পণ্যের ছবি বা ফটো (Photo)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GreenPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Live Preview Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F8F3),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Selected Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(72.dp)
                                    )
                                } else {
                                    Text(text = icon, fontSize = 36.sp)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            // Gallery Photo Picker Button
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("গ্যালারি থেকে ছবি নিন", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { showUrlInput = !showUrlInput },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (showUrlInput) "লিংক বন্ধ" else "ছবির লিংক", fontSize = 11.sp)
                                }

                                if (imageUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { imageUrl = "" },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear Photo", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    // Optional Image URL input
                    if (showUrlInput) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("ছবির ওয়েব লিংক (URL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Fast Emoji Icon Picker
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "অথবা প্রতীক/আইকন নির্বাচন করুন:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonIcons.forEach { emoji ->
                            val isSelected = icon == emoji && imageUrl.isBlank()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GreenPrimary.copy(alpha = 0.2f) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GreenPrimary else Color.LightGray.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable {
                                        icon = emoji
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                // 2. Product Name Section
                item {
                    Text(
                        text = "২. পণ্যের নাম (Product Name) *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("পণ্যের নাম লিখুন (যেমন: মিনিকেট চাল)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 3. Product Price Section
                item {
                    Text(
                        text = "৩. মূল্য / দাম (Price in Taka) *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("মূল্য (টাকা)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("একক (কেজি/লিটার)") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, price, unit, icon, imageUrl)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("বাতিল")
            }
        }
    )
}
