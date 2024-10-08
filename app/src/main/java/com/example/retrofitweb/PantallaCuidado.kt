package com.example.retrofitweb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.BaseActivity

class PantallaCuidado : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.pantalla_cuidado, findViewById(R.id.content_frame))
    }
}