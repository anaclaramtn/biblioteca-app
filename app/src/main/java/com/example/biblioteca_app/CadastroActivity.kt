package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvJaTemConta = findViewById<TextView>(R.id.TextViewJaTemConta)

        tvJaTemConta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnCriar = findViewById<com.google.android.material.button.MaterialButton>(
            R.id.ButtonCriarConta2
        )

        val nome = findViewById<EditText>(R.id.EditTextNomeCompleto)
        val email = findViewById<EditText>(R.id.EditTextEmailCadastro)
        val senha = findViewById<EditText>(R.id.EditTextSenhaCadastro)
        val confirmarSenha = findViewById<EditText>(R.id.EditTextConfirmarSenhaCadastro)

        btnCriar.setOnClickListener {

            val nomeStr = nome.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val senhaStr = senha.text.toString().trim()
            val confirmarStr = confirmarSenha.text.toString().trim()

            // Verifica campos vazios
            if (nomeStr.isEmpty() || emailStr.isEmpty()
                || senhaStr.isEmpty() || confirmarStr.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos para continuar",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Verifica email
            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailStr)
                    .matches()
            ) {

                Toast.makeText(
                    this,
                    "E-mail inválido",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Verifica senhas
            if (senhaStr != confirmarStr) {

                Toast.makeText(
                    this,
                    "As senhas não coincidem",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Verifica requisitos da senha
            if (!senhaValida(senhaStr)) {

                Toast.makeText(
                    this,
                    "Digite uma senha que cumpra os requisitos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Cria usuário no Authentication
            auth.createUserWithEmailAndPassword(emailStr, senhaStr)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        // Pega usuário criado
                        val usuarioAtual = auth.currentUser

                        // Cria objeto com os dados
                        val dadosUsuario = hashMapOf(
                            "nome" to nomeStr,
                            "email" to emailStr,
                            "uid" to usuarioAtual?.uid
                        )

                        // Salva no Firestore
                        db.collection("usuarios")
                            .document(usuarioAtual!!.uid)
                            .set(dadosUsuario)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Cadastro realizado com sucesso",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startActivity(
                                    Intent(
                                        this,
                                        LoginActivity::class.java
                                    )
                                )

                                finish()
                            }

                            .addOnFailureListener {

                                Toast.makeText(
                                    this,
                                    "Erro ao salvar dados",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    } else {

                        Toast.makeText(
                            this,
                            "Erro: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    fun senhaValida(senha: String): Boolean {

        val temNumero = senha.any { it.isDigit() }
        val temMinuscula = senha.any { it.isLowerCase() }
        val temMaiuscula = senha.any { it.isUpperCase() }

        return temNumero && temMinuscula && temMaiuscula
    }
}