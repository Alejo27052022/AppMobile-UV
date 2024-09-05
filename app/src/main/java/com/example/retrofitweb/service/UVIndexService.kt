/*
    Este servicio se ejecuta en segundo plano y se encarga de obtener los datos del índice UV desde la API.
    Filtra los datos según la escala de color y la hora seleccionada por el usuario,
    y genera una notificación si el índice UV se encuentra dentro del rango especificado.
    Utiliza CoroutineScope para realizar operaciones de red de forma asíncrona.
 */

package com.example.retrofitweb.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.retrofitweb.R
import com.example.retrofitweb.data.models.UVIndexData
import com.example.retrofitweb.interfaces.RetrofitBuilder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

private const val NOTIFICATION_ID = 1
private const val NOTIFICATION_ID_PERMANENT = 1
private const val NOTIFICATION_ID_DATA = 2

class UVIndexService : Service() {

    private var lastNotificationData: UVIndexData? = null

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // Servicio Retrofit para hacer llamada a la API
    private val retrofitService by lazy {
        RetrofitBuilder.getRetrofitService()
    }

    private lateinit var job: Job
    private var activationTime: Long = 0
    private var currentNotification: Notification? = null
    private lateinit var wakeLock: PowerManager.WakeLock

    private var lastNotifiedData: UVIndexData? = null

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
                    // Actualizar la notificación para mostrar el error de conexión
                    val notification = createNotificationPermanentWithError("No hay conexión a Internet")
                    notificationManager.notify(NOTIFICATION_ID_PERMANENT, notification)
                } else {
                    // Actualizar la notificación para mostrar que hay conexión
                    val notification = createNotificationPermanent()
                    notificationManager.notify(NOTIFICATION_ID_PERMANENT, notification)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Registrar el BroadcastReceiver
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        activationTime = intent?.getLongExtra("activationTime", 0) ?: 0
        startMonitoring()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.example.myapp:uvindex_wakelock")
        wakeLock.acquire()

        val notification: Notification
        if (isNetworkAvailable()) {
            notification = createNotificationPermanent()
        } else {
            notification = createNotificationPermanentWithError("No hay conexión a Internet")
        }
        startForeground(NOTIFICATION_ID_PERMANENT, notification)
        return START_STICKY
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                getUVIndexData()
                delay(60 * 1000) // Verificar cada minuto
            }
        }
    }

    private fun getUVIndexData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uvIndexDataList = retrofitService.getUVIndexData()
                processUVIndexData(uvIndexDataList) // Llamar a la función aquí
            } catch (e: Exception) {
                Log.e("UVIndexService", "Error al obtener datos: ${e.message}")
            }
        }
    }

    private fun processUVIndexData(uvIndexDataList: List<UVIndexData>) {
        Log.d("UVIndexService", "Procesando ${uvIndexDataList.size} datos UV")
        val currentTime = System.currentTimeMillis()
        val relevantData = uvIndexDataList.filter {
            val dataTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(it.timestamp)?.time ?: 0
            val uvIndex = it.uvIndex // Ya es Double, no necesitamos convertirlo
            Log.d("UVIndexService", "Dato: ${it.timestamp}, UV: $uvIndex, ActivationTime: ${Date(activationTime)}")
            dataTime > activationTime && dataTime <= currentTime && uvIndex >= 6.0
        }

        val lastData = relevantData.lastOrNull()
        if (lastData != null && lastData != lastNotificationData) {
            showNotification(lastData)
            lastNotificationData = lastData
        }

        if (relevantData.isEmpty()) {
            Log.d("UVIndexService", "No se encontraron datos relevantes para notificar")
        }
    }

    private fun showNotification(data: UVIndexData) {
        // Cancelar la notificación anterior
        notificationManager.cancel(NOTIFICATION_ID_DATA)

        // Crear la nueva notificación
        val currentData = lastNotificationData
        if (currentData != null && currentData != data) {
            // Crear la nueva notificación
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alerta de índice UV")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("El índice UV es de ${data.uvIndex}\n Tome las siguientes precauciones, no olvide de " +
                            "usar protector solar y gorra para cubrirse de la radiacion UV."))
                .setSmallIcon(R.drawable.uv_alto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(NOTIFICATION_ID_DATA, notification)
            currentNotification = notification // Mantener la notificación en memoria
            Log.d("UVIndexService", "Notificación enviada para: ${data.timestamp}, UV: ${data.uvIndex}")
        }
    }

    private fun createNotificationPermanent(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UV Index Service")
            .setContentText("El servicio de índice UV está funcionando en segundo plano")
            .setSmallIcon(R.drawable.uv_alto)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Set priority to maximum
            .setOngoing(true) // Make the notification ongoing
            .build()
        return notification
    }

    private fun createNotificationPermanentWithError(errorMessage: String): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UV Index Service")
            .setContentText(errorMessage)
            .setSmallIcon(R.drawable.uv_alto)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "UV Index Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        wakeLock.release()
        stopSelf()
        unregisterReceiver(networkChangeReceiver)
    }

    companion object {
        const val CHANNEL_ID = "uv_index_channel"
    }
}






