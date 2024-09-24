package com.example.retrofitweb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.retrofitweb.service.UVIndexService

class NotificationManager(private val context: Context) {
    private val alertPreferences: SharedPreferences = context.getSharedPreferences("alert_prefs", Context.MODE_PRIVATE)
    private var isNotificationEnabled: Boolean = false

    fun enableNotifications() {
        isNotificationEnabled = true
        updateButtonText()
        alertPreferences.edit().putBoolean("is_notification_enabled", isNotificationEnabled).apply()
        // Start the notification service
        val intent = Intent(context, UVIndexService::class.java)
        intent.putExtra("activationTime", System.currentTimeMillis())
        context.startService(intent)
        Log.d("NotificationManager", "Notificaciones activadas")
    }

    fun disableNotifications() {
        isNotificationEnabled = false
        updateButtonText()
        alertPreferences.edit().putBoolean("is_notification_enabled", isNotificationEnabled).apply()
        // Stop the notification service
        val intent = Intent(context, UVIndexService::class.java)
        context.stopService(intent)
        Log.d("NotificationManager", "Notificaciones desactivadas")
    }

    private fun updateButtonText() {
        // Update the button text based on the notification state
        // No es necesario implementar esto aquí, ya que el texto del botón se actualizará en la actividad
    }
}