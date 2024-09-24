package com.example.retrofitweb

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.retrofitweb.data.models.UVIndexData
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class UVIndexAdapter(private val uvIndexDataList: List<UVIndexData>) :
    RecyclerView.Adapter<UVIndexAdapter.UVIndexViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UVIndexViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_uv_index, parent, false)
        return UVIndexViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UVIndexViewHolder, position: Int) {
        val data = uvIndexDataList[position]
        holder.bind(data)
    }

    override fun getItemCount() = uvIndexDataList.size

    inner class UVIndexViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgWeatherData: ImageView = itemView.findViewById(R.id.img_weather_data)
        private val textViewDataHour: TextView = itemView.findViewById(R.id.text_view_data_hour)
        private val textViewDataUV: TextView = itemView.findViewById(R.id.text_view_data_uv)
        private val textViewDate: TextView = itemView.findViewById(R.id.text_view_date)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(data: UVIndexData) {
            val uvIndex = data.uvIndex
            val weatherImageRes = when {
                uvIndex > 6 -> R.drawable.uv_alto
                uvIndex > 4 && uvIndex <= 6 -> R.drawable.uv_medio
                else -> R.drawable.uv_bajo
            }

            val timestamp = data.timestamp
            val localDateTime = LocalDateTime.parse(timestamp)
            val hourString = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.forLanguageTag("es-ES"))
            val dateString = localDateTime.format(formatter)

            imgWeatherData.setImageResource(weatherImageRes)
            textViewDataHour.text = hourString
            textViewDate.text = dateString
            textViewDataUV.text = uvIndex.toString()

            // Define colores para cada rango de índice UV
            val textColor = when {
                uvIndex > 10 -> ContextCompat.getColor(itemView.context, R.color.color_purple)
                uvIndex > 8 -> ContextCompat.getColor(itemView.context, R.color.color_red)
                uvIndex > 6 -> ContextCompat.getColor(itemView.context, R.color.color_orange)
                uvIndex > 3 -> ContextCompat.getColor(itemView.context, R.color.color_yellow)
                else -> ContextCompat.getColor(itemView.context, R.color.color_green)
            }

            // Cambia el color del texto en los TextViews
            textViewDataUV.setTextColor(textColor)
            textViewDataUV.text = "${uvIndex} Índice UV"
        }
    }
}
