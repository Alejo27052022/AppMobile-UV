/*
    Esta clase es la actividad principal que muestra el índice UV y gestiona la interfaz de usuario relacionada
    con las notificaciones y la imagen del clima. Aquí se configuran los componentes visuales, se maneja el estado
    de las notificaciones, y se registra un receptor para actualizar los datos de UV. La lógica de actualización
    de la UI y la obtención de datos desde la API también está contenida aquí.
*/

package com.example.retrofitweb

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.BaseActivity
import com.example.retrofitweb.data.models.UVIndexData
import com.example.retrofitweb.interfaces.RetrofitBuilder
import com.example.retrofitweb.service.UVIndexService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Inicio : BaseActivity() {

    // Servicio Retrofit para hacer llamada a la API
    private val retrofitService by lazy {
        RetrofitBuilder.getRetrofitService()
    }

    private var notificationActivationTime: Long = 0

    // Componentes de la interfaz de usuario
    private lateinit var hourView: TextView
    private lateinit var textView: TextView
    private lateinit var textView2: TextView
    private lateinit var weatherImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var recyclerViewReport: RecyclerView
    private val uvIndexDataList = mutableListOf<UVIndexData>()
    private lateinit var uvIndexAdapter: UVIndexAdapter

    // Botón de notificaciones
    private var isNotificationEnabled: Boolean = false
    private lateinit var alertPreferences: SharedPreferences


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el layout y inicializar los componentes de la interfaz de usuario
        val contentFrame: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(R.layout.inicio_day, contentFrame, true)

        // Inicializar los componentes de la interfaz de usuario
        textView = contentFrame.findViewById(R.id.view_uv)
        textView2 = contentFrame.findViewById(R.id.info_uv)
        hourView = contentFrame.findViewById(R.id.view_hour)
        weatherImageView = contentFrame.findViewById(R.id.image_weather)

        // Configurar el RecyclerView
        recyclerViewReport = findViewById(R.id.recycler_view_report)
        uvIndexAdapter = UVIndexAdapter(uvIndexDataList)
        recyclerViewReport.adapter = uvIndexAdapter
        recyclerViewReport.layoutManager = LinearLayoutManager(this)

        // Obtener las preferencias de notificaciones
        alertPreferences = getSharedPreferences("alert_prefs", Context.MODE_PRIVATE)
        isNotificationEnabled = alertPreferences.getBoolean("is_notification_enabled", false)

        // Verificar si la versión de Android es O o posterior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Obtener los datos del índice UV
            getUVIndexData()
        } else {
            // Mostrar un mensaje de error si la versión de Android es anterior a O
            textView2.text =
                "Esta funcionalidad no está disponible en versiones anteriores a Android O."
            textView2.setTextColor(Color.RED)
        }

        // Actualizar la hora y la imagen del clima
        updateHourAndWeatherImage()
        // Establecer un timer para actualizar la hora y la imagen del clima cada minuto
        handler.postDelayed(updateTimeRunnable, 60000)

        val dateInput = findViewById<EditText>(R.id.date)

        dateInput.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                dateInput.setText(date)
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateHourAndWeatherImage()
            handler.postDelayed(this, 60000)
        }
    }

    private fun updateUVIndexList(uvIndexDataList: List<UVIndexData>) {
        this.uvIndexDataList.clear()
        this.uvIndexDataList.addAll(uvIndexDataList.takeLast(6))
        uvIndexAdapter.notifyDataSetChanged()
    }

    private fun updateHourAndWeatherImage(uvIndex: Float = -1.0f) {
        val currentHour = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        hourView.text = currentHour

        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        val weatherImageRes = when {
            uvIndex > 6 -> R.drawable.sol_image2
            uvIndex > 4 -> R.drawable.uv_medio
            hourOfDay in 6..11 -> R.drawable.uv_medio
            hourOfDay in 12..18 -> R.drawable.sol_image2
            else -> R.drawable.uv_bajo
        }
        weatherImageView.setImageResource(weatherImageRes)
    }

    private fun getUVIndexText(uvIndex: Float): String {
        return when {
            uvIndex in 0.0f..2.99f -> "El índice UV se encuentra en escala Baja"
            uvIndex in 3.0f..5.99f -> "El índice UV se encuentra en escala Moderado"
            uvIndex in 6.0f..7.99f -> "El índice UV se encuentra en escala Alto"
            uvIndex in 8.0f..10.99f -> "El índice UV se encuentra en escala Muy Alto"
            else -> "El índice UV se encuentra en escala Extremadamente Alto"
        }
    }

    private fun changeBackground(uvIndex: Float) {
        val inicio = findViewById<ConstraintLayout>(R.id.inicio)
        val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        when {
            uvIndex in 0.0f..2.99f -> relativeLayout.setBackgroundResource(R.drawable.fondo_degradado_bajo)
            uvIndex in 3.0f..5.99f -> relativeLayout.setBackgroundResource(R.drawable.fondo_degradado_moderado)
            uvIndex in 6.0f..7.99f -> relativeLayout.setBackgroundResource(R.drawable.fondo_degradado_alto)
            uvIndex in 8.0f..10.99f -> relativeLayout.setBackgroundResource(R.drawable.fondo_degradado_muy_alto)
            else -> relativeLayout.setBackgroundResource(R.drawable.fondo_degradado_ext_alto)
        }

        when (hourOfDay){
            in 6..11 -> inicio.setBackgroundResource(R.drawable.fondo_degradado)
            in 12..18 -> inicio.setBackgroundResource(R.drawable.fondo_degrado_tarde)
            in 19..5 -> inicio.setBackgroundResource(R.drawable.fondo_degradado_noche)
        }
    }

    private fun updateUVIndexUI(uvIndex: Float) {
        if (uvIndex == 0.0f) {
            textView.text = "0.0"
            textView.setTextColor(Color.WHITE)
            textView2.text = "No se ha capturado ningún dato"
            textView2.setTextColor(Color.WHITE)
        } else {
            textView.text = uvIndex.toString()
            textView.setTextColor(Color.WHITE)
            textView2.text = getUVIndexText(uvIndex)
            textView2.setTextColor(Color.WHITE)
        }
        changeBackground(uvIndex)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUVIndexData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uvIndexDataList = retrofitService.getUVIndexData()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val convertedDataList = uvIndexDataList.map { data ->
                    data.copy(timestamp = LocalDateTime.parse(data.timestamp, formatter).toString())
                }
                withContext(Dispatchers.Main) {
                    processUVIndexData(convertedDataList)
                    updateUVIndexList(convertedDataList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError(e)
                }
            }
        }
    }

    // Proceso de los Datos
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processUVIndexData(uvIndexDataList: List<UVIndexData>) {
        Log.d("Inicio", "Procesando datos UV: $uvIndexDataList")

        if (uvIndexDataList.isNullOrEmpty()) {
            textView2.text = "No se ha capturado ningún dato"
            textView2.setTextColor(Color.RED)
            updateHourAndWeatherImage()
            return
        }

        val currentDateTime = LocalDateTime.now()
        val startOfCurrentRange = currentDateTime.minusHours(1)
        val endOfCurrentRange = currentDateTime

        val latestDataInRange = uvIndexDataList.filter {
            val timestamp = LocalDateTime.parse(it.timestamp)
            timestamp.isAfter(startOfCurrentRange) && timestamp.isBefore(endOfCurrentRange)
        }.maxByOrNull {
            LocalDateTime.parse(it.timestamp)
        }

        if (latestDataInRange != null) {
            val maxUVIndex = latestDataInRange.uvIndex
            textView.text = "$maxUVIndex"
            Log.d("Inicio", "Índice UV más reciente en la última hora: $maxUVIndex")
            updateUVIndexUI(maxUVIndex.toFloat())
        } else {
            textView.text = "0.0"
            textView2.text = "No se ha capturado ningún dato en la última hora"
            textView.setTextColor(Color.RED)
            textView2.setTextColor(Color.RED)
            updateHourAndWeatherImage()
        }

    }

    // Manejo de Errores
    private fun handleError(e: Exception) {
        Log.e("Inicio", "Error al obtener datos de la API: $e")
        textView.text = "Error ${e.message}"
        textView.setTextColor(Color.RED)
        updateHourAndWeatherImage()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}