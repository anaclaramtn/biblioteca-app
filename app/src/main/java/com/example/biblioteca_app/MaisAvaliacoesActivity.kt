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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

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

        // Carregamento inicial padrão: Mais curtidas primeiro
        carregarAvaliacoes("curtidas", Query.Direction.DESCENDING)

        setupNavBar()
    }

    // ==========================================
    // CARREGAMENTO DINÂMICO (CURTIDAS E DATA)
    // ==========================================
    private fun carregarAvaliacoes(campoOrdenacao: String, direcao: Query.Direction) {
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy(campoOrdenacao, direcao)
            .get()
            .addOnSuccessListener { docs ->
                binding.containerAvaliacoes.removeAllViews()

                val avaliacoes = docs.mapNotNull { doc ->
                    val av = doc.toObject(Avaliacao::class.java)
                    // Garantimos que pegamos o ID do documento e a data do timestamp do Firestore
                    av.copy(id = doc.id)
                }

                atualizarResumo(avaliacoes)

                if (avaliacoes.isEmpty()) {
                    binding.txtSemAvaliacoes.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                binding.txtSemAvaliacoes.visibility = View.GONE

                // Passamos o documento original também para extrair o Timestamp com segurança externa
                docs.forEachIndexed { index, doc ->
                    if (index < avaliacoes.size) {
                        val timestamp = doc.getTimestamp("data")
                        adicionarItemAvaliacao(avaliacoes[index], timestamp)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar avaliações: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private fun adicionarItemAvaliacao(avaliacao: Avaliacao, timestamp: Timestamp?) {
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

                // Exibição e formatação de data corrigida
                if (timestamp != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    item.txtData.text = sdf.format(timestamp.toDate())
                    item.txtData.visibility = View.VISIBLE
                } else {
                    item.txtData.visibility = View.GONE
                }

                configurarLikeEModais(item, avaliacao)
            }
    }

    // ==========================================
    // LIKE (ATÔMICO) + ACIONAMENTO DE DENÚNCIA
    // ==========================================
    private fun configurarLikeEModais(
        item: ItemAvaliacaoBinding,
        avaliacao: Avaliacao
    ) {
        var curtido = false
        var count = avaliacao.curtidas

        item.btnCurtir.setOnClickListener {
            curtido = !curtido
            val ref = db.collection("avaliacoes").document(avaliacao.id)

            if (curtido) {
                count++
                item.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                ref.update("curtidas", FieldValue.increment(1))
            } else {
                count--
                item.btnCurtir.setImageResource(R.drawable.ic_heart)
                ref.update("curtidas", FieldValue.increment(-1))
            }

            item.txtCurtidas.text = count.toString()
            avaliacao.curtidas = count
        }

        item.btnDenunciar.setOnClickListener {
            mostrarDialogDenuncia(avaliacao.id)
        }
    }

    // =========================
    // BOTÕES E ALERT DIALOG
    // =========================
    private fun configurarBotoes() {
        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnAvaliar.setOnClickListener {
            val intent = Intent(this, AvaliarActivity::class.java)
            intent.putExtra("ID_LIVRO", idLivro)
            startActivity(intent)
        }

        binding.btnOrdenar.setOnClickListener {
            // As 4 opções solicitadas para o menu de ordenação
            val opcoes = arrayOf("Mais curtidas", "Menos curtidas", "Mais recentes", "Menos recentes")

            AlertDialog.Builder(this)
                .setTitle("Ordenar por")
                .setItems(opcoes) { _, which ->
                    when (which) {
                        0 -> carregarAvaliacoes("curtidas", Query.Direction.DESCENDING)
                        1 -> carregarAvaliacoes("curtidas", Query.Direction.ASCENDING)
                        2 -> carregarAvaliacoes("data", Query.Direction.DESCENDING)
                        3 -> carregarAvaliacoes("data", Query.Direction.ASCENDING)
                    }
                }
                .show()
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
    private fun mostrarDialogDenuncia(idAvaliacao: String) {
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
                // Aqui você pode recuperar qual RadioButton foi selecionado se necessário, ex:
                // val motivo = findViewById<RadioButton>(selected).text.toString()

                val denuncia = hashMapOf(
                    "idAvaliacao" to idAvaliacao,
                    "data" to Timestamp.now()
                )

                db.collection("denuncias")
                    .add(denuncia)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        Toast.makeText(this, "Denúncia enviada com sucesso", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao enviar denúncia", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        dialog.show()
    }
}