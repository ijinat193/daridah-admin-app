package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.ProductRepository
import com.example.model.CartItem
import com.example.model.CustomerInfo
import java.net.URLEncoder

object WhatsAppHelper {

    fun formatOrderMessage(
        items: List<CartItem>,
        info: CustomerInfo,
        itemsTotal: Int,
        deliveryFee: Int = 30,
        distanceKm: Int = 3,
        grandTotal: Int = itemsTotal + deliveryFee
    ): String {
        val sb = StringBuilder()
        sb.append("🛒 *দাড়িদহ বাজার - নতুন অর্ডার*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("👤 *গ্রাহকের নাম:* ${info.name}\n")
        sb.append("📱 *মোবাইল:* ${info.phone}\n")
        sb.append("📍 *ডেলিভারি ঠিকানা:* ${info.address}\n")
        sb.append("🛵 *ডেলিভারি দূরত্ব:* $distanceKm কি.মি.\n")
        if (info.note.isNotBlank()) {
            sb.append("📝 *বিশেষ নোট:* ${info.note}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *পণ্যের তালিকা:*\n")

        items.forEach { item ->
            val subtotal = item.product.price * item.quantity
            sb.append("• ${item.product.icon} ${item.product.name} × ${item.quantity} ${item.product.unit} = ৳$subtotal\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 *পণ্যের মোট মূল্য:* ৳$itemsTotal\n")
        val feeDetail = if (distanceKm <= 1) {
            "৳$deliveryFee (১ কি.মি. বেস চার্জ)"
        } else {
            val extraKm = distanceKm - 1
            val extraFee = extraKm * 25
            "৳$deliveryFee (১ কি.মি. ৩০৳ + অতিরিক্ত $extraKm কি.মি. ${extraFee}৳)"
        }
        sb.append("🛵 *ডেলিভারি চার্জ ($distanceKm কি.মি.):* $feeDetail\n")
        sb.append("💰 *সর্বমোট প্রদেয়: ৳$grandTotal*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🙏 দাড়িদহ বাজার অ্যাপ থেকে পাঠানো অর্ডার।")

        return sb.toString()
    }

    fun openWhatsApp(context: Context, message: String): Boolean {
        return try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${ProductRepository.WHATSAPP_NUMBER}&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // Secondary fallback via browser wa.me
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val fallbackUri = Uri.parse("https://wa.me/${ProductRepository.WHATSAPP_NUMBER}?text=$encodedMessage")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp ওপেন করা যাচ্ছে না: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    fun dialHelpline(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ProductRepository.HELPLINE_NUMBER}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "কল করা সম্ভব হচ্ছে না: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp(context: Context) {
        try {
            val shareText = "🛒 *${ProductRepository.SHOP_NAME} - অনলাইন শপ*\n" +
                    "${ProductRepository.SHOP_TAGLINE}\n\n" +
                    "🛵 ঘরে বসে বাজারের নিত্যপ্রয়োজনীয় সকল পণ্য কিনুন সহজেই।\n" +
                    "📍 শপ লোকেশন: ${ProductRepository.STORE_LOCATION_NAME}\n" +
                    "🚚 ডেলিভারি চার্জ: ১ কি.মি. পর্যন্ত ৩০৳, এরপর প্রতি কি.মি. ২৫৳\n" +
                    "📞 হেল্পলাইন/WhatsApp: ${ProductRepository.HELPLINE_NUMBER}\n\n" +
                    "অ্যাপটি ব্যবহার করুন ও আপনার মতামত/রিভিউ দিন:\n${ProductRepository.APP_SHARE_URL}"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, ProductRepository.SHOP_NAME)
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "দাড়িদহ বাজার শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা যাচ্ছে না: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDaridahaMap(context: Context) {
        try {
            // Plus code 38VM+F8 Daridaha
            val geoUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${URLEncoder.encode("38VM+F8 Daridaha", "UTF-8")}")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "ম্যাপ ওপেন করা যাচ্ছে না: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
