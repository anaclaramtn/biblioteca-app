package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class AdminAlteracaoSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_alteracaosenha)

        // 🔹 HEADER PADRÃO
        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.txtTitulo)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)

        titulo.text = "Configurações"

        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            finish() // 🔥 padrão correto (não recria tela anterior)
        }

        // 🔹 CAMPOS
        val editSenhaAtual = findViewById<EditText>(R.id.EditTextSenhaAtual)
        val editSenhaNova = findViewById<EditText>(R.id.EditTextNovaSenha)
        val editConfirmar = findViewById<EditText>(R.id.EditTextConfirmarNovaSenha)

        val btnCancelar = findViewById<MaterialButton>(R.id.ButtonCancelar)
        val btnConcluir = findViewById<MaterialButton>(R.id.ButtonConcluir)

        btnCancelar.setOnClickListener { finish() }

        btnConcluir.setOnClickListener {
            val atual = editSenhaAtual.text.toString()
            val nova = editSenhaNova.text.toString()
            val confirmar = editConfirmar.text.toString()

            if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nova != confirmar) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!senhaValida(nova)) {
                Toast.makeText(this, "Senha inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupNavBar()
    }

    private fun senhaValida(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temMinuscula = senha.any { it.isLowerCase() }
        val temMaiuscula = senha.any { it.isUpperCase() }

        return temNumero && temMinuscula && temMaiuscula
    }

    // 🔥 PADRÃO IGUAL ÀS OUTRAS TELAS ADMIN
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuarioadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_notif -> {
                    startActivity(Intent(this, AdminNotificacoesActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_menu -> {
                    true
                }

                else -> false
            }
        }
    }
}