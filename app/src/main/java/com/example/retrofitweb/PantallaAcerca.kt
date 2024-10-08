package com.example.retrofitweb

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import com.example.myapp.BaseActivity

class PantallaAcerca : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.pantalla_acerca, findViewById(R.id.content_frame))
    }
}