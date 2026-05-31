package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.*
import com.example.biblioteca_app.models.Avaliacao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat

class MaisAvaliacoesActivity : AppCompatActivity() {

    private lateinit var binding: TelaMaisAvaliacoesBinding
    private val db = FirebaseFirestore.getInstance()

    private var idLivro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaMaisAvaliacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idLivro = intent.getStringExtra("ID_LIVRO") ?: ""

        if (idLivro.isEmpty()) {
            Toast.makeText(this, "Livro inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarBotoes()
        carregarAvaliacoesPorCurtidas()
        setupNavBar()
    }

    // =========================
    // CARREGAMENTO PRINCIPAL
    // =========================
    private fun carregarAvaliacoesPorCurtidas() {

        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("curtidas", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { docs ->

                binding.containerAvaliacoes.removeAllViews()

                val avaliacoes = docs.mapNotNull {
                    it.toObject(Avaliacao::class.java).copy(id = it.id)
                }

                atualizarResumo(avaliacoes)

                if (avaliacoes.isEmpty()) {
                    binding.txtSemAvaliacoes.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                binding.txtSemAvaliacoes.visibility = View.GONE

                avaliacoes.forEach {
                    adicionarItemAvaliacao(it)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar avaliações", Toast.LENGTH_SHORT).show()
            }
    }

    // =========================
    // RESUMO (MÉDIA + TOTAL)
    // =========================
    private fun atualizarResumo(avaliacoes: List<Avaliacao>) {

        val total = avaliacoes.size
        val media = if (total == 0) 0.0 else avaliacoes.map { it.nota }.average()

        val df = DecimalFormat("#.#")

        binding.layoutResumo.txtMedia.text = df.format(media)
        binding.layoutResumo.txtTotalAvaliacoes.text = "($total avaliações)"
        binding.layoutResumo.txtEstrelasMedia.text = converterEstrelas(media)

        binding.txtTituloHeader.text = "Avaliações ($total)"
    }

    // =========================
    // ESTRELAS
    // =========================
    private fun converterEstrelas(media: Double): String {

        val estrelas = media.toInt()
        val resto = media - estrelas

        val builder = StringBuilder()

        repeat(estrelas) { builder.append("⭐") }

        if (resto >= 0.5) builder.append("☆")

        return builder.toString().padEnd(5, '☆')
    }

    // =========================
    // ITEM DA LISTA
    // =========================
    private fun adicionarItemAvaliacao(avaliacao: Avaliacao) {

        val item = ItemAvaliacaoBinding.inflate(
            layoutInflater,
            binding.containerAvaliacoes,
            false
        )

        binding.containerAvaliacoes.addView(item.root)

        db.collection("usuarios")
            .document(avaliacao.idUsuario)
            .get()
            .addOnSuccessListener { user ->

                val nome = user.getString("nome") ?: "Usuário"

                item.txtNomeUsuario.text = nome
                item.txtTituloAvaliacao.text = avaliacao.titulo
                item.txtComentario.text = avaliacao.descricao
                item.txtCurtidas.text = avaliacao.curtidas.toString()

                item.txtData.visibility = View.GONE

                configurarLikeLocal(item, avaliacao)
            }
    }

    // =========================
    // LIKE (LOCAL + FIRESTORE OPCIONAL)
    // =========================
    private fun configurarLikeLocal(
        item: ItemAvaliacaoBinding,
        avaliacao: Avaliacao
    ) {
        var curtido = false
        var count = avaliacao.curtidas

        item.btnCurtir.setOnClickListener {

            curtido = !curtido

            if (curtido) {
                count++
                item.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
            } else {
                count--
                item.btnCurtir.setImageResource(R.drawable.ic_heart)
            }

            item.txtCurtidas.text = count.toString()

            // opcional (persistir no banco)
            db.collection("avaliacoes")
                .document(avaliacao.id)
                .update("curtidas", count)
        }

        item.btnDenunciar.setOnClickListener {
            mostrarDialogDenuncia()
        }
    }

    // =========================
    // BOTÕES
    // =========================
    private fun configurarBotoes() {

        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnAvaliar.setOnClickListener {
            val intent = Intent(this, AvaliarActivity::class.java)
            intent.putExtra("ID_LIVRO", idLivro)
            startActivity(intent)
        }

        binding.btnOrdenar.setOnClickListener {

            val opcoes = arrayOf("Mais curtidas", "Menor curtidas")

            AlertDialog.Builder(this)
                .setTitle("Ordenar por")
                .setItems(opcoes) { _, which ->

                    when (which) {
                        0 -> carregarAvaliacoesPorCurtidasDesc()
                        1 -> carregarAvaliacoesPorCurtidasAsc()
                    }
                }
                .show()
        }
    }

    private fun carregarAvaliacoesPorCurtidasDesc() {
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("curtidas", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { docs ->
                renderLista(docs)
            }
    }

    private fun carregarAvaliacoesPorCurtidasAsc() {
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("curtidas", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { docs ->
                renderLista(docs)
            }
    }

    private fun renderLista(docs: com.google.firebase.firestore.QuerySnapshot) {

        binding.containerAvaliacoes.removeAllViews()

        val avaliacoes = docs.map {
            it.toObject(Avaliacao::class.java).copy(id = it.id)
        }

        atualizarResumo(avaliacoes)

        avaliacoes.forEach {
            adicionarItemAvaliacao(it)
        }
    }

    // =========================
    // NAVBAR
    // =========================
    private fun setupNavBar() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_busca

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TelaHomeActivity::class.java))
                R.id.nav_busca -> startActivity(Intent(this, BuscaActivity::class.java))
                R.id.nav_notif -> startActivity(Intent(this, NotificacoesActivity::class.java))
                R.id.nav_menu -> startActivity(Intent(this, MenuActivity::class.java))
                else -> return@setOnItemSelectedListener false
            }
            finish()
            true
        }
    }

    // =========================
    // DENÚNCIA
    // =========================
    private fun mostrarDialogDenuncia() {

        val dialogBinding = DialogDenunciaBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelar.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnEnviar.setOnClickListener {

            val selected = dialogBinding.radioGroup.checkedRadioButtonId

            if (selected == -1) {
                Toast.makeText(this, "Selecione um motivo", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                Toast.makeText(this, "Denúncia enviada", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}