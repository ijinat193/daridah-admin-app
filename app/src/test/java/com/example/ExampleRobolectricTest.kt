package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProductRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("দাড়িদহ বাজার", appName)
  }

  @Test
  fun `products repository contains 80 items`() {
    assertEquals(80, ProductRepository.products.size)
    assertTrue(ProductRepository.categories.contains("সব"))
    assertTrue(ProductRepository.categories.contains("চাল-ডাল"))
  }

  @Test
  fun `delivery charge calculation logic matches user rules`() {
    val config = com.example.model.DeliveryConfig(baseKm = 3, baseFee = 30, extraPerKmFee = 10)

    // Within 3 km: exactly 30 TK
    assertEquals(30, config.calculateFee(1))
    assertEquals(30, config.calculateFee(2))
    assertEquals(30, config.calculateFee(3))

    // Above 3 km: 30 + 10 per extra km
    assertEquals(40, config.calculateFee(4)) // 30 + 10
    assertEquals(50, config.calculateFee(5)) // 30 + 20
    assertEquals(60, config.calculateFee(6)) // 30 + 30
    assertEquals(100, config.calculateFee(10)) // 30 + 70
  }

  @Test
  fun `user preferences saves and retrieves distance and customer info`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.UserPreferences(context)

    val info = com.example.model.CustomerInfo(
      name = "করিম",
      phone = "01712345678",
      address = "দাড়িদহ উত্তরপাড়া",
      note = "জরুরি",
      distanceKm = 5
    )
    prefs.saveCustomerInfo(info)

    val loaded = prefs.getCustomerInfo()
    assertEquals("করিম", loaded.name)
    assertEquals("01712345678", loaded.phone)
    assertEquals("দাড়িদহ উত্তরপাড়া", loaded.address)
    assertEquals(5, loaded.distanceKm)
  }

  @Test
  fun `search filtering finds products by Bengali and English terms`() {
    val allProducts = ProductRepository.products

    // Search by Bengali name
    val aluMatches = allProducts.filter { it.name.contains("আলু") }
    assertTrue(aluMatches.isNotEmpty())

    // Search by category
    val fishMatches = allProducts.filter { it.category == "মাছ-মাংস" }
    assertTrue(fishMatches.isNotEmpty())

    // Search by rice
    val riceMatches = allProducts.filter { it.name.contains("চাল") }
    assertTrue(riceMatches.isNotEmpty())
  }

  @Test
  fun `verify new WhatsApp number and Daridaha store location constants`() {
    assertEquals("8801772626238", ProductRepository.WHATSAPP_NUMBER)
    assertEquals("01772626238", ProductRepository.HELPLINE_NUMBER)
    assertEquals("38VM+F8", ProductRepository.STORE_PLUS_CODE)
    assertTrue(ProductRepository.deliveryAreas.isNotEmpty())
    assertEquals(1, ProductRepository.deliveryAreas.first().distanceKm)
  }
}
