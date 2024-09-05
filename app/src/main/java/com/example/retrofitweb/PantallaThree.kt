package com.example.retrofitweb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class PantallaThree : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_inicio_three)

        val pantalla_three : ConstraintLayout = findViewById(R.id.layout_pantalla_three)
        pantalla_three.setOnClickListener {
            Preferencias.setPrimeraVez(this, false)
            val intent : Intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }
    }
}