package com.amigo.ticketbooker.profile.notification

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sharedPreferences = application.getSharedPreferences(
        NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE
    )
    
    private val _notificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _bookingNotificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_BOOKING_NOTIFICATIONS, true)
    )
    val bookingNotificationsEnabled: StateFlow<Boolean> = _bookingNotificationsEnabled.asStateFlow()
    
    private val _offersNotificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_OFFERS_NOTIFICATIONS, true)
    )
    val offersNotificationsEnabled: StateFlow<Boolean> = _offersNotificationsEnabled.asStateFlow()
    
    private val _trainUpdatesNotificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_TRAIN_UPDATES_NOTIFICATIONS, true)
    )
    val trainUpdatesNotificationsEnabled: StateFlow<Boolean> = _trainUpdatesNotificationsEnabled.asStateFlow()
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
            
            // If main notifications are disabled, update the notification manager
            if (!enabled) {
                NotificationManager.cancelAllScheduledNotifications(getApplication())
            } else {
                // If re-enabled, schedule notifications based on current settings
                // Create notification channel first to ensure it exists
                NotificationManager.createNotificationChannel(getApplication())
                scheduleNotificationsIfNeeded()
            }
        }
    }
    
    fun setBookingNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _bookingNotificationsEnabled.value = enabled
            sharedPreferences.edit().putBoolean(KEY_BOOKING_NOTIFICATIONS, enabled).apply()
            scheduleNotificationsIfNeeded()
        }
    }
    
    fun setOffersNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _offersNotificationsEnabled.value = enabled
            sharedPreferences.edit().putBoolean(KEY_OFFERS_NOTIFICATIONS, enabled).apply()
            scheduleNotificationsIfNeeded()
        }
    }
    
    fun setTrainUpdatesNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _trainUpdatesNotificationsEnabled.value = enabled
            sharedPreferences.edit().putBoolean(KEY_TRAIN_UPDATES_NOTIFICATIONS, enabled).apply()
            scheduleNotificationsIfNeeded()
        }
    }
    
    private fun scheduleNotificationsIfNeeded() {
        if (_notificationsEnabled.value) {
            NotificationManager.scheduleRandomNotifications(
                context = getApplication(),
                bookingEnabled = _bookingNotificationsEnabled.value,
                offersEnabled = _offersNotificationsEnabled.value,
                trainUpdatesEnabled = _trainUpdatesNotificationsEnabled.value
            )
        }
    }
    
    companion object {
        private const val NOTIFICATION_PREFERENCES = "notification_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_BOOKING_NOTIFICATIONS = "booking_notifications"
        private const val KEY_OFFERS_NOTIFICATIONS = "offers_notifications"
        private const val KEY_TRAIN_UPDATES_NOTIFICATIONS = "train_updates_notifications"
    }
}
