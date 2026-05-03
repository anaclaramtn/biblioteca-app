package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_menu)

        val btnSair = findViewById<TextView>(R.id.btnSair)
        val btnConfiguracoes = findViewById<TextView>(R.id.btnConfiguracoes)

        btnConfiguracoes.setOnClickListener {
            startActivity(Intent(this, AdminConfiguracoesActivity::class.java))
        }

        btnSair.setOnClickListener {
            mostrarDialogoSaida()
        }

        setupNavBar()
    }

    private fun mostrarDialogoSaida() {
        AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_menu

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
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_menu -> true

                else -> false
            }
        }
    }
}