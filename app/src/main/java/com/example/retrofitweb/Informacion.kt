package com.example.retrofitweb

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapp.BaseActivity
import com.google.android.material.navigation.NavigationView

class Informacion : BaseActivity () {

    val url_web_espoch = "https://www.espoch.edu.ec/";
    val url_web_gitea = "http://gitea.espoch.edu.ec:8085/";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.informacion, findViewById(R.id.content_frame))

        val inicio_var: ImageView = findViewById(R.id.icon_home)
        inicio_var.setOnClickListener {
            val intent: Intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }

        val web_gitea : ImageView = findViewById(R.id.web_gitea)
        web_gitea.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url_web_gitea))
            startActivity(intent)
        }

        val web_espoch : ImageView = findViewById(R.id.web_espoch)
        web_espoch.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url_web_espoch))
            startActivity(intent)
        }
    }
}