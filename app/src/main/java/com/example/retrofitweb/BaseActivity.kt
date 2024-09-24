package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.retrofitweb.GenerarAlerta
import com.example.retrofitweb.Grafico
import com.example.retrofitweb.Inicio
import com.example.retrofitweb.PantallaAcerca
import com.example.retrofitweb.R
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawer: DrawerLayout
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        drawer = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open, R.string.nav_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_one -> {
                val intent = Intent(this, Inicio::class.java)
                startActivity(intent)
            }
            R.id.nav_item_three -> {
                val intent = Intent(this, PantallaAcerca::class.java)
                startActivity(intent)
            }
            R.id.nav_item_alerta -> {
                val intent = Intent(this, GenerarAlerta::class.java)
                startActivity(intent)
            }
            R.id.nav_item_grafico -> {
                val intent = Intent(this, Grafico::class.java)
                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
