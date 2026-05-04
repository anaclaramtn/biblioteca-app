package com.example.biblioteca_app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar botão voltar
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar livros curtidos (carrossel)
        setupLivrosCurtidos()

        // Configurar clique no livro alugado (Título e Capa)
        val cliqueLivroAlugado = View.OnClickListener {
            abrirLivro()
        }
        findViewById<TextView>(R.id.tvTituloLivro).setOnClickListener(cliqueLivroAlugado)
        findViewById<ImageView>(R.id.ivCapaLivro).setOnClickListener(cliqueLivroAlugado)

        setupNavBar()
    }

    private fun setupLivrosCurtidos() {
        // Livro 1
        configurarItemLivro(R.id.livro1, R.drawable.capa_star_wars, "Star Wars", "George Lucas")
        // Livro 2
        configurarItemLivro(R.id.livro2, R.drawable.frankstein, "Frankstein", "Mary Shelley")
        // Livro 3
        configurarItemLivro(R.id.livro3, R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
        // Livro 4
        configurarItemLivro(R.id.livro4, R.drawable.logo, "Biblioteca", "Unifor")
    }

    private fun configurarItemLivro(id: Int, imagem: Int, titulo: String, autor: String) {
        val itemView = findViewById<View>(id)
        itemView.findViewById<ImageView>(R.id.imgCapa).setImageResource(imagem)
        itemView.findViewById<TextView>(R.id.txtTituloLivro).text = titulo
        itemView.findViewById<TextView>(R.id.txtAutor).text = autor
        itemView.setOnClickListener { abrirLivro() }
    }

    private fun abrirLivro() {
        val intent = Intent(this, LivroActivity::class.java)
        startActivity(intent)
    }

    fun onDevolverClick(view: View) {
        val botao = view as Button
        botao.text = "Solicitação Enviada"
        botao.isEnabled = false
        // Estilo acinzentado conforme LudotecaActivity
        botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        botao.setTextColor(Color.WHITE)
        
        if (botao is com.google.android.material.button.MaterialButton) {
            botao.strokeWidth = 0
        }

        Toast.makeText(this, "solicitacao enviada", Toast.LENGTH_SHORT).show()
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