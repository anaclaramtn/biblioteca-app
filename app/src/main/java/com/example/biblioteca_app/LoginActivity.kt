package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            // Verifica se o usuário é administrador no Firestore
                            db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener { document ->
                                    val isAdmin = document.getBoolean("isAdmin") ?: false
                                    
                                    Toast.makeText(
                                        this,
                                        "Login realizado com sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Redireciona para AdminHomeActivity se for admin, senão TelaHomeActivity
                                    val destination = if (isAdmin) {
                                        AdminHomeActivity::class.java
                                    } else {
                                        TelaHomeActivity::class.java
                                    }
                                    
                                    startActivity(Intent(this, destination))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao recuperar dados do perfil", Toast.LENGTH_SHORT).show()
                                }
                        }
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