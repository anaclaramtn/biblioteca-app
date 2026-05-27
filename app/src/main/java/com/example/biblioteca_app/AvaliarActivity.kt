package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.DialogConfirmacaoCancelarBinding
import com.example.biblioteca_app.databinding.TelaAvaliarBinding
import com.example.biblioteca_app.models.Avaliacao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AvaliarActivity : AppCompatActivity() {

    private lateinit var binding: TelaAvaliarBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var idLivro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaAvaliarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtém o idLivro passado pela Intent
        idLivro = intent.getStringExtra("ID_LIVRO")

        configurarHeader()
        configurarBotoes()
        setupNavBar()
    }

    private fun configurarHeader() {
        val header = binding.header
        header.txtTitulo.text = "Nova Avaliação"
        header.btnBack.visibility = android.view.View.VISIBLE
        header.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun configurarBotoes() {
        binding.btnConfirmar.setOnClickListener {
            val nota = binding.ratingBarAvaliacao.rating
            val titulo = binding.edtTituloAvaliacao.text.toString()
            val comentario = binding.edtComentarioAvaliacao.text.toString()
            val usuario = auth.currentUser

            if (usuario == null) {
                Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nota == 0f) {
                Toast.makeText(this, "Dê uma nota", Toast.LENGTH_SHORT).show()
            } else if (titulo.isEmpty() || comentario.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                salvarAvaliacaoNoBanco(nota, titulo, comentario, usuario.uid)
            }
        }

        binding.btnCancelar.setOnClickListener {
            mostrarConfirmacaoCancelar()
        }
    }

    private fun salvarAvaliacaoNoBanco(nota: Float, titulo: String, comentario: String, idUsuario: String) {
        val timestampAtual = Timestamp.now()

        val novaAvaliacaoRef = db.collection("avaliacoes").document()
        val idAvaliacao = novaAvaliacaoRef.id

        val avaliacao = Avaliacao(
            id = idAvaliacao,
            idLivro = idLivro ?: "ID_LIVRO_TESTE_123",
            idUsuario = idUsuario,
            titulo = titulo,
            descricao = comentario,
            nota = nota,
            data = timestampAtual,
            curtidas = 0
        )

        novaAvaliacaoRef.set(avaliacao)
            .addOnSuccessListener {
                Toast.makeText(this, "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar avaliação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarConfirmacaoCancelar() {
        val dialogBinding = DialogConfirmacaoCancelarBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnNao.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSim.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_busca

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