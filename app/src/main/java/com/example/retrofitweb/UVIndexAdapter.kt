package com.example.retrofitweb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.retrofitweb.data.models.UVIndexData

class UVIndexAdapter(private val uvIndexDataList: List<UVIndexData>) :
    RecyclerView.Adapter<UVIndexAdapter.UVIndexViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UVIndexViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_uv_index, parent, false)
        return UVIndexViewHolder(view)
    }

    override fun onBindViewHolder(holder: UVIndexViewHolder, position: Int) {
        val data = uvIndexDataList[position]
        holder.bind(data)
    }

    override fun getItemCount() = uvIndexDataList.size

    inner class UVIndexViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgWeatherData: ImageView = itemView.findViewById(R.id.img_weather_data)
        private val textViewDataHour: TextView = itemView.findViewById(R.id.text_view_data_hour)
        private val textViewDataUV: TextView = itemView.findViewById(R.id.text_view_data_uv)

        fun bind(data: UVIndexData) {
            val uvIndex = data.uvIndex
            val weatherImageRes = when {
                uvIndex > 6 -> R.drawable.uv_alto
                uvIndex > 4 && uvIndex <= 6 -> R.drawable.uv_medio
                else -> R.drawable.uv_bajo
            }

            imgWeatherData.setImageResource(weatherImageRes)
            textViewDataHour.text = data.timestamp // Assumes timestamp is formatted correctly
            textViewDataUV.text = uvIndex.toString()

            // Define colores para cada rango de índice UV
            val (backgroundColor, textColor) = when {
                uvIndex > 10 -> Pair(ContextCompat.getColor(itemView.context, R.color.color_purple), ContextCompat.getColor(itemView.context, R.color.white))
                uvIndex > 8 -> Pair(ContextCompat.getColor(itemView.context, R.color.color_red), ContextCompat.getColor(itemView.context, R.color.black))
                uvIndex > 6 -> Pair(ContextCompat.getColor(itemView.context, R.color.color_orange), ContextCompat.getColor(itemView.context, R.color.black))
                uvIndex > 4 -> Pair(ContextCompat.getColor(itemView.context, R.color.color_yellow), ContextCompat.getColor(itemView.context, R.color.black))
                else -> Pair(ContextCompat.getColor(itemView.context, R.color.color_green), ContextCompat.getColor(itemView.context, R.color.black))
            }

            // Aplica el color de fondo al contenedor del ítem
            itemView.setBackgroundColor(backgroundColor)

            // Cambia el color del texto en los TextViews
            textViewDataUV.setTextColor(textColor)
            textViewDataHour.setTextColor(textColor)
            textViewDataUV.text = "${uvIndex} Índice UV"
        }
    }
}
