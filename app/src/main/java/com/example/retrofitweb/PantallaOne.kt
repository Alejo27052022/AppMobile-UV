package com.example.retrofitweb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class PantallaOne : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_inicio_one)

        val pantalla_two: ConstraintLayout = findViewById(R.id.layout_pantalla_one)
        pantalla_two.setOnClickListener {
            Preferencias.setPrimeraVez(this, false)
            val intent: Intent = Intent(this, PantallaTwo::class.java)
            startActivity(intent)
        }

    }
}