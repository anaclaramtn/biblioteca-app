package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class TelaRedefinicaoSenhaActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_redefinicao_senha)

        auth = FirebaseAuth.getInstance()

        val btnVoltar = findViewById<ImageButton>(R.id.ButtonVoltar)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val emailRedefSenha = findViewById<EditText>(R.id.TextInputEditTextEmail)
        val btnRedefinicao = findViewById<MaterialButton>(R.id.ButtonRedefinicao)

        btnRedefinicao.setOnClickListener {

            val email = emailRedefSenha.text.toString().trim()

            // RF03.3
            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_LONG
                ).show()

                // RF03.4
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                Toast.makeText(
                    this,
                    "Digite um email que siga o padrão",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                // RF03.5
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                this,
                                "O link foi enviado! Verifique seu email.",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {

                            Toast.makeText(
                                this,
                                "Seu email não existe em nosso banco",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


            }
        }
    }
}

