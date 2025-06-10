package com.amigo.ticketbooker.notification

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class NotificationData(
    val bookingNotifications: List<NotificationItem> = emptyList(),
    val offerNotifications: List<NotificationItem> = emptyList(),
    val trainUpdateNotifications: List<NotificationItem> = emptyList()
)

data class NotificationItem(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val imageUrl: String? = null
)

object NotificationDataStore {
    private const val NOTIFICATION_JSON_FILE = "notifications.json"
    private var cachedNotificationData: NotificationData? = null
    
    fun getNotificationData(context: Context): NotificationData {
        // Return cached data if available
        cachedNotificationData?.let { return it }
        
        // Check if the JSON file exists in assets
        try {
            val inputStream = context.assets.open(NOTIFICATION_JSON_FILE)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = bufferedReader.use { it.readText() }
            
            // Parse JSON
            val notificationData = parseNotificationData(jsonString)
            cachedNotificationData = notificationData
            return notificationData
        } catch (e: Exception) {
            e.printStackTrace() // Already present
            Log.w("NotificationDataStore", "Using default notification data due to error: ${e.message}")
            return createDefaultNotificationData().also {
                cachedNotificationData = it
            }
        }

    }
    
    fun parseNotificationData(jsonString: String): NotificationData {
        try {
            val jsonObject = JSONObject(jsonString)
            
            // Parse booking notifications
            val bookingArray = jsonObject.optJSONArray("bookingNotifications")
            val bookingNotifications = mutableListOf<NotificationItem>()
            for (i in 0 until (bookingArray?.length() ?: 0)) {
                val item = bookingArray?.getJSONObject(i)
                item?.let {
                    bookingNotifications.add(
                        NotificationItem(
                            title = it.optString("title", ""),
                            body = it.optString("body", ""),
                            deepLink = it.optString("deepLink"),
                            imageUrl = it.optString("imageUrl")
                        )
                    )
                }
            }
            
            // Parse offer notifications
            val offerArray = jsonObject.optJSONArray("offerNotifications")
            val offerNotifications = mutableListOf<NotificationItem>()
            for (i in 0 until (offerArray?.length() ?: 0)) {
                val item = offerArray?.getJSONObject(i)
                item?.let {
                    offerNotifications.add(
                        NotificationItem(
                            title = it.optString("title", ""),
                            body = it.optString("body", ""),
                            deepLink = it.optString("deepLink"),
                            imageUrl = it.optString("imageUrl")
                        )
                    )
                }
            }
            
            // Parse train update notifications
            val trainArray = jsonObject.optJSONArray("trainUpdateNotifications")
            val trainUpdateNotifications = mutableListOf<NotificationItem>()
            for (i in 0 until (trainArray?.length() ?: 0)) {
                val item = trainArray?.getJSONObject(i)
                item?.let {
                    trainUpdateNotifications.add(
                        NotificationItem(
                            title = it.optString("title", ""),
                            body = it.optString("body", ""),
                            deepLink = it.optString("deepLink"),
                            imageUrl = it.optString("imageUrl")
                        )
                    )
                }
            }
            
            return NotificationData(
                bookingNotifications = bookingNotifications,
                offerNotifications = offerNotifications,
                trainUpdateNotifications = trainUpdateNotifications
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return createDefaultNotificationData()
        }
    }
    
    private fun createDefaultNotificationData(): NotificationData {
        // Create default notification data if JSON file is not available
        return NotificationData(
            bookingNotifications = listOf(
                NotificationItem(
                    title = "Booking Confirmed!",
                    body = "Your ticket has been confirmed. Have a safe journey!"
                ),
                NotificationItem(
                    title = "Booking Update",
                    body = "Your ticket status has been updated to RAC. Check your booking for details."
                ),
                NotificationItem(
                    title = "Booking Reminder",
                    body = "Your journey to Mumbai starts tomorrow. Don't forget to pack!"
                ),
                NotificationItem(
                    title = "Booking Waitlisted",
                    body = "Your ticket is currently waitlisted at position WL5. We'll notify you of any changes."
                ),
                NotificationItem(
                    title = "Payment Successful",
                    body = "Your payment of ₹1,250 for booking ID IRCTC12345 has been received."
                )
            ),
            offerNotifications = listOf(
                NotificationItem(
                    title = "Special Discount!",
                    body = "Use code TRAIN20 to get 20% off on your next booking!"
                ),
                NotificationItem(
                    title = "Weekend Offer",
                    body = "Book tickets for weekend travel and get 10% cashback!"
                ),
                NotificationItem(
                    title = "New User Offer",
                    body = "First time booking? Use WELCOME15 for a special discount!"
                ),
                NotificationItem(
                    title = "Premium Membership",
                    body = "Upgrade to premium and get priority bookings and exclusive offers!"
                ),
                NotificationItem(
                    title = "Refer & Earn",
                    body = "Refer a friend and both get 50 tokens for free bookings!"
                )
            ),
            trainUpdateNotifications = listOf(
                NotificationItem(
                    title = "Train Delayed",
                    body = "Rajdhani Express (12301) is running 30 minutes late."
                ),
                NotificationItem(
                    title = "Platform Change",
                    body = "Your train will now depart from Platform 5 instead of Platform 3."
                ),
                NotificationItem(
                    title = "Train Arriving Soon",
                    body = "Your train is arriving in 15 minutes. Please proceed to the platform."
                ),
                NotificationItem(
                    title = "PNR Status Update",
                    body = "Your PNR status has changed from WL3 to Confirmed. Check details."
                ),
                NotificationItem(
                    title = "Train Cancelled",
                    body = "Due to maintenance, train number 12345 has been cancelled today."
                )
            )
        )
    }
}
