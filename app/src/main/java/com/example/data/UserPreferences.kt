package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CustomerInfo
import com.example.model.OrderRecord
import org.json.JSONArray
import org.json.JSONObject

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("daridaho_bazar_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "cust_name"
        private const val KEY_PHONE = "cust_phone"
        private const val KEY_ADDRESS = "cust_address"
        private const val KEY_NOTE = "cust_note"
        private const val KEY_DISTANCE = "cust_distance"
        private const val KEY_FAVORITES = "favorite_ids"
        private const val KEY_ORDERS = "past_orders"
        private const val KEY_PRODUCTS_CACHE = "products_cache_json"
        private const val KEY_DELIVERY_BASE_KM = "delivery_base_km"
        private const val KEY_DELIVERY_BASE_FEE = "delivery_base_fee"
        private const val KEY_DELIVERY_EXTRA_FEE = "delivery_extra_fee"
        private const val KEY_CLOUD_URL = "cloud_sync_url"
        private const val KEY_LAST_SYNC = "last_sync_time"
        private const val KEY_NOTICE = "store_notice"
    }

    fun getCustomerInfo(): CustomerInfo {
        return CustomerInfo(
            name = prefs.getString(KEY_NAME, "") ?: "",
            phone = prefs.getString(KEY_PHONE, "") ?: "",
            address = prefs.getString(KEY_ADDRESS, "") ?: "",
            note = prefs.getString(KEY_NOTE, "") ?: "",
            distanceKm = prefs.getInt(KEY_DISTANCE, 1).coerceAtLeast(1)
        )
    }

    fun saveCustomerInfo(info: CustomerInfo) {
        prefs.edit()
            .putString(KEY_NAME, info.name)
            .putString(KEY_PHONE, info.phone)
            .putString(KEY_ADDRESS, info.address)
            .putString(KEY_NOTE, info.note)
            .putInt(KEY_DISTANCE, info.distanceKm.coerceAtLeast(1))
            .apply()
    }

    fun getDeliveryConfig(): com.example.model.DeliveryConfig {
        val baseKm = prefs.getInt(KEY_DELIVERY_BASE_KM, 1)
        val baseFee = prefs.getInt(KEY_DELIVERY_BASE_FEE, 30)
        val extraFee = prefs.getInt(KEY_DELIVERY_EXTRA_FEE, 25)
        return com.example.model.DeliveryConfig(baseKm, baseFee, extraFee)
    }

    fun saveDeliveryConfig(config: com.example.model.DeliveryConfig) {
        prefs.edit()
            .putInt(KEY_DELIVERY_BASE_KM, config.baseKm)
            .putInt(KEY_DELIVERY_BASE_FEE, config.baseFee)
            .putInt(KEY_DELIVERY_EXTRA_FEE, config.extraPerKmFee)
            .apply()
    }

    fun getCloudSyncUrl(): String {
        return prefs.getString(
            KEY_CLOUD_URL,
            "https://raw.githubusercontent.com/daridahobazar/catalog/main/products.json"
        ) ?: ""
    }

    fun saveCloudSyncUrl(url: String) {
        prefs.edit().putString(KEY_CLOUD_URL, url.trim()).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    fun saveLastSyncTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, time).apply()
    }

    fun getStoreNotice(): String {
        return prefs.getString(KEY_NOTICE, "") ?: ""
    }

    fun saveStoreNotice(notice: String) {
        prefs.edit().putString(KEY_NOTICE, notice).apply()
    }

    fun getCachedProducts(): List<com.example.model.Product>? {
        val jsonStr = prefs.getString(KEY_PRODUCTS_CACHE, null) ?: return null
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<com.example.model.Product>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.model.Product(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        price = obj.getInt("price"),
                        unit = obj.getString("unit"),
                        category = obj.getString("category"),
                        icon = obj.optString("icon", "📦"),
                        imageUrl = obj.optString("imageUrl", ""),
                        isPopular = obj.optBoolean("isPopular", false),
                        inStock = obj.optBoolean("inStock", true)
                    )
                )
            }
            if (list.isNotEmpty()) list else null
        } catch (_: Exception) {
            null
        }
    }

    fun saveCachedProducts(products: List<com.example.model.Product>) {
        val array = JSONArray()
        products.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("price", p.price)
                put("unit", p.unit)
                put("category", p.category)
                put("icon", p.icon)
                put("imageUrl", p.imageUrl)
                put("isPopular", p.isPopular)
                put("inStock", p.inStock)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PRODUCTS_CACHE, array.toString()).apply()
    }

    fun getFavoriteIds(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavorite(productId: Int): Set<Int> {
        val current = getFavoriteIds().toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        prefs.edit()
            .putStringSet(KEY_FAVORITES, current.map { it.toString() }.toSet())
            .apply()
        return current
    }

    fun getOrderHistory(): List<OrderRecord> {
        val jsonStr = prefs.getString(KEY_ORDERS, null) ?: return emptyList()
        val list = mutableListOf<OrderRecord>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val amt = obj.getInt("totalAmount")
                val dist = obj.optInt("distanceKm", 3)
                val fee = obj.optInt("deliveryFee", 30)
                val grand = obj.optInt("grandTotal", amt + fee)
                list.add(
                    OrderRecord(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        customerName = obj.getString("customerName"),
                        customerPhone = obj.getString("customerPhone"),
                        address = obj.getString("address"),
                        totalAmount = amt,
                        itemCount = obj.getInt("itemCount"),
                        summaryText = obj.getString("summaryText"),
                        distanceKm = dist,
                        deliveryFee = fee,
                        grandTotal = grand
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse error
        }
        return list
    }

    fun saveOrder(order: OrderRecord) {
        val current = getOrderHistory().toMutableList()
        current.add(0, order) // latest first
        // keep up to 50 orders
        val trimmed = if (current.size > 50) current.take(50) else current

        val jsonArray = JSONArray()
        trimmed.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("timestamp", item.timestamp)
                put("customerName", item.customerName)
                put("customerPhone", item.customerPhone)
                put("address", item.address)
                put("totalAmount", item.totalAmount)
                put("itemCount", item.itemCount)
                put("summaryText", item.summaryText)
                put("distanceKm", item.distanceKm)
                put("deliveryFee", item.deliveryFee)
                put("grandTotal", item.grandTotal)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ORDERS, jsonArray.toString()).apply()
    }
}
