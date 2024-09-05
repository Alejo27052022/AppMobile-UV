package com.example.retrofitweb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class PantallaTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_inicio_two)

        val pantalla_two: ConstraintLayout = findViewById(R.id.pantalla_inicio_two)
        pantalla_two.setOnClickListener {
            Preferencias.setPrimeraVez(this, false)
            val intent : Intent = Intent(this, PantallaThree::class.java)
            startActivity(intent)
        }

    }
}