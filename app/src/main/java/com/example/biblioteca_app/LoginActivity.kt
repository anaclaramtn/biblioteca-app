package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        auth = FirebaseAuth.getInstance()

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
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // LOGIN FIREBASE
            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Login realizado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, TelaHomeActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            "E-mail ou senha inválidos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}