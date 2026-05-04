package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminConfiguracoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_configuracoes)

        // 🔹 HEADER (mesmo padrão das outras telas admin)
        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.txtTitulo)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)

        titulo.text = "Configurações"

        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            startActivity(Intent(this, AdminMenuActivity::class.java))
            finish()
        }

        // 🔹 BOTÕES
        val btnAlterarSenha = findViewById<TextView>(R.id.btnAlterarSenha)
        val btnSobreOApp = findViewById<TextView>(R.id.btnSobreOApp)

        btnAlterarSenha.setOnClickListener {
            startActivity(Intent(this, AdminAlteracaoSenhaActivity::class.java))
        }

        btnSobreOApp.setOnClickListener {
            startActivity(Intent(this, AdminSobreOAppActivity::class.java))
        }

        setupNavBar()
    }

    // PADRÃO ADMIN (igual Home/AdminMenu)
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuarioadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_notif -> {
                    startActivity(Intent(this, AdminNotificacoesActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_menu -> {
                    startActivity(Intent(this, AdminMenuActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}