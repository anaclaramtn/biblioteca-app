package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CadastroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)

        val tvJaTemConta = findViewById<TextView>(R.id.TextViewJaTemConta)
        tvJaTemConta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnCriar = findViewById<com.google.android.material.button.MaterialButton>(R.id.ButtonCriarConta2)

        val nome = findViewById<EditText>(R.id.EditTextNomeCompleto)
        val email = findViewById<EditText>(R.id.EditTextEmailCadastro)
        val senha = findViewById<EditText>(R.id.EditTextSenhaCadastro)
        val confirmarSenha = findViewById<EditText>(R.id.EditTextConfirmarSenhaCadastro)

        btnCriar.setOnClickListener {
            val nomeStr = nome.text.toString()
            val emailStr = email.text.toString()
            val senhaStr = senha.text.toString()
            val confirmarStr = confirmarSenha.text.toString()

            if (nomeStr.isEmpty() || emailStr.isEmpty() || senhaStr.isEmpty() || confirmarStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senhaStr != confirmarStr) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!senhaValida(senhaStr)) {
                Toast.makeText(this, "A senha não atende aos requisitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //lembrar de perguntar sobre a senha, sobre como devemos fazer
            Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun senhaValida(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temMinuscula = senha.any { it.isLowerCase() }
        val temMaiuscula = senha.any { it.isUpperCase() }

        return temNumero && temMinuscula && temMaiuscula
    }
}

