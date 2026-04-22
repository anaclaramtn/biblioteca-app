package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AlteracaoDeSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_alteracao_de_senha)

        // 1. Referenciar os componentes do XML com os IDs padronizados
        val btnVoltar = findViewById<ImageView>(R.id.ButtonVoltar)
        val btnConcluir = findViewById<MaterialButton>(R.id.ButtonConcluir)
        val btnCancelar = findViewById<MaterialButton>(R.id.ButtonCancelar)

        val editSenhaAtual = findViewById<EditText>(R.id.EditTextSenhaAtual)
        val editSenhaNova = findViewById<EditText>(R.id.EditTextNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.EditTextConfirmarNovaSenha)

        // Botão Voltar e Cancelar (fecham a tela)
        btnVoltar.setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }

        // 2. Lógica do botão Concluir
        btnConcluir.setOnClickListener {
            val senhaAtualStr = editSenhaAtual.text.toString()
            val senhaNovaStr = editSenhaNova.text.toString()
            val confirmarStr = editConfirmarSenha.text.toString()

            // Validação de campos vazios
            if (senhaAtualStr.isEmpty() || senhaNovaStr.isEmpty() || confirmarStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação de igualdade
            if (senhaNovaStr != confirmarStr) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação de requisitos
            if (!senhaValida(senhaNovaStr)) {
                Toast.makeText(this, "A nova senha não cumpre os requisitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sucesso
            Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun senhaValida(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temMinuscula = senha.any { it.isLowerCase() }
        val temMaiuscula = senha.any { it.isUpperCase() }

        return temNumero && temMinuscula && temMaiuscula
    }
}