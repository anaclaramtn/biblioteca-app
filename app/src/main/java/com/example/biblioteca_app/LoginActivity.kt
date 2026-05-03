package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        val btnCriarConta = findViewById<MaterialButton>(R.id.ButtonCriarConta1)
        val btnEsqueceuSenha = findViewById<MaterialButton>(R.id.ButtonEsqueceuSenha)
        val btnEntrar = findViewById<MaterialButton>(R.id.ButtonEntrar)

        val editEmail = findViewById<EditText>(R.id.EditTextEmailLogin)
        val editSenha = findViewById<EditText>(R.id.EditTextSenhaLogin)

        btnCriarConta.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        btnEsqueceuSenha.setOnClickListener {
            val intent = Intent(this, TelaRedefinicaoSenhaActivity::class.java)
            startActivity(intent)
        }

        btnEntrar.setOnClickListener {

            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            // Verifica campos vazios
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(
                    this,
                    "Preencha todos os campos\nPara prosseguir com o login, favor, preencher todos os campos",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Login USER
            if (email == "User" && senha == "123") {
                val intent = Intent(this, TelaHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Login ADMIN
            else if (email == "Admin" && senha == "321") {
                val intent = Intent(this, AdminHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Login inválido
            else {
                Toast.makeText(
                    this,
                    "Login inválido\nVerifique suas credenciais",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}