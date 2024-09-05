package com.example.retrofitweb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class Portada : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.portada)

        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        val fadeInAnimation = AlphaAnimation(0f, 1f)
        fadeInAnimation.duration = 500
        progressBar.startAnimation(fadeInAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOutAnimation = AlphaAnimation(1f, 0f)
            fadeOutAnimation.duration = 500
            progressBar.startAnimation(fadeOutAnimation)
            progressBar.visibility = View.GONE

            if (Preferencias.esPrimeraVez(this)) {
                // Mostrar las pantallas de introducción
                val intent = Intent(this, PantallaOne::class.java)
                startActivity(intent)
            } else {
                // Redireccionar directamente a la clase Inicio
                val intent = Intent(this, Inicio::class.java)
                startActivity(intent)
            }
            finish()
        }, 2000)
    }
}