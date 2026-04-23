package com.example.biblioteca_app

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NoticiaCompletaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_noticia_completa)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltarNoticia)
        val imgNoticia = findViewById<ImageView>(R.id.imgNoticiaCompleta)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloNoticiaCompleta)
        val txtConteudo = findViewById<TextView>(R.id.txtConteudoNoticia)

        // Receber dados da Intent
        val titulo = intent.getStringExtra("TITULO") ?: "Título da Notícia"
        val imagemRes = intent.getIntExtra("IMAGEM", R.drawable.logo)
        
        txtTitulo.text = titulo
        imgNoticia.setImageResource(imagemRes)

        txtConteudo.text = """
            Esse evento da biblioteca da Unifor é um evento bem interessante e muito legal. Vale a pena ir pra ele e ele vai ser massa. Recomendo que todos vão pra ver o que vai ter lá.
            
            Ademais, esse evento vai conter livros. Até por estarmos falando de uma biblioteca. Diga-se de passagem que os livros valem a pena de serem adequadamente lidos. Esse evento vai ter um sorteio de um livro muito massa e autografado pelo renomado autor Ygor Costa. Recomendo bastante!
        """.trimIndent()

        btnVoltar.setOnClickListener {
            finish()
        }
    }
}