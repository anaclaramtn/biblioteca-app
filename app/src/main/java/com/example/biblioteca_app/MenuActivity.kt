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

        var btnConfiguracoes = findViewById<TextView>(R.id.btnConfiguracoes)
        var btnPesquisaCientifica = findViewById<TextView>(R.id.btnPesquisa)
        val btnPerfil = findViewById<TextView>(R.id.btnPerfil)
        val btnHistorico = findViewById<TextView>(R.id.btnHistorico)
        val btnLudoteca = findViewById<TextView>(R.id.btnLudoteca)

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }

        btnLudoteca.setOnClickListener {
            startActivity(Intent(this, LudotecaActivity::class.java))
        }

        btnConfiguracoes.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }
        btnPesquisaCientifica.setOnClickListener {
            startActivity(Intent(this, PesquisaCientificaActivity::class.java))
        }
        setupNavBar()
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