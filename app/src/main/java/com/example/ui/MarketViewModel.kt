package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OnlineSyncManager
import com.example.data.ProductRepository
import com.example.data.UserPreferences
import com.example.model.CartItem
import com.example.model.CustomerInfo
import com.example.model.DeliveryConfig
import com.example.model.OrderRecord
import com.example.model.Product
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MarketUiState(
    val selectedCategory: String = "সব",
    val searchQuery: String = "",
    val onlyFavorites: Boolean = false,
    val cartMap: Map<Int, Int> = emptyMap(), // productId -> quantity
    val favorites: Set<Int> = emptySet(),
    val customerInfo: CustomerInfo = CustomerInfo(),
    val orderHistory: List<OrderRecord> = emptyList(),
    val isCartOpen: Boolean = false,
    val isOrderHistoryOpen: Boolean = false,
    val isAdminOpen: Boolean = false,
    val selectedProductForDetail: Product? = null,
    val lastPlacedOrder: OrderRecord? = null,
    val deliveryConfig: DeliveryConfig = DeliveryConfig(),
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val syncStatusMessage: String? = null,
    val cloudSyncUrl: String = ""
)

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val syncManager = OnlineSyncManager(application)

    private val _products = MutableStateFlow<List<Product>>(syncManager.getActiveProducts())
    val allProducts: StateFlow<List<Product>> = _products

    private val _uiState = MutableStateFlow(
        MarketUiState(
            favorites = userPreferences.getFavoriteIds(),
            customerInfo = userPreferences.getCustomerInfo(),
            orderHistory = userPreferences.getOrderHistory(),
            deliveryConfig = syncManager.getActiveDeliveryConfig(),
            lastSyncTime = syncManager.getLastSyncTime(),
            cloudSyncUrl = syncManager.getCloudSyncUrl()
        )
    )
    val uiState: StateFlow<MarketUiState> = _uiState

    init {
        // Auto-check online catalog on startup in background
        syncOnline(silent = true)
    }

    val filteredProducts: StateFlow<List<Product>> = combine(_uiState, _products) { state, products ->
        val query = state.searchQuery.trim().lowercase()
        products.filter { product ->
            val matchesCategory = (state.selectedCategory == "সব") || (product.category == state.selectedCategory)
            val matchesSearch = query.isBlank() || matchesProductSearch(product, query)
            val matchesFavorite = !state.onlyFavorites || state.favorites.contains(product.id)

            matchesCategory && matchesSearch && matchesFavorite
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _products.value)

    private fun matchesProductSearch(product: Product, query: String): Boolean {
        val nameLower = product.name.lowercase()
        val catLower = product.category.lowercase()
        val unitLower = product.unit.lowercase()

        if (nameLower.contains(query) || catLower.contains(query) || unitLower.contains(query)) {
            return true
        }

        // Support popular phonetic / English terms for quick mobile search
        return when {
            query.startsWith("al") || query == "potato" -> nameLower.contains("আলু")
            query.startsWith("di") || query == "egg" || query == "anda" -> nameLower.contains("ডিম")
            query.startsWith("te") || query.startsWith("oil") || query.contains("soya") -> nameLower.contains("তেল")
            query.startsWith("cha") || query == "rice" || query == "bhat" -> nameLower.contains("চাল") || nameLower.contains("চা")
            query.startsWith("da") || query == "lentil" -> nameLower.contains("ডাল")
            query.startsWith("pe") || query.startsWith("on") -> nameLower.contains("পেঁয়াজ")
            query.startsWith("ro") || query.startsWith("gar") -> nameLower.contains("রসুন")
            query.startsWith("ad") || query.startsWith("gin") -> nameLower.contains("আদা")
            query.startsWith("chi") || query.startsWith("sug") -> nameLower.contains("চিনি")
            query.startsWith("lo") || query.startsWith("sal") -> nameLower.contains("লবণ")
            query.startsWith("ma") || query == "fish" || query.contains("rui") -> nameLower.contains("মাছ") || nameLower.contains("রুই")
            query.startsWith("mu") || query == "chicken" || query == "meat" || query == "beef" -> nameLower.contains("মুরগি") || nameLower.contains("মাংস") || nameLower.contains("গরু")
            else -> false
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavoritesOnly() {
        _uiState.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        _uiState.update { state ->
            val current = state.cartMap.toMutableMap()
            val newQty = (current[product.id] ?: 0) + quantity
            current[product.id] = newQty
            state.copy(cartMap = current)
        }
    }

    fun changeQuantity(productId: Int, delta: Int) {
        _uiState.update { state ->
            val current = state.cartMap.toMutableMap()
            val existing = current[productId] ?: 0
            val updated = existing + delta
            if (updated <= 0) {
                current.remove(productId)
            } else {
                current[productId] = updated
            }
            state.copy(cartMap = current)
        }
    }

    fun removeFromCart(productId: Int) {
        _uiState.update { state ->
            val current = state.cartMap.toMutableMap()
            current.remove(productId)
            state.copy(cartMap = current)
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartMap = emptyMap()) }
    }

    fun toggleFavorite(productId: Int) {
        val updated = userPreferences.toggleFavorite(productId)
        _uiState.update { it.copy(favorites = updated) }
    }

    fun openCart() {
        _uiState.update { it.copy(isCartOpen = true) }
    }

    fun closeCart() {
        _uiState.update { it.copy(isCartOpen = false) }
    }

    fun openOrderHistory() {
        _uiState.update { it.copy(isOrderHistoryOpen = true) }
    }

    fun closeOrderHistory() {
        _uiState.update { it.copy(isOrderHistoryOpen = false) }
    }

    fun openAdmin() {
        _uiState.update { it.copy(isAdminOpen = true) }
    }

    fun closeAdmin() {
        _uiState.update { it.copy(isAdminOpen = false) }
    }

    fun selectProductForDetail(product: Product?) {
        _uiState.update { it.copy(selectedProductForDetail = product) }
    }

    fun updateCustomerInfo(name: String, phone: String, address: String, note: String = "", distanceKm: Int? = null) {
        val currentDist = distanceKm ?: _uiState.value.customerInfo.distanceKm
        val info = CustomerInfo(
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            note = note.trim(),
            distanceKm = currentDist.coerceAtLeast(1)
        )
        userPreferences.saveCustomerInfo(info)
        _uiState.update { it.copy(customerInfo = info) }
    }

    fun updateDistanceKm(distanceKm: Int) {
        val validDist = distanceKm.coerceAtLeast(1)
        val current = _uiState.value.customerInfo
        val updated = current.copy(distanceKm = validDist)
        userPreferences.saveCustomerInfo(updated)
        _uiState.update { it.copy(customerInfo = updated) }
    }

    fun getCartItems(): List<CartItem> {
        val map = _uiState.value.cartMap
        val list = _products.value
        return map.mapNotNull { (id, qty) ->
            list.find { it.id == id }?.let { CartItem(it, qty) }
        }
    }

    fun getCartCount(): Int {
        return _uiState.value.cartMap.values.sum()
    }

    fun getCartTotal(): Int {
        return getCartItems().sumOf { it.subtotal }
    }

    fun getDeliveryFee(): Int {
        val dist = _uiState.value.customerInfo.distanceKm
        return _uiState.value.deliveryConfig.calculateFee(dist)
    }

    fun getGrandTotal(): Int {
        return getCartTotal() + getDeliveryFee()
    }

    fun submitOrder(context: Context): Boolean {
        val items = getCartItems()
        if (items.isEmpty()) {
            Toast.makeText(context, "আগে কিছু পণ্য কার্টে যোগ করুন।", Toast.LENGTH_SHORT).show()
            return false
        }

        val info = _uiState.value.customerInfo
        if (info.name.isBlank() || info.phone.isBlank() || info.address.isBlank()) {
            Toast.makeText(context, "দয়া করে নাম, মোবাইল নম্বর ও ঠিকানা পূরণ করুন।", Toast.LENGTH_LONG).show()
            return false
        }

        val itemsTotal = getCartTotal()
        val deliveryFee = getDeliveryFee()
        val distanceKm = info.distanceKm
        val grandTotal = getGrandTotal()

        val message = WhatsAppHelper.formatOrderMessage(
            items = items,
            info = info,
            itemsTotal = itemsTotal,
            deliveryFee = deliveryFee,
            distanceKm = distanceKm,
            grandTotal = grandTotal
        )

        // Save order to history
        val orderRecord = OrderRecord(
            id = "ORD-" + UUID.randomUUID().toString().take(6).uppercase(),
            timestamp = System.currentTimeMillis(),
            customerName = info.name,
            customerPhone = info.phone,
            address = info.address,
            totalAmount = itemsTotal,
            itemCount = getCartCount(),
            summaryText = items.joinToString(", ") { "${it.product.name} (${it.quantity})" },
            distanceKm = distanceKm,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal
        )
        userPreferences.saveOrder(orderRecord)
        _uiState.update {
            it.copy(
                orderHistory = userPreferences.getOrderHistory(),
                lastPlacedOrder = orderRecord
            )
        }

        val launched = WhatsAppHelper.openWhatsApp(context, message)
        if (launched) {
            clearCart()
            closeCart()
        }
        return launched
    }

    fun reorder(order: OrderRecord) {
        Toast.makeText(getApplication(), "অর্ডারের বিবরণী আবার কার্টে যুক্ত করা যাবে।", Toast.LENGTH_SHORT).show()
    }

    // --- Online Sync & Shopkeeper Admin Controls ---

    fun syncOnline(customUrl: String? = null, silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = "অনলাইন ডাটাবেজের সাথে সিঙ্ক হচ্ছে...") }
            val result = syncManager.fetchRemoteCatalog(customUrl)
            result.onSuccess { payload ->
                _products.value = payload.products
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        deliveryConfig = payload.deliveryConfig,
                        lastSyncTime = payload.lastUpdated,
                        syncStatusMessage = "অনলাইন থেকে সফলভাবে আপডেট হয়েছে! (${payload.products.size}টি পণ্য)"
                    )
                }
                if (!silent) {
                    Toast.makeText(getApplication(), "অনলাইন থেকে বাজার আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncStatusMessage = if (silent) null else "সিঙ্ক ব্যর্থ: ${ex.localizedMessage ?: "নেটওয়ার্ক চেক করুন"}"
                    )
                }
                if (!silent) {
                    Toast.makeText(getApplication(), "সিঙ্ক ব্যর্থ: ইন্টারনেট কানেকশন চেক করুন", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun adminUpdateProduct(product: Product) {
        val updated = syncManager.updateProductLocally(product)
        _products.value = updated
        Toast.makeText(getApplication(), "${product.name}-এর মূল্য আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun adminAddProduct(product: Product) {
        val updated = syncManager.addProductLocally(product)
        _products.value = updated
        Toast.makeText(getApplication(), "নতুন পণ্য ${product.name} যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun adminUpdateDeliveryConfig(config: DeliveryConfig) {
        val updated = syncManager.updateDeliveryConfigLocally(config)
        _uiState.update { it.copy(deliveryConfig = updated) }
        Toast.makeText(getApplication(), "ডেলিভারি চার্জ নিয়ম আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun adminResetDefaults() {
        val resetList = syncManager.resetToDefaults()
        _products.value = resetList
        _uiState.update {
            it.copy(deliveryConfig = DeliveryConfig(baseKm = 3, baseFee = 30, extraPerKmFee = 10))
        }
        Toast.makeText(getApplication(), "ডিফল্ট ৮০টি পণ্যে রিসেট করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun adminSaveCloudUrl(url: String) {
        syncManager.saveCloudSyncUrl(url)
        _uiState.update { it.copy(cloudSyncUrl = url) }
        syncOnline(customUrl = url)
    }

    fun exportCatalogJson(): String {
        return syncManager.exportCatalogJson()
    }
}
