package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.ProductRepository
import com.example.ui.theme.BadgeOrange
import com.example.ui.theme.GreenPrimary
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketHomeScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()

    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val adminSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cartCount = viewModel.getCartCount()
    val cartTotal = viewModel.getCartTotal()

    var titleTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTime > 1500) {
                                    titleTapCount = 1
                                } else {
                                    titleTapCount++
                                }
                                lastTapTime = now
                                if (titleTapCount >= 5) {
                                    titleTapCount = 0
                                    Toast.makeText(context, "দোকানদার ভেরিফিকেশন প্যানেল চালু হচ্ছে...", Toast.LENGTH_SHORT).show()
                                    viewModel.openAdmin()
                                }
                            }
                    ) {
                        Text(text = "🛒", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ProductRepository.SHOP_NAME,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.openOrderHistory() },
                        modifier = Modifier.testTag("nav_order_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Helpline call button
                    IconButton(
                        onClick = { WhatsAppHelper.dialHelpline(context) },
                        modifier = Modifier.testTag("top_bar_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Helpline",
                            tint = Color.White
                        )
                    }

                    // Share App Button (Share with friends & get reviews)
                    IconButton(
                        onClick = { WhatsAppHelper.shareApp(context) },
                        modifier = Modifier.testTag("top_bar_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = Color.White
                        )
                    }

                    // Cart icon with count badge
                    IconButton(
                        onClick = { viewModel.openCart() },
                        modifier = Modifier.testTag("top_bar_cart_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = BadgeOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "$cartCount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        bottomBar = {
            FloatingCartBar(
                itemCount = cartCount,
                totalPrice = cartTotal,
                onOpenCart = { viewModel.openCart() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Pinned Search Bar at the very top of the product screen
            TopProductSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                resultCount = filteredProducts.size,
                onClearQuery = { viewModel.updateSearchQuery("") }
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // If not actively searching, show the hero banner
                if (uiState.searchQuery.isBlank()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HeroBanner(
                            onCallHelpline = { WhatsAppHelper.dialHelpline(context) }
                        )
                    }
                }

                // Category Chips & Filter Bar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        CategorySelector(
                            categories = ProductRepository.categories,
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { viewModel.selectCategory(it) }
                        )

                        // Results counter and favorite toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredProducts.size} টি পণ্য পাওয়া গেছে",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.onlyFavorites) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { viewModel.toggleFavoritesOnly() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (uiState.onlyFavorites) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "প্রিয় পণ্য (${uiState.favorites.size})",
                                        fontSize = 12.sp,
                                        color = if (uiState.onlyFavorites) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty state if search or filter returns 0
                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো পণ্য খুঁজে পাওয়া যায়নি",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "\"${uiState.searchQuery}\" দিয়ে কোনো পণ্য মেলেনি।" else "অন্য বিভাগ নির্বাচন করে চেষ্টা করুন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.updateSearchQuery("")
                                    viewModel.selectCategory("সব")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("সব পণ্য দেখুন")
                            }
                        }
                    }
                }

            // Product Cards
            items(filteredProducts, key = { it.id }) { product ->
                val qty = uiState.cartMap[product.id] ?: 0
                val isFav = uiState.favorites.contains(product.id)

                ProductCard(
                    product = product,
                    quantityInCart = qty,
                    isFavorite = isFav,
                    onAddToCart = { viewModel.addToCart(product, 1) },
                    onIncrement = { viewModel.changeQuantity(product.id, 1) },
                    onDecrement = { viewModel.changeQuantity(product.id, -1) },
                    onToggleFavorite = { viewModel.toggleFavorite(product.id) },
                    onClick = { viewModel.selectProductForDetail(product) }
                )
            }
        }
    }
}

    // Cart BottomSheet
    if (uiState.isCartOpen) {
        CartBottomSheet(
            sheetState = cartSheetState,
            items = viewModel.getCartItems(),
            customerInfo = uiState.customerInfo,
            deliveryConfig = uiState.deliveryConfig,
            onDismiss = { viewModel.closeCart() },
            onIncrement = { id -> viewModel.changeQuantity(id, 1) },
            onDecrement = { id -> viewModel.changeQuantity(id, -1) },
            onRemove = { id -> viewModel.removeFromCart(id) },
            onClearCart = { viewModel.clearCart() },
            onUpdateCustomerInfo = { name, phone, address, note ->
                viewModel.updateCustomerInfo(name, phone, address, note)
            },
            onDistanceChange = { km ->
                viewModel.updateDistanceKm(km)
            },
            onSubmitOrder = { viewModel.submitOrder(context) }
        )
    }

    // Order History BottomSheet
    if (uiState.isOrderHistoryOpen) {
        OrderHistorySheet(
            sheetState = historySheetState,
            orders = uiState.orderHistory,
            onDismiss = { viewModel.closeOrderHistory() }
        )
    }

    // Admin / Shopkeeper Management BottomSheet
    if (uiState.isAdminOpen) {
        val allProductsList by viewModel.allProducts.collectAsState()
        AdminSheet(
            sheetState = adminSheetState,
            products = allProductsList,
            deliveryConfig = uiState.deliveryConfig,
            cloudSyncUrl = uiState.cloudSyncUrl,
            lastSyncTime = uiState.lastSyncTime,
            isSyncing = uiState.isSyncing,
            onDismiss = { viewModel.closeAdmin() },
            onUpdateProduct = { viewModel.adminUpdateProduct(it) },
            onAddProduct = { viewModel.adminAddProduct(it) },
            onUpdateDeliveryConfig = { viewModel.adminUpdateDeliveryConfig(it) },
            onSaveCloudUrl = { viewModel.adminSaveCloudUrl(it) },
            onSyncNow = { viewModel.syncOnline() },
            onResetDefaults = { viewModel.adminResetDefaults() },
            exportJsonString = { viewModel.exportCatalogJson() }
        )
    }

    // Product Detail Dialog
    uiState.selectedProductForDetail?.let { product ->
        val currentQty = uiState.cartMap[product.id] ?: 0
        val isFav = uiState.favorites.contains(product.id)

        ProductDetailDialog(
            product = product,
            currentCartQty = currentQty,
            isFavorite = isFav,
            onDismiss = { viewModel.selectProductForDetail(null) },
            onAddToCart = { qty -> viewModel.addToCart(product, qty) },
            onToggleFavorite = { viewModel.toggleFavorite(product.id) }
        )
    }
}
