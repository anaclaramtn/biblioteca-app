package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView



class ConfiguracoesActivity : AppCompatActivity() {
    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_configuracoes)

        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> { true }
                R.id.nav_busca -> { true }
                R.id.nav_notif -> { true }
                R.id.nav_menu -> { true }
                else -> false
            }
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val btnAlterarSenha = findViewById<TextView>(R.id.btnAlterarSenha)
        val btnSobreOApp = findViewById<TextView>(R.id.btnSobreOApp)

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        btnSobreOApp.setOnClickListener {
            startActivity(Intent(this, SobreOAppActivity::class.java))
        }
    }
}