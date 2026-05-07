package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class TelaRedefinicaoSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_redefinicao_senha)

        val btnVoltar = findViewById<ImageButton>(R.id.ButtonVoltar)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val emailRedefSenha = findViewById<EditText>(R.id.TextInputEditTextEmail)
        val btnRedefinicao = findViewById<MaterialButton>(R.id.ButtonRedefinicao)

        btnRedefinicao.setOnClickListener {
            val email = emailRedefSenha.text.toString()
            if (email == "narakao@gmail.com") {
                Toast.makeText(
                    this,
                    "O codigo foi enviado!\nFavor, verificar a caixa do email!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Seu email nao existe em nosso banco\nPara prosseguir com a recuperacao de senha,\nfavor, preencher o email correto ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
