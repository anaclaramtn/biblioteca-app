package com.example.biblioteca_app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class LudotecaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_ludoteca)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val tabJogos = findViewById<TextView>(R.id.tabJogos)
        val tabSalas = findViewById<TextView>(R.id.tabSalas)
        val containerJogos = findViewById<ScrollView>(R.id.containerJogos)
        val containerSalas = findViewById<ScrollView>(R.id.containerSalas)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        tabJogos.setOnClickListener {
            // Estilo ativo para Jogos
            tabJogos.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabJogos.setTextColor(Color.parseColor("#002D5E"))
            
            // Estilo inativo para Salas
            tabSalas.setBackgroundColor(Color.WHITE)
            tabSalas.setTextColor(Color.BLACK)

            containerJogos.visibility = View.VISIBLE
            containerSalas.visibility = View.GONE
        }

        tabSalas.setOnClickListener {
            // Estilo ativo para Salas
            tabSalas.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabSalas.setTextColor(Color.parseColor("#002D5E"))
            
            // Estilo inativo para Jogos
            tabJogos.setBackgroundColor(Color.WHITE)
            tabJogos.setTextColor(Color.BLACK)

            containerJogos.visibility = View.GONE
            containerSalas.visibility = View.VISIBLE
        }
        setupNavBar()
    }

    fun onAlugarClick(view: View) {
        atualizarBotaoSolicitacao(view as Button)
    }

    fun onReservarClick(view: View) {
        atualizarBotaoSolicitacao(view as Button)
        Toast.makeText(this, "solicitacao enviada", Toast.LENGTH_SHORT).show()
    }

    private fun atualizarBotaoSolicitacao(botao: Button) {
        botao.text = "Solicitação Enviada"
        botao.isEnabled = false
        // Definindo a cor cinza para o botão desabilitado (conforme a imagem)
        botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        botao.setTextColor(Color.WHITE)
        // Removendo a borda (stroke) para ficar igual ao retângulo da imagem
        if (botao is com.google.android.material.button.MaterialButton) {
            botao.strokeWidth = 0
        }
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