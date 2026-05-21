package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AlteracaoDeSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_alteracao_de_senha)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()

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
            val senhaAtualStr = editSenhaAtual.text.toString().trim()
            val senhaNovaStr = editSenhaNova.text.toString().trim()
            val confirmarStr = editConfirmarSenha.text.toString().trim()

            // Validação de campos vazios (Padrão CadastroActivity)
            if (senhaAtualStr.isEmpty() || senhaNovaStr.isEmpty() || confirmarStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação de igualdade (Padrão CadastroActivity)
            if (senhaNovaStr != confirmarStr) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação de requisitos (Padrão CadastroActivity)
            if (!senhaValida(senhaNovaStr)) {
                Toast.makeText(this, "Digite uma senha que cumpra os requisitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioAtual = auth.currentUser
            if (usuarioAtual != null && usuarioAtual.email != null) {
                // Para trocar a senha, o Firebase exige reautenticação por segurança
                val credencial = EmailAuthProvider.getCredential(usuarioAtual.email!!, senhaAtualStr)

                usuarioAtual.reauthenticate(credencial).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Se a senha atual estiver correta, atualiza para a nova
                        usuarioAtual.updatePassword(senhaNovaStr).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Erro: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Sua senha atual não é essa.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            }
        }
        setupNavBar()
    }

    private fun senhaValida(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temMinuscula = senha.any { it.isLowerCase() }
        val temMaiuscula = senha.any { it.isUpperCase() }

        return temNumero && temMinuscula && temMaiuscula
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_menu

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_busca -> {
                    startActivity(Intent(this, BuscaActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
