package com.example.retrofitweb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
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

        //Spinner opciones
        val opciones = arrayOf(
            "Nivel bajo [0 - 2]",
            "Nivel moderado [3 - 5]",
            "Nivel alto [6 - 7]",
            "Nivel muy alto [8 - 10]",
            "Nivel extremadamente alto [11+]")

        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

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