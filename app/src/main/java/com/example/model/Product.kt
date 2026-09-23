package com.example.model

data class DeliveryConfig(
    val baseKm: Int = 1,
    val baseFee: Int = 30,
    val extraPerKmFee: Int = 25
) {
    fun calculateFee(distanceKm: Int): Int {
        val km = distanceKm.coerceAtLeast(1)
        return if (km <= baseKm) {
            baseFee
        } else {
            baseFee + (km - baseKm) * extraPerKmFee
        }
    }

    fun getFeeBreakdown(distanceKm: Int): String {
        val km = distanceKm.coerceAtLeast(1)
        return if (km <= baseKm) {
            "$km কি.মি. বেস চার্জ ৳$baseFee"
        } else {
            val extraKm = km - baseKm
            val extraCost = extraKm * extraPerKmFee
            "$baseKm কি.মি. ৳$baseFee + অতিরিক্ত $extraKm কি.মি. ৳$extraCost"
        }
    }
}

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val unit: String = "কেজি",
    val category: String = "সাধারণ",
    val icon: String = "📦",
    val imageUrl: String = "",
    val isPopular: Boolean = false,
    val inStock: Boolean = true
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Int
        get() = product.price * quantity
}

data class OrderRecord(
    val id: String,
    val timestamp: Long,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val totalAmount: Int,
    val itemCount: Int,
    val summaryText: String,
    val distanceKm: Int = 1,
    val deliveryFee: Int = 30,
    val grandTotal: Int = totalAmount + deliveryFee
)

data class CustomerInfo(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val distanceKm: Int = 1
)

data class CatalogPayload(
    val version: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis(),
    val notice: String = "",
    val deliveryConfig: DeliveryConfig = DeliveryConfig(),
    val products: List<Product> = emptyList()
)
