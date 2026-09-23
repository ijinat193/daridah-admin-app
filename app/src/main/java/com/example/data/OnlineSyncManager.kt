package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.CatalogPayload
import com.example.model.DeliveryConfig
import com.example.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OnlineSyncManager(private val context: Context) {
    private val prefs = UserPreferences(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "OnlineSyncManager"
    }

    /**
     * Loads the active products list. Priority:
     * 1. Cached modified/synced products in UserPreferences
     * 2. Default initial 80 products in ProductRepository
     */
    fun getActiveProducts(): List<Product> {
        val cached = prefs.getCachedProducts()
        return if (!cached.isNullOrEmpty()) {
            cached
        } else {
            ProductRepository.products
        }
    }

    fun getActiveDeliveryConfig(): DeliveryConfig {
        return prefs.getDeliveryConfig()
    }

    fun getStoreNotice(): String {
        return prefs.getStoreNotice()
    }

    fun getCloudSyncUrl(): String {
        return prefs.getCloudSyncUrl()
    }

    fun saveCloudSyncUrl(url: String) {
        prefs.saveCloudSyncUrl(url)
    }

    fun getLastSyncTime(): Long {
        return prefs.getLastSyncTime()
    }

    /**
     * Save product price / details locally (e.g. from Shopkeeper Admin Panel)
     */
    fun updateProductLocally(updated: Product): List<Product> {
        val current = getActiveProducts().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(updated)
        }
        prefs.saveCachedProducts(current)
        return current
    }

    fun addProductLocally(product: Product): List<Product> {
        val current = getActiveProducts().toMutableList()
        current.add(product)
        prefs.saveCachedProducts(current)
        return current
    }

    fun updateDeliveryConfigLocally(config: DeliveryConfig): DeliveryConfig {
        prefs.saveDeliveryConfig(config)
        return config
    }

    fun resetToDefaults(): List<Product> {
        val defaultList = ProductRepository.products
        prefs.saveCachedProducts(defaultList)
        prefs.saveDeliveryConfig(DeliveryConfig(baseKm = 1, baseFee = 30, extraPerKmFee = 25))
        return defaultList
    }

    /**
     * Exports full catalog to JSON string for hosting on GitHub, Google Drive, JSONBin, etc.
     */
    fun exportCatalogJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("lastUpdated", System.currentTimeMillis())
        root.put("notice", prefs.getStoreNotice())

        val delConfig = prefs.getDeliveryConfig()
        val delObj = JSONObject().apply {
            put("baseKm", delConfig.baseKm)
            put("baseFee", delConfig.baseFee)
            put("extraPerKmFee", delConfig.extraPerKmFee)
        }
        root.put("deliveryConfig", delObj)

        val prodArray = JSONArray()
        getActiveProducts().forEach { p ->
            val pObj = JSONObject().apply {
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
            prodArray.put(pObj)
        }
        root.put("products", prodArray)
        return root.toString(2)
    }

    /**
     * Sync with remote online URL
     */
    suspend fun fetchRemoteCatalog(customUrl: String? = null): Result<CatalogPayload> = withContext(Dispatchers.IO) {
        val url = (customUrl ?: prefs.getCloudSyncUrl()).trim()
        if (url.isEmpty() || !url.startsWith("http")) {
            return@withContext Result.failure(IllegalArgumentException("বৈধ ক্লাউড URL পাওয়া যায়নি"))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("ক্লাউড সার্ভার ত্রুটি: HTTP ${response.code}")
                    )
                }

                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.isBlank()) {
                    return@withContext Result.failure(Exception("সার্ভার থেকে কোনো ডাটা পাওয়া যায়নি"))
                }

                val json = JSONObject(bodyStr)
                val version = json.optInt("version", 1)
                val lastUpdated = json.optLong("lastUpdated", System.currentTimeMillis())
                val notice = json.optString("notice", "")

                var deliveryConfig = prefs.getDeliveryConfig()
                if (json.has("deliveryConfig")) {
                    val delJson = json.getJSONObject("deliveryConfig")
                    deliveryConfig = DeliveryConfig(
                        baseKm = delJson.optInt("baseKm", 1),
                        baseFee = delJson.optInt("baseFee", 30),
                        extraPerKmFee = delJson.optInt("extraPerKmFee", 25)
                    )
                    prefs.saveDeliveryConfig(deliveryConfig)
                }

                val productList = mutableListOf<Product>()
                if (json.has("products")) {
                    val pArray = json.getJSONArray("products")
                    for (i in 0 until pArray.length()) {
                        val obj = pArray.getJSONObject(i)
                        productList.add(
                            Product(
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
                }

                if (productList.isNotEmpty()) {
                    prefs.saveCachedProducts(productList)
                }
                prefs.saveLastSyncTime(System.currentTimeMillis())
                if (notice.isNotEmpty()) {
                    prefs.saveStoreNotice(notice)
                }

                val payload = CatalogPayload(
                    version = version,
                    lastUpdated = lastUpdated,
                    notice = notice,
                    deliveryConfig = deliveryConfig,
                    products = productList.ifEmpty { getActiveProducts() }
                )
                Result.success(payload)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
