package com.example.retrofitweb

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.retrofitweb.interfaces.RetrofitBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CustomAlertService : Service() {
    private lateinit var job: Job
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val retrofitService by lazy {
        RetrofitBuilder.getRetrofitService()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        startForeground(NOTIFICATION_ID_CUSTOM, createNotificationPermanent())
        return START_STICKY
    }

    private fun startMonitoring() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                checkAndNotify()
                delay(15 * 60 * 1000) // Verificar cada 15 minutos
            }
        }
    }

    private suspend fun checkAndNotify() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val selectedScale = prefs.getString("color_scale", "Muy Alto")

        try {
            val uvIndexDataList = retrofitService.getUVIndexData()
            val currentUVIndex = uvIndexDataList.lastOrNull()?.uvIndex ?: return

            val shouldNotify = when (selectedScale) {
                "Bajo" -> currentUVIndex in 0.0..2.9
                "Moderado" -> currentUVIndex in 3.0..5.9
                "Alto" -> currentUVIndex in 6.0..7.9
                "Muy Alto" -> currentUVIndex in 8.0..10.9
                "Extremadamente Alto" -> currentUVIndex >= 11.0
                else -> false
            }

            if (shouldNotify) {
                showNotification("Alerta de índice UV personalizada", "El índice UV actual es $currentUVIndex (${selectedScale}).")
            }
        } catch (e: Exception) {
            Log.e("CustomAlertService", "Error al obtener datos: ${e.message}")
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.uv_alto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID_DATA, notification)
    }

    private fun createNotificationPermanent(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Servicio de Alerta Personalizada")
            .setContentText("El servicio de alerta personalizada está funcionando en segundo plano")
            .setSmallIcon(R.drawable.uv_alto)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val CHANNEL_ID = "custom_alert_channel"
        const val NOTIFICATION_ID_CUSTOM = 2
        const val NOTIFICATION_ID_DATA = 3
    }
}