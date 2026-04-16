package com.example.biblioteca_app

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class TelaRedefinicaoSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_redefinicao_senha)

        val btnVoltar = findViewById<ImageButton>(R.id.ButtonVoltar)

        btnVoltar.setOnClickListener {
            finish() // Volta para a tela anterior (Login)
        }
    }
}