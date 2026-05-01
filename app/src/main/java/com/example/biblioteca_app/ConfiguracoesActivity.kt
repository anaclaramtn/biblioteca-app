package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConfiguracoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_configuracoes)

        // 🔹 HEADER
        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.txtTitulo)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)

        titulo.text = "Configurações"

        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish() // 🔥 agora volta corretamente sem recriar tela
        }

        // 🔹 BOTÕES
        val btnAlterarSenha = findViewById<TextView>(R.id.btnAlterarSenha)
        val btnSobreOApp = findViewById<TextView>(R.id.btnSobreOApp)

        btnSobreOApp.setOnClickListener {
            startActivity(Intent(this, SobreOAppActivity::class.java))
        }

        setupNavBar()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_menu

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_busca -> {
                    startActivity(Intent(this, BuscaActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}