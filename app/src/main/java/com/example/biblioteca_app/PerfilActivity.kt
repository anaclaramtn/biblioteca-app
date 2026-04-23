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
            val intent = Intent(this, ConfiguracoesActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar livros curtidos (carrossel)
        setupLivrosCurtidos()
    }

    private fun setupLivrosCurtidos() {
        // Livro 1
        val livro1 = findViewById<View>(R.id.livro1)
        livro1.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.war)
        livro1.findViewById<TextView>(R.id.txtTituloLivro).text = "Star Wars"
        livro1.findViewById<TextView>(R.id.txtAutor).text = "George Lucas"

        // Livro 2
        val livro2 = findViewById<View>(R.id.livro2)
        livro2.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.uno)
        livro2.findViewById<TextView>(R.id.txtTituloLivro).text = "UNO"
        livro2.findViewById<TextView>(R.id.txtAutor).text = "Mattel"

        // Livro 3
        val livro3 = findViewById<View>(R.id.livro3)
        livro3.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.capadomquixote)
        livro3.findViewById<TextView>(R.id.txtTituloLivro).text = "Dom Quixote"
        livro3.findViewById<TextView>(R.id.txtAutor).text = "Miguel de Cervantes"

        // Livro 4
        val livro4 = findViewById<View>(R.id.livro4)
        livro4.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.logo)
        livro4.findViewById<TextView>(R.id.txtTituloLivro).text = "Biblioteca"
        livro4.findViewById<TextView>(R.id.txtAutor).text = "Unifor"
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
}