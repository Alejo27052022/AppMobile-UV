package com.example.retrofitweb

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.PanZoom
import com.androidplot.xy.PointLabelFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.StepMode
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import com.example.myapp.BaseActivity
import com.example.retrofitweb.data.models.UVIndexData
import com.example.retrofitweb.interfaces.RetrofitBuilder
import kotlinx.coroutines.launch
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Grafico : BaseActivity() {

    // Servicio Retrofit para hacer llamada a la API
    private val retrofitService by lazy {
        RetrofitBuilder.getRetrofitService()
    }

    private lateinit var xyPlot: XYPlot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentFrame: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(R.layout.pantalla_grafico, contentFrame, true)

        // Inicializar el gráfico
        xyPlot = findViewById(R.id.xyplot_graf)

        // Llamada a la API para obtener los datos
        obtenerDatosUV()
    }

    private fun obtenerDatosUV() {
        lifecycleScope.launch {
            try {
                val uvIndexDataList = retrofitService.getUVIndexData()
                if (uvIndexDataList.isNotEmpty()) {
                    // Obtener la fecha de la primera entrada
                    val fechaCapturada = uvIndexDataList.first().timestamp

                    // Convertir la fecha al formato deseado
                    val inputFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = inputFormatter.parse(fechaCapturada)

                    val outputFormatter = SimpleDateFormat("EEEE d 'de' MMMM 'del' yyyy", Locale("es", "ES"))
                    val fechaFormateada = date?.let { outputFormatter.format(it) } ?: "Fecha no disponible"

                    // Actualizar el TextView con la fecha formateada
                    val tvFechaCapturada: TextView = findViewById(R.id.txt_date)
                    tvFechaCapturada.text = "$fechaFormateada"

                    // Crear gráfico si hay datos disponibles
                    createGraph(uvIndexDataList)
                } else {
                    Log.d("Gráfico", "No hay datos de UV disponibles.")
                }
            } catch (e: Exception) {
                Log.e("Gráfico", "Error al obtener los datos de UV: ${e.message}")
            }
        }
    }



    private fun createGraph(uvIndexDataList: List<UVIndexData>) {
        val xyPlot = findViewById<XYPlot>(R.id.xyplot_graf)

        // Limpiar el gráfico antes de agregar la serie
        xyPlot.clear()

        // Crear una lista de valores x (en milisegundos) e y (Índice UV)
        val xValues = uvIndexDataList.map {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = formatter.parse(it.timestamp)
            date?.time?.toDouble() ?: 0.0
        }

        val yValues = uvIndexDataList.map { it.uvIndex.toDouble() }

        // Verificación para evitar que el gráfico falle si no hay datos válidos
        if (xValues.isEmpty() || yValues.isEmpty()) {
            Log.d("Gráfico", "No se pueden generar datos de la gráfica.")
            return
        }

        // Crear un objeto SimpleXYSeries
        val series = SimpleXYSeries(
            xValues,
            yValues,
            "Índice UV"
        )

        // Agregar la serie al XYPlot con un formateador de línea
        val formatter = LineAndPointFormatter(Color.RED, null, null, PointLabelFormatter(Color.BLACK))
        xyPlot.addSeries(series, formatter)

        // Obtener el valor mínimo y máximo de X y Y
        val minX = xValues.minOrNull() ?: 0.0
        val maxX = xValues.maxOrNull() ?: 100.0
        val minY = yValues.minOrNull() ?: 0.0
        val maxY = yValues.maxOrNull() ?: 15.0  // Máximo valor del índice UV

        // Agregar un padding para que los datos no queden pegados al borde
        val paddingY = (maxY - minY) * 0.10 // 10% de margen en el eje Y
        val paddingX = (maxX - minX) * 0.05 // 5% de margen en el eje X (opcional)

        // Establecer el rango del gráfico con padding
        xyPlot.setRangeBoundaries(minY - paddingY, BoundaryMode.FIXED, maxY + paddingY, BoundaryMode.FIXED)

        // Establecer el dominio del gráfico con padding (eje X)
        xyPlot.setDomainBoundaries(minX - paddingX, BoundaryMode.FIXED, maxX + paddingX, BoundaryMode.FIXED)

        // Configurar el espaciado entre puntos en el eje X
        xyPlot.setDomainStep(StepMode.SUBDIVIDE, 10.0)

        // Habilitar zoom y desplazamiento (pan)
        PanZoom.attach(xyPlot).apply {
            setZoom(PanZoom.Zoom.STRETCH_HORIZONTAL)
        }

        // Agregar padding gráfico
        xyPlot.setPlotMargins(0F, 0F, 0F, 50F)

        // Personalizar las etiquetas del eje X para mostrar solo la hora
        val hourFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        xyPlot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
            override fun format(source: Any, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
                val date = Date((source as Number).toLong())
                return toAppendTo.append(hourFormatter.format(date))
            }

            override fun parseObject(source: String, pos: ParsePosition): Any? {
                return null // No necesitamos implementar el parsing inverso en este caso
            }
        }

        // Limitar el desplazamiento solo al eje horizontal
        PanZoom.attach(xyPlot, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.STRETCH_HORIZONTAL)

        // Redibujar el gráfico
        xyPlot.redraw()
    }
}