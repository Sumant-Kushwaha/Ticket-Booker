package com.amigo.ticketbooker.profile.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.amigo.ticketbooker.MainActivity
import com.amigo.ticketbooker.R
import org.json.JSONObject
import java.util.Random
import java.util.concurrent.TimeUnit

object NotificationManager {
    
    private const val CHANNEL_ID = "ticket_booker_channel"
    private const val NOTIFICATION_REQUEST_CODE = 1001
    
    private const val MIN_NOTIFICATION_DELAY = 30 // Minutes
    private const val MAX_NOTIFICATION_DELAY = 180 // Minutes
    
    // Create notification channel for Android O and above
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ticket Booker Notifications"
            val descriptionText = "Notifications for the Ticket Booker app"
            val importance = AndroidNotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // Schedule random notifications
    fun scheduleRandomNotifications(
        context: Context,
        bookingEnabled: Boolean,
        offersEnabled: Boolean,
        trainUpdatesEnabled: Boolean
    ) {
        try {
            // Cancel any existing notifications first
            cancelAllScheduledNotifications(context)
            
            // Only schedule if at least one type is enabled
            if (!(bookingEnabled || offersEnabled || trainUpdatesEnabled)) {
                return
            }
            
            val random = Random()
            
            // Schedule a random notification
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("booking_enabled", bookingEnabled)
                putExtra("offers_enabled", offersEnabled)
                putExtra("train_updates_enabled", trainUpdatesEnabled)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                flags
            )

            // Random delay between MIN and MAX minutes
            val delayMinutes = random.nextInt(MAX_NOTIFICATION_DELAY - MIN_NOTIFICATION_DELAY) + MIN_NOTIFICATION_DELAY
            val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes.toLong())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            // Log the exception but don't crash
            e.printStackTrace()
        }
    }
    
    // Cancel all scheduled notifications
    fun cancelAllScheduledNotifications(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            
            // Use FLAG_UPDATE_CURRENT instead of FLAG_NO_CREATE to ensure we get a PendingIntent
            // even if one doesn't exist yet
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Cancel the alarm and the pending intent
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            // Log the exception but don't crash
            e.printStackTrace()
        }
    }
    
    // This function is no longer needed as we're loading data directly in the receiver
    private fun getNotificationData() {
        // Intentionally left empty - we'll load data directly in the receiver
    }
    
    // Show a notification
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_train)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_purple))
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }
}

// BroadcastReceiver to handle notification events
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val bookingEnabled = intent.getBooleanExtra("booking_enabled", false)
            val offersEnabled = intent.getBooleanExtra("offers_enabled", false)
            val trainUpdatesEnabled = intent.getBooleanExtra("train_updates_enabled", false)
            
            // Create a list of enabled notification types
            val enabledTypes = mutableListOf<String>()
            if (bookingEnabled) enabledTypes.add("booking")
            if (offersEnabled) enabledTypes.add("offers")
            if (trainUpdatesEnabled) enabledTypes.add("train_updates")
            
            if (enabledTypes.isEmpty()) return
            
            // Load notification data directly
            val notificationData = try {
                NotificationDataStore.getNotificationData(context)
            } catch (e: Exception) {
                e.printStackTrace()
                // If we can't load notification data, we can't show a notification
                return
            }
            
            // Select a random notification type from enabled types
            val random = Random()
            val selectedType = enabledTypes[random.nextInt(enabledTypes.size)]
            
            // Get a random notification of the selected type
            val notification = when (selectedType) {
                "booking" -> notificationData.bookingNotifications.takeIf { it.isNotEmpty() }?.random()
                "offers" -> notificationData.offerNotifications.takeIf { it.isNotEmpty() }?.random()
                "train_updates" -> notificationData.trainUpdateNotifications.takeIf { it.isNotEmpty() }?.random()
                else -> null
            }

            // Show the notification if available
            notification?.let {
                try {
                    showRandomNotification(context, it, random.nextInt(1000) + 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Schedule the next notification
            try {
                com.amigo.ticketbooker.profile.notification.NotificationManager.scheduleRandomNotifications(
                    context,
                    bookingEnabled,
                    offersEnabled,
                    trainUpdatesEnabled
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun <T> List<T>.randomOrNull(): T? {
        if (isEmpty()) return null
        return this[Random().nextInt(size)]
    }
    
    private fun showRandomNotification(context: Context, notification: NotificationItem, notificationId: Int) {
        com.amigo.ticketbooker.profile.notification.NotificationManager.showNotification(
            context,
            notification.title,
            notification.body,
            notificationId
        )
    }
}
