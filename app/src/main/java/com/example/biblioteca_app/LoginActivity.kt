package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

// como uma nova activity deve ser criada assim que adicionamos uma tela
//res > new > empty views activity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        val btnCriarConta = findViewById<MaterialButton>(R.id.ButtonCriarConta1)
        val btnEsqueceuSenha = findViewById<MaterialButton>(R.id.ButtonEsqueceuSenha)

        btnCriarConta.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        btnEsqueceuSenha.setOnClickListener {
            val intent = Intent(this, TelaRedefinicaoSenhaActivity::class.java)
            startActivity(intent)
        }
    }
}