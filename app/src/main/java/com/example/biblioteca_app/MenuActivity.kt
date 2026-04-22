package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView


class MenuActivity : AppCompatActivity() {
    lateinit var btnSair: TextView
    lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_menu)

        btnSair = findViewById<TextView>(R.id.btnSair)

        btnSair.setOnClickListener {
            mostrarDialogoSaida()
        }

        bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> {
                    // abrir tela home
                    true
                }
                R.id.nav_busca -> {
                    true
                }
                R.id.nav_notif -> {
                    true
                }
                R.id.nav_menu -> {
                    true
                }
                else -> false
            }
        }

        var btnConfiguracoes = findViewById<TextView>(R.id.btnConfiguracoes)

        btnConfiguracoes.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
            finish()
        }
    }

    private fun mostrarDialogoSaida() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Atenção")
        builder.setMessage("Tem certeza que deseja sair?")

        builder.setPositiveButton("Sim") { _, _ ->
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("Não", null)

        builder.show()
    }


}