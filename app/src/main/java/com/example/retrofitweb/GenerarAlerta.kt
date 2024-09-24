package com.example.retrofitweb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import com.example.myapp.BaseActivity
import com.example.retrofitweb.service.UVIndexService

class GenerarAlerta : BaseActivity() {

    private lateinit var alerta: Switch
    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.generar_alerta, findViewById(R.id.content_frame))

        alerta = findViewById(R.id.id_alerta)
        notificationManager = NotificationManager(this)
        sharedPreferences = getSharedPreferences("alert_prefs", Context.MODE_PRIVATE)

        // Restaurar el estado del toggle button
        val isNotificationEnabled = sharedPreferences.getBoolean("is_notification_enabled", false)
        alerta.isChecked = isNotificationEnabled

        alerta.setOnCheckedChangeListener { _, isChecked ->
            // Guardar el estado del toggle button
            sharedPreferences.edit().putBoolean("is_notification_enabled", isChecked).apply()

            if (isChecked) {
                notificationManager.enableNotifications()
                // Iniciar el servicio UVIndexService
                val intent = Intent(this, UVIndexService::class.java)
                intent.putExtra("activationTime", System.currentTimeMillis())
                startService(intent)
            } else {
                notificationManager.disableNotifications()
                // Detener el servicio UVIndexService
                stopService(Intent(this, UVIndexService::class.java))
            }
        }
    }
}